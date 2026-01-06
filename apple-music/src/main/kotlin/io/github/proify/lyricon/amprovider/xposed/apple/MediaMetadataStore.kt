package io.github.proify.lyricon.amprovider.xposed.apple

import android.media.MediaMetadata

object MediaMetadataStore {
    private val metadataCache = object : LinkedHashMap<String, Metadata>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Metadata>?): Boolean {
            return size > 100
        }
    }

    fun addMetadata(metadata: MediaMetadata): Metadata? {
        val mediaId: String = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) ?: return null
        val title: String = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist: String = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val duration: Long = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

        val newMetadata = Metadata(mediaId, title, artist, duration)
        metadataCache[mediaId] = newMetadata
        return newMetadata
    }

    fun getMetadataById(mediaId: String): Metadata? = metadataCache[mediaId]

    data class Metadata(
        val id: String,
        val title: String?,
        val artist: String?,
        val duration: Long = 0
    )
}