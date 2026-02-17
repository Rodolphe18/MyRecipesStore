package com.francotte.model

import java.time.Instant

class Ingredient(
    val name: String,
    val description:String,
    val imageUrl:String,
    val savedTimeStamp: Instant?=null
)
