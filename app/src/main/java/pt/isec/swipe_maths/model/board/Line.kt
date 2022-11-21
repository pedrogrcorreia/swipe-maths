package pt.isec.swipe_maths.model.board

import pt.isec.swipe_maths.model.levels.Levels
import kotlin.random.Random

class Line(level: Levels = Levels.Easy) {
    var numbers : Array<Int> = arrayOf()
    var operators : Array<String>
    val lineValue : Int

    private val random : Random = Random(System.currentTimeMillis())

    init{
        numbers = arrayOf(random.nextInt(level.min, level.max),
            random.nextInt(level.min, level.max),
            random.nextInt(level.min, level.max))
        operators = arrayOf(level.validOperations[random.nextInt(level.validOperations.size)],
            level.validOperations[random.nextInt(level.validOperations.size)]
            )
        lineValue = lineValue()
    }

    fun printLine(): String {
        return "${numbers[0]} ${operators[0]} " +
                "${numbers[1]} ${operators[1]} ${numbers[2]} = $lineValue"
    }

    private fun lineValue() : Int {
//        var result = numbers[0]
//        for(i in operators.indices){
//            result = Operations.calculate(result, operators[i], numbers[i+1])
//        }
        return Operations.calculateExpression(numbers, operators)
    }
}