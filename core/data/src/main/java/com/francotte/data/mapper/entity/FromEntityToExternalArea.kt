package com.francotte.data.mapper.entity

import com.francotte.database.model.AreaEntity
import com.francotte.model.Area

fun AreaEntity.asExternalModel() = Area(name = strArea)
