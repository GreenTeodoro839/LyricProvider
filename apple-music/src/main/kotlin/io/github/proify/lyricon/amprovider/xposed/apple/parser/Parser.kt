package io.github.proify.lyricon.amprovider.xposed.apple.parser

import de.robv.android.xposed.XposedHelpers

open class Parser {

    fun get(any: Any, name: String, vararg args: Any?): Any? =
        runCatching {
            XposedHelpers.callMethod(any, name, *args)
        }.getOrNull()

}