package com.samuelokello.mwenyeji.feature.guidance.announcer

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * [Speaker] backed by Android's [TextToSpeech].
 *
 * - Speaks as navigation guidance, so music lowers its volume (ducks) instead of stopping.
 * - Urgent sentences flush the queue, unless an urgent sentence is speaking: then they wait their turn.
 * - Sheng is spoken by the Swahili voice. A missing voice falls back to English.
 * - Losing audio focus to a call stops speech; it does not resume, the next sentence speaks fresh.
 *
 * Owned by whoever runs the trip (the foreground service), so it lives as long as the trip, not a screen.
 */
class TtsSpeaker(
    context: Context,
) : Speaker {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()

    private val _state = MutableStateFlow<SpeakerState>(SpeakerState.Starting)
    override val state: StateFlow<SpeakerState> = _state.asStateFlow()

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    // Everything below is guarded by [lock].
    private val held = mutableListOf<Utterance>()

    // Utterances handed to the engine and not yet finished, by id.
    private val inFlight = mutableMapOf<String, SpeechPriority>()
    private var speakingId: String? = null
    private var nextId = 0
    private var currentLanguage: Language? = null
    private var shuttingDown = false
    private var released = false
    private var focusRequest: Any? = null

    private val tts: TextToSpeech = TextToSpeech(appContext) { status -> onInit(status) }

    override fun speak(utterance: Utterance): Unit =
        synchronized(lock) {
            if (shuttingDown || released) return
            when (_state.value) {
                SpeakerState.Starting -> {
                    // Same rule as the queue: an urgent sentence replaces whatever was waiting.
                    if (utterance.priority == SpeechPriority.URGENT) held.clear()
                    held += utterance
                }

                is SpeakerState.Ready -> send(utterance)

                SpeakerState.Unavailable -> Unit
            }
        }

    override fun stop(): Unit =
        synchronized(lock) {
            if (released) return
            held.clear()
            if (_state.value is SpeakerState.Ready) tts.stop()
            inFlight.clear()
            speakingId = null
            abandonFocus()
            if (shuttingDown) mainHandler.post { release() }
        }

    override fun shutdown(): Unit =
        synchronized(lock) {
            if (shuttingDown || released) return
            shuttingDown = true
            // Safety net in case the engine never reports that it finished.
            mainHandler.postDelayed({ release() }, SHUTDOWN_TIMEOUT_MS)
            if (_state.value != SpeakerState.Starting && inFlight.isEmpty()) release()
        }

    private fun onInit(status: Int): Unit =
        synchronized(lock) {
            if (released) return
            if (status != TextToSpeech.SUCCESS) {
                held.clear()
                _state.value = SpeakerState.Unavailable
                if (shuttingDown) release()
                return
            }
            tts.setAudioAttributes(attributes)
            tts.setOnUtteranceProgressListener(progressListener)
            val voices = Language.entries.filter { tts.isLanguageAvailable(it.locale) >= TextToSpeech.LANG_AVAILABLE }.toSet()
            _state.value = SpeakerState.Ready(voices)

            val waiting = held.toList()
            held.clear()
            waiting.forEach(::send)
            if (shuttingDown && inFlight.isEmpty()) release()
        }

    /** Caller holds [lock]. */
    private fun send(utterance: Utterance) {
        val voices = (_state.value as? SpeakerState.Ready)?.voices.orEmpty()
        val language = if (utterance.language in voices) utterance.language else Language.ENGLISH
        val text = if (language == utterance.language) utterance.text else utterance.englishText

        val urgentSpeaking = speakingId?.let { inFlight[it] } == SpeechPriority.URGENT
        val flush = utterance.priority == SpeechPriority.URGENT && !urgentSpeaking
        if (flush) {
            // The engine drops everything queued; its stop callbacks may arrive later and find nothing.
            inFlight.clear()
            speakingId = null
        }

        if (language != currentLanguage) {
            tts.language = language.locale
            currentLanguage = language
        }
        if (inFlight.isEmpty()) requestFocus()
        val id = "utterance-${nextId++}"
        inFlight[id] = utterance.priority
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        if (tts.speak(text, queueMode, null, id) != TextToSpeech.SUCCESS) finished(id)
    }

    /** Caller holds [lock]. */
    private fun finished(id: String) {
        inFlight.remove(id)
        if (speakingId == id) speakingId = null
        if (inFlight.isEmpty()) {
            abandonFocus()
            if (shuttingDown) mainHandler.post { release() }
        }
    }

    private fun release(): Unit =
        synchronized(lock) {
            if (released) return
            released = true
            mainHandler.removeCallbacksAndMessages(null)
            held.clear()
            inFlight.clear()
            abandonFocus()
            tts.shutdown()
        }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {
            synchronized(lock) { if (utteranceId in inFlight) speakingId = utteranceId }
        }

        override fun onDone(utteranceId: String) {
            synchronized(lock) { finished(utteranceId) }
        }

        override fun onStop(
            utteranceId: String,
            interrupted: Boolean,
        ) {
            synchronized(lock) { finished(utteranceId) }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String) {
            synchronized(lock) { finished(utteranceId) }
        }
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = (focusRequest as? AudioFocusRequest)
                ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(focusListener, mainHandler)
                    .build()
                    .also { focusRequest = it }
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    // A phone call (or anything else that takes the audio for itself) silences guidance at once.
    private val focusListener =
        AudioManager.OnAudioFocusChangeListener { change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop()
        }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (focusRequest as? AudioFocusRequest)?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }

    private companion object {
        const val SHUTDOWN_TIMEOUT_MS = 15_000L
    }
}

/** Sheng has no locale or voice of its own; the Swahili voice reads it. */
private val Language.locale: Locale
    get() = when (this) {
        Language.ENGLISH -> Locale.Builder().setLanguage("en").setRegion("KE").build()

        Language.SWAHILI,
        Language.SHENG,
        -> Locale.Builder().setLanguage("sw").setRegion("KE").build()
    }
