package pt.isec.swipe_maths.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameBoard
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Line

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

    val board: LiveData<Board>
        get() {
            return game.boardLive
        }

    val timer : LiveData<Int>
        get(){
            return game.remainingTimeLive
        }
}