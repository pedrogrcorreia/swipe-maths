package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game {
    var level : Levels = Levels.Easy
    var board : Board = Board(level)

    fun isCorrectLine(line: Int): Boolean{
        if (board.lines[line].lineValue == board.maxValue)
            return true
        return false
    }

    fun isCorrectColumn(col: Int): Boolean{
        if(board.cols[col].colValue == board.maxValue)
            return true
        return false
    }

    fun nextBoard(){
        board = Board(level)
    }
}