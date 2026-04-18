package com.samuelokello.mwenyeji.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.samuelokello.mwenyeji.R
import com.samuelokello.mwenyeji.datasources.firebase.FirebaseService
import com.samuelokello.mwenyeji.feature.auth.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthRepository {
    fun authState(): Flow<AuthState>

    val currentUserId: String?
    val isAnonymous: Boolean

    suspend fun signInAnonymously(): String?

    suspend fun signInWithGoogle(context: Context): Result<String>

    suspend fun signOut(context: Context)

    suspend fun login(email: String, password: String): String?

    suspend fun signup(email: String, password: String): String?
}

class AuthRepositoryImpl(
    private val firebaseService: FirebaseService,
) : AuthRepository {
    private val TAG = "MwenyejiAuth"

    override fun authState(): Flow<AuthState> =
        firebaseService.authStateFlow().map { data ->
            when {
                data.uid == null -> {
                    AuthState.Anonymous
                }

                data.isAnonymous -> {
                    AuthState.Anonymous
                }

                else -> {
                    AuthState.SignedIn(
                        uid = data.uid,
                        displayName = data.displayName,
                        email = data.email,
                        photoUrl = data.photoUrl,
                    )
                }
            }
        }

    override val currentUserId: String?
        get() = firebaseService.currentUserId()

    override val isAnonymous: Boolean
        get() = firebaseService.isAnonymous()

    override suspend fun signInAnonymously(): String? = firebaseService.signInAnonymously()

    override suspend fun signInWithGoogle(context: Context): Result<String> {
        val credentialManager = CredentialManager.create(context)
        val clientId = context.getString(R.string.default_web_client_id)

        Log.d(TAG, "Starting Google sign-in. clientId=${clientId.take(20)}...")

        // Strategy 1 — previously authorised accounts only
        Log.d(TAG, "Trying strategy 1: filterByAuthorizedAccounts=true")
        val idToken =
            tryGetIdToken(
                tag = "Strategy1",
                block = {
                    val option =
                        GetGoogleIdOption
                            .Builder()
                            .setServerClientId(clientId)
                            .setFilterByAuthorizedAccounts(true)
                            .setAutoSelectEnabled(false)
                            .build()
                    GetCredentialRequest.Builder().addCredentialOption(option).build()
                },
                context = context,
                credentialManager = credentialManager,
            )

                // Strategy 2 — all Google accounts on device
                ?: run {
                    Log.d(TAG, "Strategy 1 failed. Trying strategy 2: filterByAuthorizedAccounts=false")
                    tryGetIdToken(
                        tag = "Strategy2",
                        block = {
                            val option =
                                GetGoogleIdOption
                                    .Builder()
                                    .setServerClientId(clientId)
                                    .setFilterByAuthorizedAccounts(false)
                                    .setAutoSelectEnabled(false)
                                    .build()
                            GetCredentialRequest.Builder().addCredentialOption(option).build()
                        },
                        context = context,
                        credentialManager = credentialManager,
                    )
                }

                // Strategy 3 — GetSignInWithGoogleOption (newest API)
                ?: run {
                    Log.d(TAG, "Strategy 2 failed. Trying strategy 3: GetSignInWithGoogleOption")
                    tryGetIdToken(
                        tag = "Strategy3",
                        block = {
                            val option = GetSignInWithGoogleOption.Builder(clientId).build()
                            GetCredentialRequest.Builder().addCredentialOption(option).build()
                        },
                        context = context,
                        credentialManager = credentialManager,
                    )
                }

        Log.d(TAG, "All strategies done. idToken=${if (idToken != null) "obtained" else "null"}")

        if (idToken == null) {
            return Result.failure(
                Exception("No Google account found on this device. Please add a Google account in Settings and try again."),
            )
        }

        return try {
            val uid =
                firebaseService.signInWithGoogle(idToken)
                    ?: return Result.failure(Exception("Firebase authentication failed. Please try again."))
            Log.d(TAG, "Firebase sign-in success. uid=$uid")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in exception: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tries a single Credential Manager strategy.
     * Returns the idToken string on success.
     * Returns null if no credentials available (try next strategy).
     * Re-throws cancellation so the caller knows user cancelled.
     */
    private suspend fun tryGetIdToken(
        tag: String,
        block: () -> GetCredentialRequest,
        context: Context,
        credentialManager: CredentialManager,
    ): String? =
        try {
            val request = block()
            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            Log.d(TAG, "$tag: credential type = ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Log.d(TAG, "$tag: success — token obtained")
                token
            } else {
                Log.w(TAG, "$tag: unexpected credential type: ${credential.type}")
                null
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "$tag: user cancelled")
            throw e // re-throw — caller skips error snackbar for cancellations
        } catch (e: NoCredentialException) {
            Log.d(TAG, "$tag: NoCredentialException — ${e.message}")
            null // try next strategy
        } catch (e: GetCredentialException) {
            Log.w(TAG, "$tag: GetCredentialException type=${e.type} message=${e.message}")
            null // try next strategy
        } catch (e: Exception) {
            Log.e(TAG, "$tag: unexpected exception: ${e.message}")
            null
        }

    override suspend fun signOut(context: Context) {
        firebaseService.signOut()
        try {
            CredentialManager
                .create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "clearCredentialState failed (non-fatal): ${e.message}")
        }
    }

    override suspend fun login(email: String, password: String): String? = firebaseService.loginWithEmailAndPassword(email, password)

    override suspend fun signup(email: String, password: String): String? = firebaseService.createUserWithEmailAndPassword(email, password)
}
