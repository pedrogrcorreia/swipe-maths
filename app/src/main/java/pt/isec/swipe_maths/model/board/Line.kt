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
        var result = numbers[0]
        for(i in operators.indices){
            result = Operations.calculate(result, operators[i], numbers[i+1])
        }
        return result
    }
}