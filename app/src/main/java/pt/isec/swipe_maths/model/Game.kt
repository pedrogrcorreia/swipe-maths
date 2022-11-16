package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game() {
    var level : Levels = Levels.Easy
    var board : Board = Board(level)
}