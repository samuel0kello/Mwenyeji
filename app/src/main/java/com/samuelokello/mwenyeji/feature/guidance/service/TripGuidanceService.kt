package com.samuelokello.mwenyeji.feature.guidance.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.models.GeoPoint
import com.samuelokello.mwenyeji.data.models.RideTrace
import com.samuelokello.mwenyeji.data.repository.RouteLearningRepository
import com.samuelokello.mwenyeji.datasources.preference.MwenyejiPrefs
import com.samuelokello.mwenyeji.feature.guidance.announcer.Announcer
import com.samuelokello.mwenyeji.feature.guidance.announcer.Language
import com.samuelokello.mwenyeji.feature.guidance.announcer.TtsSpeaker
import com.samuelokello.mwenyeji.feature.guidance.announcer.VibratorHaptics
import com.samuelokello.mwenyeji.feature.guidance.session.TripPhase
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEffect
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripEvent
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripPlan
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionContract.TripState
import com.samuelokello.mwenyeji.feature.guidance.session.TripSessionReducer
import com.samuelokello.mwenyeji.feature.guidance.session.TripThresholds
import com.samuelokello.mwenyeji.feature.guidance.trace.RideTraceRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Runs one guided trip in the foreground, so guidance keeps going with the screen off.
 *
 * Everything happens on the main thread: GPS fixes, notification taps and timers each become a
 * [TripEvent], the reducer decides, and the announcer speaks and vibrates. On EndTrip the service
 * stops GPS, lets the last sentence finish, then stops itself.
 */
class TripGuidanceService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val learning: RouteLearningRepository by inject()
    private val prefs: MwenyejiPrefs by inject()
    private lateinit var locationClient: FusedLocationProviderClient
    private var session: Session? = null
    private var startJob: Job? = null
    private var gpsLostJob: Job? = null
    private var stopJob: Job? = null

    private class Session(
        val request: TripRequest,
        val plan: TripPlan,
        val reducer: TripSessionReducer,
        val announcer: Announcer,
        // Null unless the rider agreed to share ride traces.
        val recorder: RideTraceRecorder?,
        val startedAt: Calendar = Calendar.getInstance(),
        var state: TripState = TripState(),
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        GuidanceNotifications.createChannel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START -> {
                val request = intent.getTripRequest()
                if (request == null) stopSelf() else start(request)
            }

            ACTION_ON_BOARD -> dispatch(TripEvent.UserTapsIOnBoard)

            ACTION_ALIGHTED -> dispatch(TripEvent.UserTapsIHaveAlighted)

            ACTION_CANCEL ->
                if (session == null) {
                    startJob?.cancel()
                    shutDown()
                } else {
                    dispatch(TripEvent.UserTapsCancel)
                }

            else -> if (session == null) stopSelf()
        }
        // A trip cannot resume after the process dies: the plan and the speech state are gone.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        session?.let { endSession(it) }
        scope.cancel()
        _trip.value = null
        super.onDestroy()
    }

    private fun start(request: TripRequest) {
        // A new trip replaces any running one.
        session?.let { endSession(it) }
        startJob?.cancel()
        stopJob?.cancel()

        if (!enterForeground(GuidanceNotifications.starting(this))) return
        _trip.value = GuidanceSnapshot(request.routeId, TripPhase.PLANNED, plan = null)

        // Start the speech engine now so it warms up while we look for the user.
        val announcer = Announcer(TtsSpeaker(this), VibratorHaptics(this), userLanguage())
        startJob = scope.launch {
            // Learned roads load while we wait for the first fix. A slow network must not hold up the trip.
            val knownPaths = async { withTimeoutOrNull(KNOWN_PATHS_TIMEOUT_MS) { loadKnownPaths(request) }.orEmpty() }
            val here = currentLocation()
            val plan = request.toPlan(here?.latitude, here?.longitude).copy(knownPaths = knownPaths.await())
            val recorder = if (prefs.getShareRideTraces().first() == true) RideTraceRecorder() else null
            session = Session(request, plan, TripSessionReducer(plan), announcer, recorder)
            _trip.value = GuidanceSnapshot(request.routeId, TripPhase.PLANNED, plan)
            dispatch(TripEvent.UserTapsStart(locationPermissionGranted = hasPreciseLocation()))
            if (session != null) startLocationUpdates()
        }
        // Stopped before the first fix arrived: nothing else owns the speech engine yet.
        startJob?.invokeOnCompletion { cause -> if (cause != null) announcer.close() }
    }

    private fun dispatch(event: TripEvent) {
        val current = session ?: return
        val (next, effects) = current.reducer.reduce(current.state, event)
        current.state = next
        if (event is TripEvent.LocationFix) current.recorder?.onFix(event, next.currentPhase)
        _trip.value = GuidanceSnapshot(current.request.routeId, next.currentPhase, current.plan)
        current.announcer.announce(effects)

        // The service cannot show a permission dialog; the route screen asks before starting.
        val cannotRun = TripEffect.RequestLocationPermission in effects
        if (TripEffect.EndTrip in effects || cannotRun) {
            finish(current)
        } else {
            notify(GuidanceNotifications.forPhase(this, current.plan, next.currentPhase))
        }
    }

    private fun finish(current: Session) {
        notify(GuidanceNotifications.forPhase(this, current.plan, current.state.currentPhase))
        endSession(current)
        shareRide(current)
        // Stay alive a little so the last sentence ("You've arrived…") is not cut off with the process.
        stopJob = scope.launch {
            delay(END_GRACE_MS)
            shutDown()
        }
    }

    private fun endSession(current: Session) {
        locationClient.removeLocationUpdates(locationCallback)
        gpsLostJob?.cancel()
        current.announcer.close()
        if (session === current) session = null
    }

    private fun shutDown() {
        _trip.value = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Learning from rides ──────────────────────────────────────────────────

    private suspend fun loadKnownPaths(request: TripRequest): List<List<GeoPoint>> =
        (learning.getKnownPaths(request.routeId, request.direction) as? DataResult.Success)?.data.orEmpty()

    /**
     * Sends the ride's path, if the rider agreed and the trip reached its stop. A cancelled trip may
     * have been the wrong bus, so its path says nothing about this route. Firestore queues the write
     * offline, so it survives the service stopping.
     */
    private fun shareRide(current: Session) {
        if (current.state.currentPhase != TripPhase.ARRIVED) return
        val points = current.recorder?.finish() ?: return
        val trace =
            RideTrace(
                routeId = current.request.routeId,
                direction = current.request.direction,
                boardingStopId = current.plan.stage.stopId,
                alightStopId = current.plan.alightStop.stopId,
                weekday = current.startedAt.get(Calendar.DAY_OF_WEEK),
                hour = current.startedAt.get(Calendar.HOUR_OF_DAY),
                points = points,
            )
        scope.launch { learning.uploadRideTrace(trace) }
    }

    // ── Location ─────────────────────────────────────────────────────────────

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::onLocation)
        }
    }

    private fun onLocation(location: Location) {
        if (session == null) return
        // A fix this rough can put the user on the wrong side of a 30m stage radius.
        if (location.hasAccuracy() && location.accuracy > TripThresholds.MAX_FIX_ACCURACY_M) return
        restartGpsLostTimer()
        dispatch(
            TripEvent.LocationFix(
                lat = location.latitude,
                lng = location.longitude,
                speed = if (location.hasSpeed()) location.speed.toDouble() else 0.0,
                accuracy = location.accuracy.toDouble(),
                elapsedMillis = location.elapsedRealtimeNanos / 1_000_000,
            ),
        )
    }

    private fun restartGpsLostTimer() {
        gpsLostJob?.cancel()
        gpsLostJob = scope.launch {
            delay(TripThresholds.GPS_LOST_TIMEOUT_MS)
            dispatch(TripEvent.GpsLost)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasPreciseLocation()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS / 2)
            .build()
        locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        restartGpsLostTimer()
    }

    /** One fresh fix to measure the walk to the stage, or null if none comes in time. */
    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): Location? {
        if (!hasPreciseLocation()) return null
        return withTimeoutOrNull(FIRST_FIX_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                val cancellation = CancellationTokenSource()
                locationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { continuation.resume(null) }
                continuation.invokeOnCancellation { cancellation.cancel() }
            }
        }
    }

    private fun hasPreciseLocation(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    // ── Foreground and notification ──────────────────────────────────────────

    private fun enterForeground(notification: Notification): Boolean {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        return try {
            ServiceCompat.startForeground(this, GuidanceNotifications.NOTIFICATION_ID, notification, type)
            true
        } catch (e: SecurityException) {
            // Location permission was revoked between the tap and here.
            stopSelf()
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun notify(notification: Notification) {
        // Without the notification permission the trip still runs; only the notification is hidden.
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            NotificationManagerCompat.from(this).notify(GuidanceNotifications.NOTIFICATION_ID, notification)
        }
    }

    /** No language setting yet: Swahili phones get Swahili, everyone else English. */
    private fun userLanguage(): Language = if (Locale.getDefault().language == "sw") Language.SWAHILI else Language.ENGLISH

    companion object {
        internal const val ACTION_START = "com.samuelokello.mwenyeji.guidance.START"
        internal const val ACTION_ON_BOARD = "com.samuelokello.mwenyeji.guidance.ON_BOARD"
        internal const val ACTION_ALIGHTED = "com.samuelokello.mwenyeji.guidance.ALIGHTED"
        internal const val ACTION_CANCEL = "com.samuelokello.mwenyeji.guidance.CANCEL"

        private const val LOCATION_INTERVAL_MS = 2_000L
        private const val FIRST_FIX_TIMEOUT_MS = 10_000L
        private const val END_GRACE_MS = 8_000L
        private const val KNOWN_PATHS_TIMEOUT_MS = 5_000L

        private val _trip = MutableStateFlow<GuidanceSnapshot?>(null)

        /** The running trip, or null when none is running. */
        val trip: StateFlow<GuidanceSnapshot?> = _trip.asStateFlow()

        /** Call only once precise location is granted. */
        fun start(
            context: Context,
            request: TripRequest,
        ) {
            val intent = Intent(context, TripGuidanceService::class.java)
                .setAction(ACTION_START)
                .putTripRequest(request)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) = send(context, ACTION_CANCEL)

        fun onBoard(context: Context) = send(context, ACTION_ON_BOARD)

        fun alighted(context: Context) = send(context, ACTION_ALIGHTED)

        private fun send(
            context: Context,
            action: String,
        ) {
            context.startService(Intent(context, TripGuidanceService::class.java).setAction(action))
        }
    }
}

/**
 * What the guidance screen draws.
 *
 * @param routeId The route the trip is on, so a route's screen only offers to open its own trip.
 * @param plan    Null for the first few seconds, while the service waits for a location fix.
 */
data class GuidanceSnapshot(
    val routeId: String,
    val phase: TripPhase,
    val plan: TripPlan?,
)
