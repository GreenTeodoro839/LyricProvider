package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricWord

object LyricsWordParser : Parser() {

    fun parser(any: Any): MutableList<LyricWord> {
        val words = mutableListOf<LyricWord>()
        val size = get(any, "size") as? Long ?: 0
        for (i in 0..<size) {
            val ptr: Any = get(any, "get", i) ?: continue
            val wordNative = get(ptr, "get") ?: continue
            words.add(parserWordNative(wordNative))
        }
        return words
    }

    fun parserWordNative(o: Any): LyricWord {
        val word = LyricWord()
        LyricsTimingParser.parser(word, o)
        word.text = get(o, "getHtmlLineText") as? String
        return word
    }

}