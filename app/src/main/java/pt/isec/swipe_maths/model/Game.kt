package pt.isec.swipe_maths.model

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game {
    var level : Levels = Levels.Easy
    var board : Board = Board(level)

    var remainingTime : MutableLiveData<Int> = MutableLiveData()

    val timer = object: CountDownTimer((level.timer * 1000).toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            remainingTime.postValue((millisUntilFinished/1000).toInt())
            Log.i("Debug", "${remainingTime.value}")
        }

        override fun onFinish() {}
    }

    fun startTime(){
        timer.start()
        Log.i("Debug", "$remainingTime")
    }

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