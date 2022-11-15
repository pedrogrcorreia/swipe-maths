package pt.isec.swipe_maths.model.board

import android.util.Log

class Column(firstNumber: Int, secondNumber: Int, thirdNumber: Int) {
    private var numbers : Array<Int> = arrayOf()
    private var operators : Array<String>


    init {
        numbers = arrayOf(firstNumber, secondNumber, thirdNumber)
    }

    init {
        operators = arrayOf("-", "+")
    }

    fun printColumn() : String {
        var lineString : String = ""
        for(i in numbers.indices){
            lineString += numbers[i].toString() + " "
        }
        return lineString
    }

    fun colValue() : Int {
        var result = numbers[0]
        for(i in operators.indices){
            result = Operations.calculate(result, operators[i], numbers[i+1])
        }
        return result
    }
}