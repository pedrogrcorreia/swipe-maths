package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.R
import kotlin.random.Random

class GameBoard(private val min : Int, private val  max : Int, private val operations : Array<String>) {
    val numbers = Array(5){IntArray(5)}

    var numbersArray = arrayOf(
        arrayOf(1, 2, 3, 4, 5),
        arrayOf(6, 7, 8, 9, 10),
        arrayOf(11, 12, 13, 14, 15),
        arrayOf(16, 17, 18, 19, 20),
        arrayOf(21, 22, 23, 24, 25),
    )

    var textArray : Array<Array<Int>> = arrayOf(
        arrayOf(R.id.sq00, R.id.sq01, R.id.sq02, R.id.sq03, R.id.sq04),
        arrayOf(R.id.sq10, R.id.sq11, R.id.sq12, R.id.sq13, R.id.sq14),
        arrayOf(R.id.sq20, R.id.sq21, R.id.sq22, R.id.sq23, R.id.sq24),
        arrayOf(R.id.sq30, R.id.sq31, R.id.sq32, R.id.sq33, R.id.sq34),
        arrayOf(R.id.sq40, R.id.sq41, R.id.sq42, R.id.sq43, R.id.sq44),
    )

    fun initializeNumbers(){
        for(i in numbers.indices){
            for(j in 0 until numbers[i].size){
                numbers[i][j] = Random.nextInt(min, max)
            }
        }
    }


}