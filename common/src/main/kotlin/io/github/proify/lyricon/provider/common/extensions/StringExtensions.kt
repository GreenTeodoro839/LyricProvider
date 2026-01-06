package io.github.proify.lyricon.provider.common.extensions

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.io.encoding.Base64

fun String.deflate(): ByteArray {
    val defeater = Deflater()
    defeater.setInput(toByteArray(Charsets.UTF_8))
    defeater.finish()

    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)

    while (!defeater.finished()) {
        val count = defeater.deflate(buffer)
        outputStream.write(buffer, 0, count)
    }
    return outputStream.toByteArray()
}

fun ByteArray.inflate(): String {
    val inflater = Inflater()
    inflater.setInput(this)

    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)

    while (!inflater.finished()) {
        val count = inflater.inflate(buffer)
        outputStream.write(buffer, 0, count)
    }
    return outputStream.toString(Charsets.UTF_8)
}

inline fun <reified T : Any> String.base64Encode(): T {
    return when (T::class) {
        String::class -> Base64.encode(this.toByteArray()) as T
        ByteArray::class -> Base64.encodeToByteArray(this.toByteArray()) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

inline fun <reified T : Any> String.base64Decode(): T {
    return when (T::class) {
        String::class -> Base64.decode(this.toByteArray()).toString(Charsets.UTF_8) as T
        ByteArray::class -> Base64.decode(this.toByteArray()) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

inline fun <reified T : Any> ByteArray.base64Encode(): T {
    return when (T::class) {
        String::class -> Base64.encode(this) as T
        ByteArray::class -> Base64.encodeToByteArray(this) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}

inline fun <reified T : Any> ByteArray.base64Decode(): T {
    return when (T::class) {
        String::class -> Base64.decode(this).toString(Charsets.UTF_8) as T
        ByteArray::class -> Base64.decode(this) as T
        else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
    }
}