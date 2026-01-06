package io.github.proify.lyricon.amprovider.xposed.apple.model

interface LyricTiming {
    var agent: String?
    var begin: Int
    var duration: Int
    var end: Int
}