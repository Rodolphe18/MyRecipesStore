package com.francotte.ui


val DeviceMode.nbHomeColumns: Int
    get() =
        when (this) {
            DeviceMode.PhonePortrait -> 1
            DeviceMode.PhoneLandscape -> 3
            DeviceMode.TabletPortrait -> 2
            DeviceMode.TabletLandscape -> 4
        }

val DeviceMode.nbCategoriesColumns: Int
    get() =
        when (this) {
            DeviceMode.PhonePortrait -> 2
            else -> 4
        }

val DeviceMode.nbSectionColumns: Int
    get() =
        when (this) {
            DeviceMode.PhonePortrait -> 2
            else -> 4
        }

val DeviceMode.nbSectionFavorites: Int
    get() =
        when (this) {
            DeviceMode.PhonePortrait -> 2
            DeviceMode.PhoneLandscape -> 3
            DeviceMode.TabletPortrait -> 2
            DeviceMode.TabletLandscape -> 4
        }

val DeviceMode.nbIngredientsColumns: Int
    get() =
        when (this) {
            DeviceMode.PhonePortrait -> 3
            else -> 6

        }
