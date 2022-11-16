package pt.isec.swipe_maths.model.board

import pt.isec.swipe_maths.model.levels.Levels
import kotlin.random.Random

class Column(firstNumber: Int,
             secondNumber: Int,
             thirdNumber: Int,
             level: Levels = Levels.Easy) {
    var numbers : Array<Int> = arrayOf()
    var operators : Array<String>
    val colValue : Int


    init {
        numbers = arrayOf(firstNumber, secondNumber, thirdNumber)
        operators = arrayOf(level.validOperations[Random.nextInt(level.validOperations.size)],
            level.validOperations[Random.nextInt(level.validOperations.size)])
        colValue = colValue()
    }

    fun printColumn(): String {
        return "${numbers[0]} ${operators[0]} " +
                "${numbers[1]} ${operators[1]} ${numbers[2]} = $colValue"
    }

    private fun colValue() : Int {
        var result = numbers[0]
        for(i in operators.indices){
            result = Operations.calculate(result, operators[i], numbers[i+1])
        }
        return result
    }
}