package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricLine

object LyricsLineParser : Parser() {

    fun parser(any: Any): MutableList<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val size = get(any, "size") as? Long ?: 0
        for (i in 0..<size) {
            val ptr = get(any, "get", i) ?: continue
            val lineNative = get(ptr, "get") ?: continue
            lines.add(parserLyricsLineNative(lineNative))
        }
        return lines
    }

    fun parserLyricsLineNative(o: Any): LyricLine {
        val line = LyricLine()
        LyricsTimingParser.parser(line, o)

        val backgroundWords = get(o, "getBackgroundWords", false)
        backgroundWords?.let { line.backgroundWords = LyricsWordParser.parser(it) }
        line.backgroundText = get(o, "getHtmlBackgroundVocalsLineText") as? String

        line.text = get(o, "getHtmlLineText") as? String
        val words = get(o, "getWords")
        words?.let { line.words = LyricsWordParser.parser(it) }
        return line
    }

}