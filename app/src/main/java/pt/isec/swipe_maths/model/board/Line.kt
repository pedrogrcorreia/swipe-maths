package pt.isec.swipe_maths.model.board

import com.google.gson.*
import pt.isec.swipe_maths.model.levels.Levels
import java.lang.reflect.Type
import kotlin.random.Random

class Line(level: Levels = Levels.Easy) {
    companion object{
        private val random : Random = Random(System.currentTimeMillis())
    }
    var numbers : Array<Int> = arrayOf()
    var operators : Array<String>
    val lineValue : Int

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
        return Operations.calculateExpression(numbers, operators)
    }
}