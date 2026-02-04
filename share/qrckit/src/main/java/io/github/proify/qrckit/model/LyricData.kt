/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.qrckit.model

import io.github.proify.lrckit.LrcParser
import io.github.proify.lrckit.model.LrcData
import io.github.proify.lrckit.model.LrcLine
import io.github.proify.qrckit.QrcParser
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * 歌词数据模型类，支持逐字歌词 (QRC) 与普通歌词 (LRC) 的解析。
 * 能够自动关联翻译和罗马音数据，并提供对时间戳微小偏差的容错处理。
 *
 * @property lyricsRaw 原始歌词文本 (支持 QRC 格式或标准 LRC 格式)
 * @property translationRaw 原始翻译文本 (LRC 格式)
 * @property romaRaw 原始罗马音文本 (LRC 格式)
 */
@Serializable
data class LyricData(
    val lyricsRaw: String? = null,
    val translationRaw: String? = null,
    val romaRaw: String? = null
) {
    /**
     * 核心业务属性：解析并合并后的富文本歌词行列表。
     * 采用 [lazy] 延迟加载，仅在首次访问时执行复杂的解析与匹配逻辑。
     */
    val richLyricLines: List<RichLyricLine> by lazy {
        if (lyricsRaw.isNullOrBlank()) return@lazy emptyList()

        // 1. 预构建索引 Map：将翻译和罗马音按开始时间索引，以将匹配复杂度降低至 O(1)
        val transMap = lrcTranslationData.lines.associateBy { it.start }
        val romaMap = lrcRomaData.lines.associateBy { it.start }

        // 2. 解析逻辑：优先尝试解析 QRC (支持逐字显示)，失败或为空则降级为普通 LRC
        val qrcLines =
            runCatching { QrcParser.parseXML(lyricsRaw).firstOrNull()?.lines }.getOrNull()

        if (!qrcLines.isNullOrEmpty()) {
            qrcLines.map { it.toRichLyric(transMap, romaMap) }
        } else {
            lrcData.lines.map { it.toRichLyric(transMap, romaMap) }
        }
    }

    // --- 内部解析属性 (私有懒加载) ---

    private val lrcData: LrcData by lazy {
        if (lyricsRaw.isNullOrBlank()) LrcData() else LrcParser.parseLrc(lyricsRaw)
    }

    private val lrcTranslationData: LrcData by lazy {
        if (translationRaw.isNullOrBlank()) LrcData() else LrcParser.parseLrc(translationRaw)
    }

    private val lrcRomaData: LrcData by lazy {
        if (romaRaw.isNullOrBlank()) LrcData() else LrcParser.parseLrc(romaRaw)
    }

    // --- 转换扩展函数 ---

    /** 将 [LyricLine] (QRC 格式行) 转换为带有关联数据的 [RichLyricLine] */
    private fun LyricLine.toRichLyric(transMap: Map<Long, LrcLine>, romaMap: Map<Long, LrcLine>) =
        RichLyricLine(
            start = start,
            end = end,
            duration = duration,
            text = text,
            translation = findMatchedText(start, transMap),
            roma = findMatchedText(start, romaMap),
            words = words
        )

    /** 将 [LrcLine] (标准 LRC 格式行) 转换为带有关联数据的 [RichLyricLine] */
    private fun LrcLine.toRichLyric(transMap: Map<Long, LrcLine>, romaMap: Map<Long, LrcLine>) =
        RichLyricLine(
            start = start,
            end = end,
            duration = duration,
            text = text,
            translation = findMatchedText(start, transMap),
            roma = findMatchedText(start, romaMap)
        )

    /**
     * 在辅助歌词 Map 中寻找与当前行匹配的文本。
     *
     * 匹配算法：
     * 1. **精确匹配**：首先尝试 O(1) 的哈希查找。
     * 2. **模糊匹配**：若失败，则在 100ms 的误差范围内搜索首个符合条件的行（解决不同解析器对毫秒进位处理不一致的问题）。
     *
     * @param startTime 目标行的开始时间戳 (ms)
     * @param map 待搜索的索引表
     * @return 匹配到的文本，未找到则返回 null
     */
    private fun findMatchedText(startTime: Long, map: Map<Long, LrcLine>): String? {
        if (map.isEmpty()) return null

        // 优先精确匹配
        map[startTime]?.let { return it.text }

        // 处理时间戳偏移（例如 1230ms 匹配到 1235ms）
        return map.values.firstOrNull { abs(it.start - startTime) < 100 }?.text
    }
}