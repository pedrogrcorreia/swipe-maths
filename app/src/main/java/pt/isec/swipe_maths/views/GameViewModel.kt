package pt.isec.swipe_maths.views

import android.app.Application
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

class GameViewModel() : ViewModel() {

    var game : Game? = null
    private var initialize : Boolean = false

    private val _gameBoard : MutableLiveData<GameBoard> by lazy {
        MutableLiveData<GameBoard>().apply {
            value = game?.gameBoard
        }
    }

    val gameBoard : LiveData<GameBoard>
        get() = _gameBoard

    fun initializeViewModel(game: Game){
        if(!initialize){
            initialize = true
            this.game = game
        }
    }

    fun changeValue(){
        _gameBoard.value = game?.gameBoard?.randomNumbers()
    }

    fun getMaxValue() : Int? {
        return _gameBoard.value?.maxValue()
    }
}