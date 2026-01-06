package io.github.proify.lyricon.amprovider.xposed.apple.model

import kotlinx.serialization.Serializable

@Serializable
data class AppleSong(
    var name: String? = null,
    var artist: String? = null,
    var musicId: String? = null,
    var agents: MutableList<LyricAgent> = mutableListOf(),
    var duration: Int = 0,
    var lyrics: MutableList<LyricLine> = mutableListOf()
)