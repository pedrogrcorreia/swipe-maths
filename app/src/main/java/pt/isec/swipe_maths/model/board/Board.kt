package pt.isec.swipe_maths.model.board

class Board(maxValue: Int) {
    var lines : Array<Line> = arrayOf()
    var cols : MutableList<Column> = mutableListOf()

    init {
        lines = arrayOf(Line(maxValue), Line(maxValue), Line(maxValue))
        for(i in 0 until 3){
            cols.add(Column(
                lines[0].numbers[i],
                lines[1].numbers[i],
                lines[2].numbers[i]
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