package pt.isec.swipe_maths.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.Levels
import pt.isec.swipe_maths.network.Client

class GameViewModel(var game: Game = GameManager.game) : ViewModel() {

    class GameViewModelFactory(private var game: Game)
        : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(game) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    val state: LiveData<GameStates>
        get() {
            return game.gameState
        }

    val board: LiveData<Board>
        get() {
            return game.board
        }

    val timer : LiveData<Int>
        get(){
            return game.remainingTime
        }

    val level : LiveData<Levels>
        get(){
            return game.level
        }

    val correctAnswers : LiveData<Int>
        get(){
            return game.correctAnswers
        }

    val nextLevelProgress : LiveData<Int>
        get(){
            return game.nextLevelProgress
        }

    val points : LiveData<Int>
        get(){
            return game.points
        }

    val totalTime : Int
        get() = game.totalTime

    fun startGame(){
        game.startTime()
    }

    fun linePlay(selectedLine: Int): Boolean = game.isCorrectLine(selectedLine)

    fun columnPlay(selectedCol: Int): Boolean = game.isCorrectColumn(selectedCol)

    fun nextLevelTimerUp(){
        game.newLevel()
    }

    fun updateGame(newGame: Game) {
        game.apply {
            board.postValue(newGame.boardData)
            gameState.postValue(newGame.gameStateData)
            level.postValue(newGame.levelData)
            remainingTime.postValue(newGame.remainingTimeData)
            nextLevelProgress.postValue(newGame.nextLevelProgressData)
            points.postValue(newGame.pointsData)
            correctAnswers.postValue(newGame.correctAnswersData)
        }
    }
}