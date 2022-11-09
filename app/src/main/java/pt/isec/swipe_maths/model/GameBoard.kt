package pt.isec.swipe_maths.model

import android.util.Log
import pt.isec.swipe_maths.R
import kotlin.random.Random

class GameBoard(private val min : Int, private val  max : Int, private val operations : Array<String>) {
    var numbers = Array(3){IntArray(3)}
    var verticalOperations = Array(3){Array<String>(2){""} }
    var horizontalOperations = Array(3){Array<String>(2){""} }

    var maxValue = 0

    fun initializeNumbers(): GameBoard{
//        for(i in numbers.indices){
//            for(j in 0 until numbers[i].size){
//                numbers[i][j] = Random.nextInt(min, max)
//            }
//        }
        numbers = arrayOf(intArrayOf(1,2,3), intArrayOf(4,5,6), intArrayOf(7,8,9))
        verticalOperations = arrayOf(arrayOf("+", "*"), arrayOf("-", "/"), arrayOf("-", "+"))
        horizontalOperations = arrayOf(arrayOf("+", "+"), arrayOf("+", "+"), arrayOf("+", "+"))
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

    fun maxValue() : Int {
        horizontalCalculations()
        return maxValue
    }

    private fun horizontalCalculations(){
        calculateLine(numbers[0], horizontalOperations[0])
    }

    private fun calculateLine(numbers: IntArray, operations: Array<String>){
        var result = numbers[0]
        maxValue = 0
        Log.i("Debug", "numbers: ${numbers[0]}")
        for(i in operations.indices){
            when(operations[i]){
                "+" -> {
                    result += numbers[i+1]
                    if(result > maxValue){
                        maxValue = result
                    }
                }
                "-" -> {
                    result += numbers[i+1]
                    if(result > maxValue){
                        maxValue = result
                    }
                }
                "*" -> {
                    result += numbers[i+1]
                    if(result > maxValue){
                        maxValue = result
                    }
                }
                "/" -> {
                    result += numbers[i+1]
                    if(result > maxValue){
                        maxValue = result
                    }
                }
            }
        }
    }
}