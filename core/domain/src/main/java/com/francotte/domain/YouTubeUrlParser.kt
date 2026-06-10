package com.francotte.domain

object YouTubeUrlParser {

    /** Extracts the YouTube video id from the various URL formats, or "" if none matches. */
    fun extractVideoId(url: String): String = when {
        url.contains("watch?v=") ->
            url.substringAfter("v=").substringBefore("&")
        url.contains("youtu.be/") ->
            url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        url.contains("/shorts/") ->
            url.substringAfter("/shorts/").substringBefore("?").substringBefore("&")
        url.contains("/embed/") ->
            url.substringAfter("/embed/").substringBefore("?").substringBefore("&")
        else -> ""
    }
}
