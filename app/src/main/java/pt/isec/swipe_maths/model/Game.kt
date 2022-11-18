package pt.isec.swipe_maths.model

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels

class Game {
    var level : Levels = Levels.Easy

    var levelLive : MutableLiveData<Levels> = MutableLiveData(Levels.Easy)

    var boardLive : MutableLiveData<Board> = MutableLiveData(Board(level))

    var board : Board = boardLive.value!!

    var remainingTimeLive : MutableLiveData<Int> = MutableLiveData(level.timer)

    var remainingTime : Int = remainingTimeLive.value!!

    var correctAnswersLive : MutableLiveData<Int> = MutableLiveData(0)

    var correctAnswers : Int = correctAnswersLive.value!!

    var timer: CountDownTimer? = null

    var gameState = GameStates.WAITING_FOR_START

    private fun startTimer(){
        timer = object: CountDownTimer((remainingTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeLive.postValue((millisUntilFinished/1000).toInt())
            }
            override fun onFinish() {}
        }.start()
    }

    fun startTime(){
        startTimer()
    }

    fun isCorrectLine(line: Int): Boolean{
        if (board.lines[line].lineValue == board.maxValue) {
            correctPlay()
            return true
        }
        return false
    }

    fun isCorrectColumn(col: Int): Boolean{
        if(board.cols[col].colValue == board.maxValue) {
            correctPlay()
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

    private fun correctPlay(){
        correctAnswers = correctAnswersLive.value!!
        correctAnswersLive.postValue(++correctAnswers)
        Log.i("Debug", "correctAnswers: $correctAnswers")
        if(correctAnswers == level.correctAnswers){
            gameState = GameStates.WAITING_FOR_LEVEL
            level = level.nextLevel
            levelLive.postValue(level)
            correctAnswersLive.postValue(0)
        }
        nextBoard()
    }

    private fun nextBoard(){
        board = Board(level)
        boardLive.postValue(board)
        Log.i("Debug", board.printBoard())
        timer?.cancel()
        addTime()
        startTimer()
    }
}