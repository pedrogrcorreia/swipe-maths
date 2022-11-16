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

class GameViewModel(private var board: Board) : ViewModel() {

    class GameViewModelFactory(private var board: Board)
        : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(board) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

        private val _gameBoard : MutableLiveData<Board> by lazy {
        MutableLiveData<Board>().apply {
            value = board
        }
    }

    val boardData : LiveData<Board>
        get() {
            return _gameBoard
        }

    fun getLines() : Array<Line> {
        return board.lines
    }

    fun updateBoard(board: Board){
        Log.i("Debug", board.printBoard())
        _gameBoard.value = board
    }

//    var game : Game? = null
//    private var initialize : Boolean = false
//
//    private val _gameBoard : MutableLiveData<GameBoard> by lazy {
//        MutableLiveData<GameBoard>().apply {
//            value = game?.gameBoard
//        }
//    }
//
//    val gameBoard : LiveData<GameBoard>
//        get() = _gameBoard
//
//    fun initializeViewModel(game: Game){
//        if(!initialize){
//            initialize = true
//            this.game = game
//        }
//    }
//
//    fun changeValue(){
//        _gameBoard.value = game?.gameBoard?.randomNumbers()
//    }
//
//    fun getMaxValue() : Int? {
//        return _gameBoard.value?.maxValue()
//    }
}