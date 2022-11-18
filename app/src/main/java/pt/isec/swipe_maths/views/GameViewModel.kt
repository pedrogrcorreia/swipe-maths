package pt.isec.swipe_maths.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameBoard
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Line
import pt.isec.swipe_maths.model.levels.Levels

class GameViewModel(private var game: Game) : ViewModel() {

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
            return game.boardLive
        }

    val timer : LiveData<Int>
        get(){
            return game.remainingTimeLive
        }

    val level : LiveData<Levels>
        get(){
            return game.levelLive
        }

    val correctAnswers : LiveData<Int>
        get(){
            return game.correctAnswersLive
        }
}