package pt.isec.swipe_maths.model.board

import android.util.Log
import pt.isec.swipe_maths.model.levels.Levels
import kotlin.random.Random

class Line(level: Levels = Levels.Easy) {
    var numbers : Array<Int> = arrayOf()
    private var operators : Array<String>

    init{
        numbers = arrayOf(Random.nextInt(level.min, level.max),
            Random.nextInt(level.min, level.max),
            Random.nextInt(level.min, level.max))
        operators = arrayOf(level.validOperations[Random.nextInt(level.validOperations.size)],
            level.validOperations[Random.nextInt(level.validOperations.size)]
            )
    }

    fun printLine() : String {
        var lineString : String = ""
//        for(i in numbers.indices){
//            lineString += numbers[i].toString() + " "
//        }
        lineString = "${numbers[0]} ${operators[0]} " +
                "${numbers[1]} ${operators[1]} ${numbers[2]}"
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