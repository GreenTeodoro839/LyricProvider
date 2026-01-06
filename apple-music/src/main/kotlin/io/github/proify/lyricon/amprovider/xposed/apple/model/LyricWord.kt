package io.github.proify.lyricon.amprovider.xposed.apple.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricWord(
    override var agent: String? = null,
    override var begin: Int = 0,
    override var duration: Int = 0,
    override var end: Int = 0,
    var text: String? = null
) : LyricTiming