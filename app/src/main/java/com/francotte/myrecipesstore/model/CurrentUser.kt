package com.francotte.myrecipesstore.model

import androidx.compose.runtime.Immutable
import com.francotte.myrecipesstore.protobuf.User
import com.francotte.myrecipesstore.protobuf.user
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Immutable
@Serializable
data class CurrentUser(
    val userId: Long,
    val username: String? = null,
    @Transient val provider: Provider = Provider.EMAIL,
)  {

    constructor(user: User) : this(
        userId = user.id,
        username = user.userName,
        provider = user.method.toProvider()
    )
    fun toProto() = user {
        id = userId;
        userName = username!!;
        method = provider.toConnectionMethod()
    }


}