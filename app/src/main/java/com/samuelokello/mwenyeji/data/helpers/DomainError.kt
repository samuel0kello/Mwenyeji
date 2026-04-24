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
