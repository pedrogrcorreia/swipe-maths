package pt.isec.swipe_maths.model.board

import android.util.Log
import kotlin.random.Random

class Line(maxValue: Int = 10) {
    var numbers : Array<Int> = arrayOf()
    private var operators : Array<String>

    init{
        numbers = arrayOf(Random.nextInt(maxValue), Random.nextInt(maxValue), Random.nextInt(maxValue))
        operators = arrayOf("+", "*")
    }

    fun printLine() : String {
        var lineString : String = ""
        for(i in numbers.indices){
            lineString += numbers[i].toString() + " "
        }
        return lineString
    }

    fun lineValue() : Int {
        var result = numbers[0]
        for(i in operators.indices){
            result = Operations.calculate(result, operators[i], numbers[i+1])
        }
        return result
    }
}