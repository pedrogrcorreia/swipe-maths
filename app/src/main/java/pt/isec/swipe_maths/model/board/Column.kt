package pt.isec.swipe_maths.model.board

import pt.isec.swipe_maths.model.levels.Levels
import kotlin.random.Random

class Column(firstNumber: Int,
             secondNumber: Int,
             thirdNumber: Int,
             level: Levels = Levels.Easy) {

    companion object{
        private val random : Random = Random(System.currentTimeMillis())
    }
    
    var numbers : Array<Int> = arrayOf()
    var operators : Array<String>
    val colValue : Int

    init {
        numbers = arrayOf(firstNumber, secondNumber, thirdNumber)
        operators = arrayOf(level.validOperations[random.nextInt(level.validOperations.size)],
            level.validOperations[random.nextInt(level.validOperations.size)])
        colValue = colValue()
    }

    fun printColumn(): String {
        return "${numbers[0]} ${operators[0]} " +
                "${numbers[1]} ${operators[1]} ${numbers[2]} = $colValue"
    }

    private fun colValue() : Int {
        return Operations.calculateExpression(numbers, operators)
    }
}