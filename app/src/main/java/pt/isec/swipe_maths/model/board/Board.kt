package pt.isec.swipe_maths.model.board

import pt.isec.swipe_maths.model.levels.Levels

class Board(level: Levels = Levels.Easy) {
    var lines : Array<Line> = arrayOf()
    var cols : MutableList<Column> = mutableListOf()

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
}