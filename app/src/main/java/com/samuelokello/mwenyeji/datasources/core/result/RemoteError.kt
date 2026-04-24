package com.samuelokello.mwenyeji.datasources.core.result

sealed class RemoteError(
    open val technicalMessage: String,
) {
    // Auth
    data object InvalidCredentials : RemoteError("Invalid email or password")

    data object UserNotFound : RemoteError("No account with that email")

    data object EmailAlreadyInUse : RemoteError("Email already registered")

    data object WeakPassword : RemoteError("Password is too weak")

    data object UserDisabled : RemoteError("Account is disabled")

    data object RequiresRecentLogin : RemoteError("Please sign in again")

    data object AccountCollision : RemoteError("Account exists with different credentials")

    // Firestore
    data object PermissionDenied : RemoteError("You don't have permission to do that")

    data object NotFound : RemoteError("Resource not found")

    data object Unavailable : RemoteError("Service temporarily unavailable")

    data object DeadlineExceeded : RemoteError("Request timed out")

    data object Cancelled : RemoteError("Request was cancelled")

    data object AlreadyExists : RemoteError("Resource already exists")

    // Network / HTTP
    data object NoNetwork : RemoteError("No internet connection")

    data object Unauthorized : RemoteError("Authentication required")

    data object Forbidden : RemoteError("Access forbidden")

    data object BadRequest : RemoteError("Invalid request")

    data object ServerError : RemoteError("Server error")

    data class Unknown(
        override val technicalMessage: String,
    ) : RemoteError(technicalMessage)
}
