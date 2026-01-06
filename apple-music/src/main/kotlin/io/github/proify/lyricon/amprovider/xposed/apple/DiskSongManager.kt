package io.github.proify.lyricon.amprovider.xposed.apple

import android.content.Context
import io.github.proify.lyricon.amprovider.xposed.apple.model.AppleSong
import io.github.proify.lyricon.provider.common.extensions.deflate
import io.github.proify.lyricon.provider.common.extensions.inflate
import kotlinx.serialization.json.Json
import java.io.File

object DiskSongManager {
    private var baseDir: File? = null

    val json = Json {
        ignoreUnknownKeys = true
    }

    fun init(context: Context) {
        baseDir = File(context.filesDir, "lyricon/songs")
    }

    fun save(song: AppleSong): Boolean {
        val id = song.musicId
        if (id.isNullOrBlank()) return false
        val string = json.encodeToString(song)
        return runCatching {
            getFile(id).also {
                it.parentFile?.mkdirs()
            }.writeBytes(string.deflate())
        }.isSuccess
    }

    fun load(id: String): AppleSong? {
        return runCatching {
            getFile(id).takeIf { it.exists() }
                ?.readBytes()
                ?.inflate()
                ?.let {
                    return json.decodeFromString<AppleSong>(it)
                }
        }.getOrNull()
    }

    fun hasCache(id: String): Boolean {
        return getFile(id).exists()
    }

    private fun getFile(id: String): File = File(baseDir, "$id.json.gz")
}