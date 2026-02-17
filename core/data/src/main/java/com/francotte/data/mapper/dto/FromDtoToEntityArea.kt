package com.francotte.data.mapper.dto

import com.francotte.database.model.AreaEntity
import com.francotte.network.model.NetworkArea

fun NetworkArea.asEntity(): AreaEntity = AreaEntity(strArea = strArea)
