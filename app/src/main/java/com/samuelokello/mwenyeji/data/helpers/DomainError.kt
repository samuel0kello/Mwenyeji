package com.samuelokello.mwenyeji.data.helpers

/**
 * User-meaningful error categories. Repositories translate infrastructure
 * errors ([RemoteError]) into these. ViewModels/UI never see RemoteError.
 */
sealed class DomainError {
    // Auth
    data object InvalidCredentials : DomainError()

    data object UserNotFound : DomainError()

    data object EmailAlreadyInUse : DomainError()

    data object WeakPassword : DomainError()

    data object AccountDisabled : DomainError()

    data object SessionExpired : DomainError()

    // Sign-in UX
    data object UserCancelled : DomainError()

    data object NoGoogleAccount : DomainError()

    // Connectivity / availability
    data object NoConnection : DomainError()

    data object ServiceUnavailable : DomainError()

    // Authorization
    data object NotAuthorized : DomainError()

    // Fallback
    data class Unknown(
        val technicalMessage: String,
    ) : DomainError()
}

fun DomainError.toUserMessage(): String =
    when (this) {
        DomainError.NoConnection -> "No internet connection. Check your network and try again."
        DomainError.NoGoogleAccount -> "No Google account found on this device."
        DomainError.ServiceUnavailable -> "Service is temporarily unavailable. Try again in a moment."
        DomainError.NotAuthorized -> "You don't have access to do that."
        DomainError.SessionExpired -> "Your session expired. Please sign in again."
        DomainError.UserCancelled -> ""
        DomainError.InvalidCredentials -> "Invalid credentials."
        DomainError.UserNotFound -> "No account found with that email."
        DomainError.EmailAlreadyInUse -> "That email is already registered."
        DomainError.WeakPassword -> "Password is too weak."
        DomainError.AccountDisabled -> "This account has been disabled."
        is DomainError.Unknown -> "Something went wrong. Please try again."
    }
