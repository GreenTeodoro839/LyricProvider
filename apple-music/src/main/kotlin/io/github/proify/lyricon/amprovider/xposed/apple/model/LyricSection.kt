package io.github.proify.lyricon.amprovider.xposed.apple.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricSection(
    override var agent: String? = null,
    override var begin: Int = 0,
    override var duration: Int = 0,
    override var end: Int = 0,
    var lines: MutableList<LyricLine> = mutableListOf()
) : LyricTiming