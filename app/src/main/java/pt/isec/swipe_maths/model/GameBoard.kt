package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.R
import kotlin.random.Random

class GameBoard(private val min : Int, private val  max : Int, private val operations : Array<String>) {
    var numbers = Array(3){IntArray(3)}

    var verticalOperations = Array(3){Array<String>(2){""} }
    var horizontalOperations = Array(3){Array<String>(2){""} }

    fun initializeNumbers(): GameBoard{
//        for(i in numbers.indices){
//            for(j in 0 until numbers[i].size){
//                numbers[i][j] = Random.nextInt(min, max)
//            }
//        }
        numbers = arrayOf(intArrayOf(1,2,3), intArrayOf(4,5,6), intArrayOf(7,8,9))
        verticalOperations = arrayOf(arrayOf("+", "*"), arrayOf("-", "/"), arrayOf("-", "+"))
        horizontalOperations = arrayOf(arrayOf("-", "+"), arrayOf("/", "*"), arrayOf("/", "/"))
        return this
    }

    fun randomNumbers() : GameBoard{
        for(i in numbers.indices){
            for(j in 0 until numbers[i].size){
                numbers[i][j] = Random.nextInt(min, max)
            }
        }
        return this
    }

    fun getVerticalOperation(row: Int, col:Int) : String{
        return verticalOperations[row][col]
    }

    fun getHorizontalOperation(row: Int, col: Int) : String = horizontalOperations[row][col]

    fun getNumber(row: Int, col: Int) : String = numbers[row][col].toString()
}