package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game() {
    var level : Levels = Levels.Easy
    var gameBoard: GameBoard = GameBoard(level.min!!, level.max!!, level.validOperations)

    init {
        gameBoard.initializeNumbers()
    }
}