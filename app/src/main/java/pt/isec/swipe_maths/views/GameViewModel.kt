package pt.isec.swipe_maths.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameBoard
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Line
import pt.isec.swipe_maths.model.levels.Levels
import pt.isec.swipe_maths.utils.NetUtils

class GameViewModel(var game: Game = Game()) : ViewModel() {

    class GameViewModelFactory(private var game: Game)
        : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(game) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

//    constructor(game: Game) : this() {
//        this.game = game
//    }
//
//    private var game : Game = Game()

    var changeGame : Game = game
        set(value) {
            field = value
            game = value
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
}