package io.github.proify.lyricon.amprovider.xposed.apple

import io.github.proify.lyricon.amprovider.xposed.apple.model.AppleSong
import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricAgent
import io.github.proify.lyricon.lyric.model.DoubleLyricLine
import io.github.proify.lyricon.lyric.model.LyricWord
import io.github.proify.lyricon.lyric.model.Song
import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricLine as AppleLyricLine
import io.github.proify.lyricon.amprovider.xposed.apple.model.LyricWord as AppleLyricWord

fun AppleSong.toSong(): Song = AppleSongMapper.map(this)

object AppleSongMapper {

    fun map(song: AppleSong): Song {
        return Song(
            id = song.musicId,
            name = song.name,
            artist = song.artist,
            duration = song.duration,
            lyrics = convertLyrics(song.lyrics, song.agents)
        )
    }

    private fun convertLyrics(
        appleLyrics: List<AppleLyricLine>,
        agents: List<LyricAgent>
    ): MutableList<DoubleLyricLine> {
        val agentDirectionMap = computeAgentDirections(agents)

        return appleLyrics.map { appleLine ->
            DoubleLyricLine().apply {
                text = appleLine.text
                words = appleLine.words.map { it.toLyricWord() }.toMutableList()

                secondaryText = appleLine.backgroundText
                secondaryWords = appleLine.backgroundWords.map { it.toLyricWord() }.toMutableList()

                begin = appleLine.begin
                end = appleLine.end
                duration = appleLine.duration

                val directionType = agentDirectionMap[appleLine.agent]

                isAlignedRight = directionType == LyricDirection.RIGHT

            }
        }.toMutableList()
    }

    private fun AppleLyricWord.toLyricWord(): LyricWord = LyricWord(
        text = this.text,
        begin = this.begin,
        duration = this.duration,
        end = this.end
    )

    /**
     * 计算 Agent ID 与 歌词方向的映射关系。
     * 逻辑：找到前两个类型为 PERSON 的 Agent。第一个为左(默认)，第二个为右。
     */
    private fun computeAgentDirections(agents: List<LyricAgent>?): Map<String, LyricDirection> {
        if (agents.isNullOrEmpty()) return emptyMap()

        val personAgents = agents.filter {
            LyricAgent.getType(it.type) == LyricAgent.Type.PERSON
        }

        // 如果少于2人，不需要区分左右，全部默认即可
        if (personAgents.size < 2) return emptyMap()

        val leftAgentId = personAgents[0].id
        val rightAgentId = personAgents[1].id

        val map = HashMap<String, LyricDirection>()

        if (leftAgentId != null) {
            map[leftAgentId] = LyricDirection.DEFAULT
        }
        if (rightAgentId != null) {
            map[rightAgentId] = LyricDirection.RIGHT
        }

        return map
    }

    private enum class LyricDirection {
        DEFAULT, RIGHT
    }
}