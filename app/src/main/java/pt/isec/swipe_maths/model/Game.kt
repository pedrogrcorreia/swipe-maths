package pt.isec.swipe_maths.model

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game {
    var level : Levels = Levels.Easy

    var boardLive : MutableLiveData<Board> = MutableLiveData(Board(level))

    val board : Board
        get(){
            return boardLive.value!!
        }

    var remainingTimeLive : MutableLiveData<Int> = MutableLiveData(level.timer)

    var remainingTime : Int = remainingTimeLive.value!!

    var correctAnswers : MutableLiveData<Int> = MutableLiveData(0)

    var timer: CountDownTimer? = null

    private fun startTimer(){
        timer = object: CountDownTimer((remainingTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeLive.postValue((millisUntilFinished/1000).toInt())
//            Log.i("Debug", "${remainingTime.value}")
            }
            override fun onFinish() {}
        }.start()
    }


    fun startTime(){
        startTimer()
        Log.i("Debug", "$remainingTime")
    }

    fun isCorrectLine(line: Int): Boolean{
        if (board.lines[line].lineValue == board.maxValue) {
            boardLive.postValue(Board())
            timer?.cancel()
            addTime()
            startTimer()
            return true
        }
        return false
    }

    fun isCorrectColumn(col: Int): Boolean{
        if(board.cols[col].colValue == board.maxValue) {
            boardLive.postValue(Board())
            timer?.cancel()
            addTime()
            startTimer()
            return true
        }
        return false
    }

    private fun addTime(){
        remainingTime = remainingTimeLive.value!!
        if(remainingTime > level.timer - level.bonusTime){
            remainingTime = level.timer
            remainingTimeLive.postValue(remainingTime)
        } else {
            remainingTime += level.bonusTime
            remainingTimeLive.postValue(remainingTime)
        }
        Log.i("Debug", "$remainingTime")
    }

}