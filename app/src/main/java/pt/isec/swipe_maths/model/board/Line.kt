package pt.isec.swipe_maths.model.board

import android.util.Log
import kotlin.random.Random

class Line {
    private var numbers : Array<Int> = arrayOf()
    private var operators : Array<String>

    init{
        numbers = arrayOf(Random.nextInt(10), Random.nextInt(10), Random.nextInt(10))
        operators = arrayOf("+", "*")
    }

    fun printLine() : String {
        var lineString : String = ""
        Log.i("Debug", "${numbers.indices}")
        for(i in numbers.indices){
            lineString += numbers[i].toString() + " "
        }
        return lineString
    }

    fun lineValue() : Int {
        val firstNumber = numbers[0]
        val secondNumber = numbers[1]
        val thirdNumber = numbers[2]
        val firstOperator = operators[0]
        val secondOperator = operators[1]
        var result = 0
        result = Operations.calculate(firstNumber, firstOperator, secondNumber)
        Log.i("Debug", "result: $result")
        result = Operations.calculate(result, secondOperator, thirdNumber)
        return result
    }
}