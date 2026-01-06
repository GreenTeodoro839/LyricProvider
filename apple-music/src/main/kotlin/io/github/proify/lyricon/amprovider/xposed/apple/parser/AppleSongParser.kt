package io.github.proify.lyricon.amprovider.xposed.apple.parser

import io.github.proify.lyricon.amprovider.xposed.apple.MediaMetadataStore
import io.github.proify.lyricon.amprovider.xposed.apple.model.AppleSong
import io.github.proify.lyricon.amprovider.xposed.apple.parser.LyricsSectionParser.mergeLyrics

object AppleSongParser : Parser() {

    fun parser(o: Any): AppleSong {
        return AppleSong().apply {
            musicId = get(o, "getAdamId").toString()
            get(o, "getAgents")?.let { agents = LyricsAgentParser.parserAgentVector(it) }
            duration = get(o, "getDuration") as? Int ?: 0

            // language = get(o, "getLanguage") as? String
            // lyricsId = get(o, "getLyricsId") as? String
            // queueId = get(o, "getQueueId") as? Long ?: 0L

            val sections = get(o, "getSections")
            if (sections != null) {
                lyrics = LyricsSectionParser.parserSectionVector(sections).mergeLyrics()
            }

            // timing = get(o, "getTiming") as? Long ?: 0L
            // timingName = get(o, "getAvailableTiming")?.name()
            // translation = get(o, "getTranslation") as? String
            // translationLanguages = StringVectorParser.parserStringVectorNative(get(o, "getTranslationLanguages"))


            musicId?.let {
                MediaMetadataStore.getMetadataById(it)?.let { it1 ->
                    name = it1.title
                    artist = it1.artist
                }
            }

        }
    }

}