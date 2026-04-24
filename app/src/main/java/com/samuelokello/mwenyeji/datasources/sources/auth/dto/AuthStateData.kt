package com.samuelokello.mwenyeji.datasources.sources.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthStateData(
    val uid: String?,
    val isAnonymous: Boolean,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
)
