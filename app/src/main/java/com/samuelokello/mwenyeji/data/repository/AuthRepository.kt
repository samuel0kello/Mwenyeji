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
import com.samuelokello.mwenyeji.data.helpers.DataResult
import com.samuelokello.mwenyeji.data.helpers.DomainError
import com.samuelokello.mwenyeji.data.helpers.toDataResult
import com.samuelokello.mwenyeji.datasources.sources.auth.AuthRemoteDataSource
import com.samuelokello.mwenyeji.datasources.sources.auth.dto.AuthStateData
import com.samuelokello.mwenyeji.feature.auth.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUserId: String?
    val isAnonymous: Boolean

    suspend fun signInAnonymously(): DataResult<String>

    suspend fun signInWithEmail(email: String, password: String): DataResult<String>

    suspend fun createAccountWithEmail(email: String, password: String): DataResult<String>

    suspend fun signInWithGoogle(context: Context): DataResult<String>

    suspend fun signOut(context: Context): DataResult<Unit>
}

internal class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override val authState: Flow<AuthState> =
        authRemoteDataSource.observeAuthState().map { it.toAuthState() }

    override val currentUserId: String? get() = authRemoteDataSource.currentUserId
    override val isAnonymous: Boolean get() = authRemoteDataSource.isAnonymous

    override suspend fun signInAnonymously(): DataResult<String> = authRemoteDataSource.signInAnonymously().toDataResult()

    override suspend fun signInWithEmail(email: String, password: String): DataResult<String> =
        authRemoteDataSource.signInWithEmail(email, password).toDataResult()

    override suspend fun createAccountWithEmail(email: String, password: String): DataResult<String> =
        authRemoteDataSource.createAccountWithEmail(email, password).toDataResult()

    override suspend fun signInWithGoogle(context: Context): DataResult<String> {
        val idToken =
            when (val credResult = obtainGoogleIdToken(context)) {
                is GoogleCredentialResult.Success -> credResult.idToken

                is GoogleCredentialResult.Cancelled -> return DataResult.Error(DomainError.UserCancelled)

                is GoogleCredentialResult.NoAccount -> return DataResult.Error(DomainError.NoGoogleAccount)

                is GoogleCredentialResult.Failed -> return DataResult.Error(
                    DomainError.Unknown(credResult.message),
                )
            }
        return authRemoteDataSource.signInWithGoogle(idToken).toDataResult()
    }

    override suspend fun signOut(context: Context): DataResult<Unit> {
        val result = authRemoteDataSource.signOut().toDataResult()
        // Always try to clear Credential Manager state, even if Firebase sign-out failed.
        runCatching {
            CredentialManager
                .create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { Log.w(TAG, "clearCredentialState failed (non-fatal): ${it.message}") }
        return result
    }

    // Google Credential Manager
    private sealed interface GoogleCredentialResult {
        data class Success(
            val idToken: String,
        ) : GoogleCredentialResult

        data object Cancelled : GoogleCredentialResult

        data object NoAccount : GoogleCredentialResult

        data class Failed(
            val message: String,
        ) : GoogleCredentialResult
    }

    private suspend fun obtainGoogleIdToken(context: Context): GoogleCredentialResult {
        val credentialManager = CredentialManager.create(context)
        val clientId = context.getString(R.string.default_web_client_id)

        Log.d(TAG, "Starting Google sign-in. clientId=${clientId.take(10)}...")

        for (strategy in googleSignInStrategies(clientId)) {
            return when (val result = tryStrategy(strategy, context, credentialManager)) {
                is GoogleCredentialResult.Success -> result

                is GoogleCredentialResult.Cancelled -> result

                // don't retry on cancel
                is GoogleCredentialResult.NoAccount -> continue

                // try next
                is GoogleCredentialResult.Failed -> continue // try next
            }
        }
        return GoogleCredentialResult.NoAccount
    }

    private fun googleSignInStrategies(clientId: String): List<SignInStrategy> =
        listOf(
            SignInStrategy("AuthorizedOnly") {
                val option =
                    GetGoogleIdOption
                        .Builder()
                        .setServerClientId(clientId)
                        .setFilterByAuthorizedAccounts(true)
                        .setAutoSelectEnabled(false)
                        .build()
                GetCredentialRequest.Builder().addCredentialOption(option).build()
            },
            SignInStrategy("AllAccounts") {
                val option =
                    GetGoogleIdOption
                        .Builder()
                        .setServerClientId(clientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build()
                GetCredentialRequest.Builder().addCredentialOption(option).build()
            },
            SignInStrategy("ButtonFlow") {
                val option = GetSignInWithGoogleOption.Builder(clientId).build()
                GetCredentialRequest.Builder().addCredentialOption(option).build()
            },
        )

    private data class SignInStrategy(
        val name: String,
        val buildRequest: () -> GetCredentialRequest,
    )

    private suspend fun tryStrategy(
        strategy: SignInStrategy,
        context: Context,
        credentialManager: CredentialManager,
    ): GoogleCredentialResult =
        try {
            val response = credentialManager.getCredential(context, strategy.buildRequest())
            val credential = response.credential

            if (credential is CustomCredential &&
                credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                Log.d(TAG, "${strategy.name}: success")
                GoogleCredentialResult.Success(token)
            } else {
                Log.w(TAG, "${strategy.name}: unexpected credential type ${credential.type}")
                GoogleCredentialResult.Failed("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "${strategy.name}: user cancelled")
            GoogleCredentialResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.d(TAG, "${strategy.name}: no credential — ${e.message}")
            GoogleCredentialResult.NoAccount
        } catch (e: GetCredentialException) {
            Log.w(TAG, "${strategy.name}: GetCredentialException ${e.type} — ${e.message}")
            GoogleCredentialResult.Failed(e.message ?: "Credential error")
        }

    private fun AuthStateData.toAuthState(): AuthState =
        when {
            uid == null -> {
                AuthState.Anonymous
            }

            isAnonymous -> {
                AuthState.Anonymous
            }

            else -> {
                AuthState.SignedIn(
                    uid = uid,
                    displayName = displayName,
                    email = email,
                    photoUrl = photoUrl,
                )
            }
        }

    private companion object {
        const val TAG = "MwenyejiAuth"
    }
}
