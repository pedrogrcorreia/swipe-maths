package pt.isec.swipe_maths

import org.junit.Test

import org.junit.Assert.*
import pt.isec.swipe_maths.utils.FirestoreUtils

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun getData(){
        FirestoreUtils.highscoresSinglePlayer()
    }
}