package io.github.proify.lyricon.provider.common

import io.github.proify.lyricon.provider.common.extensions.decodeBase64
import io.github.proify.lyricon.provider.common.extensions.encodeBase64
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        println("5555555".encodeBase64<String>().decodeBase64())
    }
}