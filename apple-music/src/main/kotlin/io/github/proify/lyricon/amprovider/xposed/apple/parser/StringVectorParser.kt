package io.github.proify.lyricon.amprovider.xposed.apple.parser

object StringVectorParser : Parser() {

    fun parserStringVectorNative(any: Any): MutableList<String> {
        val size = get(any, "size") as Long
        return (0 until size.toInt()).map { i ->
            get(any, "get", i) as String
        }.toMutableList()
    }

}