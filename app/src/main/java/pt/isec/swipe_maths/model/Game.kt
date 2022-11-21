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
    var level : MutableLiveData<Levels> = MutableLiveData(Levels.Easy)

    var board : MutableLiveData<Board> = MutableLiveData(level.value?.let { Board(it) })

    var remainingTime : MutableLiveData<Int> = MutableLiveData(level.value!!.timer)

    var correctAnswers : MutableLiveData<Int> = MutableLiveData(0)

    var gameState : MutableLiveData<GameStates> = MutableLiveData(GameStates.WAITING_FOR_START)

    var timer: CountDownTimer? = null

    private fun startTimer(){
        println(remainingTime.value!!)
        timer = object: CountDownTimer((remainingTime.value!! * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime.postValue((millisUntilFinished/1000).toInt())
            }
            override fun onFinish() {}
        }.start()
    }

    fun startTime(){
        startTimer()
    }

    fun isCorrectLine(line: Int): Boolean{
        if (board.value?.lines?.get(line)?.lineValue == board.value?.maxValue) {
            correctPlay()
            return true
        }
        nextBoard()
        return false
    }

    fun isCorrectColumn(col: Int): Boolean{
        if(board.value?.cols?.get(col)?.colValue == board.value?.maxValue) {
            correctPlay()
            return true
        }
        nextBoard()
        return false
    }

    private fun addTime(){
        if(remainingTime.value!! > level.value!!.timer - level.value!!.bonusTime){
            remainingTime.value = (level.value!!.timer)
        } else {
            remainingTime.value = (remainingTime.value!! + level.value!!.bonusTime)
        }
    }

    private fun correctPlay(){
        correctAnswers.value = correctAnswers.value!! + 1
        if(correctAnswers.value!! == level.value!!.correctAnswers){
            gameState.value = GameStates.WAITING_FOR_LEVEL
            timer!!.cancel()
            println("correctAnswers: $correctAnswers")
        }
        else {
            addTime()
            timer!!.cancel()
            startTimer()
            nextBoard()
        }
    }

    private fun nextBoard(){
        board.value = Board(level.value!!)
    }

    fun newLevel(){
        correctAnswers.value = 0
        level.value = level.value!!.nextLevel
        remainingTime.value = level.value!!.timer
        nextBoard()
        gameState.value = GameStates.PLAYING
    }
}