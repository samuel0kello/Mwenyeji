package com.samuelokello.mwenyeji.datasources.core.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError

@PublishedApi
internal object FirebaseErrorMapper {
    fun map(throwable: Throwable): RemoteError =
        when (throwable) {
            is FirebaseNetworkException -> {
                RemoteError.NoNetwork
            }

            is FirebaseAuthInvalidUserException -> {
                when (throwable.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> RemoteError.UserNotFound
                    "ERROR_USER_DISABLED" -> RemoteError.UserDisabled
                    else -> RemoteError.InvalidCredentials
                }
            }

            is FirebaseAuthInvalidCredentialsException -> {
                RemoteError.InvalidCredentials
            }

            is FirebaseAuthUserCollisionException -> {
                when (throwable.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> RemoteError.EmailAlreadyInUse
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> RemoteError.AccountCollision
                    else -> RemoteError.AccountCollision
                }
            }

            is FirebaseAuthWeakPasswordException -> {
                RemoteError.WeakPassword
            }

            is FirebaseAuthRecentLoginRequiredException -> {
                RemoteError.RequiresRecentLogin
            }

            is FirebaseFirestoreException -> {
                when (throwable.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> RemoteError.PermissionDenied
                    FirebaseFirestoreException.Code.NOT_FOUND -> RemoteError.NotFound
                    FirebaseFirestoreException.Code.UNAVAILABLE -> RemoteError.Unavailable
                    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> RemoteError.DeadlineExceeded
                    FirebaseFirestoreException.Code.CANCELLED -> RemoteError.Cancelled
                    FirebaseFirestoreException.Code.ALREADY_EXISTS -> RemoteError.AlreadyExists
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> RemoteError.Unauthorized
                    else -> RemoteError.Unknown(throwable.message ?: "Firestore: ${throwable.code}")
                }
            }

            else -> {
                RemoteError.Unknown(throwable.message ?: throwable::class.simpleName.orEmpty())
            }
        }
}
