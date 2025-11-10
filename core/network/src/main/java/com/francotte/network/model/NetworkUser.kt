package com.francotte.network.model

import com.francotte.model.ConnectionMethod
import kotlinx.serialization.Serializable

@Serializable
data class NetworkUser(
    val userId: Long,
    val username: String? = null,
    val email:String?=null,
    val image: String?=null,
    @Transient val provider: ConnectionMethod = ConnectionMethod.EMAIL,
)