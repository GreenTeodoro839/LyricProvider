package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricTiming

object LyricsTimingParser : Parser() {

    fun parser(timing: LyricTiming, any: Any) {
        timing.agent = get(any, "getAgent") as? String
        timing.begin = get(any, "getBegin") as? Int ?: 0
        timing.end = get(any, "getEnd") as? Int ?: 0
        timing.duration = get(any, "getDuration") as? Int ?: 0
    }

}