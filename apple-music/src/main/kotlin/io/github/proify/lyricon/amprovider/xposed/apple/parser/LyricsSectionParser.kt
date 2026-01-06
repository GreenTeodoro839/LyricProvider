package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricLine
import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricSection

object LyricsSectionParser : Parser() {

    fun parserSectionVector(any: Any): MutableList<LyricSection> {
        val sections = mutableListOf<LyricSection>()
        val size = get(any, "size") as Long
        for (i in 0..<size) {
            val sectionPtr = get(any, "get", i) ?: continue
            val sectionNative = get(sectionPtr, "get") ?: continue
            sections.add(parserSectionNative(sectionNative))
        }
        return sections
    }

    fun parserSectionNative(any: Any): LyricSection {
        val section = LyricSection()
        LyricsTimingParser.parser(section, any)

        val lines = get(any, "getLines")
        lines?.let { section.lines = LyricsLineParser.parser(it) }
        return section
    }

    fun MutableList<LyricSection>.mergeLyrics(): MutableList<LyricLine> =
        this.flatMap { it.lines }.toMutableList()

}