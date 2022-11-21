package pt.isec.swipe_maths.model.board

import android.util.Log
import pt.isec.swipe_maths.model.levels.Levels

class Board(level: Levels = Levels.Easy) {
    var lines : Array<Line> = arrayOf()
    var cols : MutableList<Column> = mutableListOf()
    var maxValue : Int = 0

    init {
        lines = arrayOf(Line(level), Line(level), Line(level))
        for(i in 0 until 3){
            cols.add(Column(
                lines[0].numbers[i],
                lines[1].numbers[i],
                lines[2].numbers[i],
                level
            ))
        }
        maxValue = maxOperation()
        println(printBoard())
    }

    fun printBoard() : String {
        var boardString : String = ""
        for(i in lines.indices){
            boardString += "Line $i: " + lines[i].printLine() + "\n"
        }
        for(i in cols.indices) {
            boardString += "Col $i: " + cols[i].printColumn() + "\n"
        }
        return boardString
    }

    private fun maxOperation() : Int {
        var localMaxValue = 0
        for(i in lines.indices){
           if(lines[i].lineValue > localMaxValue){
               localMaxValue = lines[i].lineValue
           }
        }

        for(i in cols.indices){
            if(cols[i].colValue > localMaxValue){
                localMaxValue = cols[i].colValue
            }
        }
        return localMaxValue
    }
}