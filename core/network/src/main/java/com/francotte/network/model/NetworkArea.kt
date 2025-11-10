package com.francotte.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkAreas(val meals:List<NetworkArea>)

@Serializable
data class NetworkArea(val strArea:String)

