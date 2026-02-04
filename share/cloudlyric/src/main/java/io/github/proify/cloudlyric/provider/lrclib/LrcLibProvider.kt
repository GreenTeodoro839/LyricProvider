/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.cloudlyric.provider.lrclib

import io.github.proify.cloudlyric.LyricsProvider
import io.github.proify.cloudlyric.LyricsResult
import io.github.proify.cloudlyric.toRichLines
import io.github.proify.lrckit.LrcParser
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class LrcLibProvider : LyricsProvider {
    companion object {
        const val ID = "LrcLib"

        private const val USER_AGENT: String =
            "CloudLyric (https://github.com/proify/LyricProvider)"

        private const val BASE_URL = "https://lrclib.net/api"
    }

    override val id: String = ID

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun search(
        query: String?,
        trackName: String?,
        artistName: String?,
        albumName: String?,
        limit: Int
    ): List<LyricsResult> {
        val queryParams = mutableMapOf<String, String>()
        query?.let { queryParams["q"] = it }
        trackName?.let { queryParams["track_name"] = it }
        artistName?.let { queryParams["artist_name"] = it }
        albumName?.let { queryParams["album_name"] = it }

        if (queryParams["q"] == null && queryParams["track_name"] == null) return emptyList()

        val responseBody =
            sendRawRequest("$BASE_URL/search?${encodeParams(queryParams)}") ?: return emptyList()
        return try {
            val response = json.decodeFromString<List<LrcLibResponse>>(responseBody)
            return response
                .sortedByDescending { calculateIntegrityScore(it) }
                .take(limit)
                .map {
                    //println( it)
                    LyricsResult(
                        trackName = it.trackName,
                        artistName = it.artistName,
                        albumName = it.albumName,
                        rich = LrcParser.parseLrc(it.syncedLyrics).lines.toRichLines(),
                        instrumental = it.instrumental,
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun sendRawRequest(url: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", USER_AGENT)
            .GET()
            .build()
        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun encodeParams(params: Map<String, String>): String {
        return params.map {
            "${URLEncoder.encode(it.key, StandardCharsets.UTF_8)}=${
                URLEncoder.encode(
                    it.value,
                    StandardCharsets.UTF_8
                )
            }"
        }.joinToString("&")
    }

    private fun calculateIntegrityScore(response: LrcLibResponse): Int {
        var score = 0
        if (response.trackName.isNullOrBlank().not()) score += 20
        if (response.artistName.isNullOrBlank().not()) score += 20
        if (response.albumName.isNullOrBlank().not()) score += 10
        if (response.syncedLyrics.isNullOrBlank().not()) score += 50
        return score
    }
}