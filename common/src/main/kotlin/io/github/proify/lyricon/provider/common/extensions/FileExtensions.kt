package io.github.proify.lyricon.provider.common.extensions

import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
//
//fun File.write(
//    data: CharSequence,
//    append: Boolean = false,
//    charset: Charset = StandardCharsets.UTF_8
//) {
//    parentFile?.mkdirs()
//    outputStream().bufferedWriter(charset).use { writer ->
//        if (append) {
//            writer.append(data)
//        } else {
//            writer.write(data.toString())
//        }
//    }
//}
//
//fun File.write(
//    data: ByteArray,
//    append: Boolean = false
//) {
//    parentFile?.mkdirs()
//    FileOutputStream(this, append).use { it.write(data) }
//}
//
//fun File.readAsString(
//    charset: Charset = StandardCharsets.UTF_8
//): String = inputStream().bufferedReader(charset).use { it.readText() }
//
//fun File.readAsBytes(): ByteArray = inputStream().use { it.readBytes() }