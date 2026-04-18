package com.samuelokello.mwenyeji.feature.auth

sealed interface AuthState {
    data object Loading : AuthState
 
    data object Anonymous : AuthState
 
    data class SignedIn(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : AuthState
}
