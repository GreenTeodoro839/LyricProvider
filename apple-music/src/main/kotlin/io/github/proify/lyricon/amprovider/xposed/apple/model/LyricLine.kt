package io.github.proify.lyricon.amprovider.xposed.apple.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricLine(
    override var agent: String? = null,
    override var begin: Int = 0,
    override var duration: Int = 0,
    override var end: Int = 0,
    var text: String? = null,
    var words: MutableList<LyricWord> = mutableListOf(),
    var backgroundWords: MutableList<LyricWord> = mutableListOf(),
    var backgroundText: String? = null,
) : LyricTiming