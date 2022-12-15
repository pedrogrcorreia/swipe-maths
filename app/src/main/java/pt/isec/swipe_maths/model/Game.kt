package pt.isec.swipe_maths.model

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.InstanceCreator
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.ILevels
import pt.isec.swipe_maths.model.levels.Levels
import java.lang.reflect.Type

class Game() : InstanceCreator<Game> {
    @Transient
    var level : MutableLiveData<Levels> = MutableLiveData(Levels.Easy)

    var levelData : Levels = Levels.Easy
        set(value){
            field = value
            level.postValue(value)
        }

    @Transient
    var board : MutableLiveData<Board> = MutableLiveData(level.value?.let { Board(it) })

    var boardData : Board = Board()
        set(value) {
            field = value
            board.postValue(value)
        }

    @Transient
    var remainingTime : MutableLiveData<Int> = MutableLiveData(level.value!!.timer)

    var remainingTimeData : Int = level.value!!.timer
        set(value) {
            field = value
            remainingTime.postValue(value)
        }

    @Transient
    var correctAnswers : MutableLiveData<Int> = MutableLiveData(0)

    var correctAnswersData : Int = 0
        set(value) {
            field = value
            correctAnswers.postValue(value)
        }

    @Transient
    var gameState : MutableLiveData<GameStates> = MutableLiveData(GameStates.WAITING_FOR_START)
        set(value){
            field = value
            gameStateData = value.value!!
        }

    var gameStateData : GameStates = GameStates.WAITING_FOR_START
        set(value) {
            field = value
            gameState.postValue(value)
        }

    @Transient
    var nextLevelProgress : MutableLiveData<Int> = MutableLiveData(level.value?.correctAnswers)

    var nextLevelProgressData : Int = level.value!!.correctAnswers
        set(value) {
            field = value
            nextLevelProgress.postValue(value)
        }

    @Transient
    var points : MutableLiveData<Int> = MutableLiveData(0)

    var pointsData : Int = 0
        set(value) {
            field = value
            points.postValue(value)
        }

    @Transient
    private var timer: CountDownTimer? = null

    var totalTime : Int = 0

    private fun startTimer(){
        println(remainingTime.value!!)
        timer = object: CountDownTimer((remainingTime.value!! * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                totalTime++
                remainingTime.postValue((millisUntilFinished/1000).toInt())
            }
            override fun onFinish() {
                gameOver()
            }
        }.start()
    }

    init {
        level.postValue(levelData)
        board.postValue(boardData)
        remainingTime.postValue(remainingTimeData)
        gameState.postValue(gameStateData)
        nextLevelProgress.postValue(nextLevelProgressData)
        correctAnswers.postValue(correctAnswersData)
        points.postValue(pointsData)
    }

    fun startTime(){
//        gameState.value = GameStates.PLAYING
        gameStateData = GameStates.PLAYING
        startTimer()
    }

    fun isCorrectLine(line: Int): Boolean{
        if (board.value?.lines?.get(line)?.lineValue == board.value?.maxValue) {
            correctPlay()
            return true
        } else if(board.value?.lines?.get(line)?.lineValue == board.value?.secMaxValue){
            points.postValue(points.value!! + 1)
        }
        nextBoard()
        return false
    }

    fun isCorrectColumn(col: Int): Boolean{
        if(board.value?.cols?.get(col)?.colValue == board.value?.maxValue) {
            correctPlay()
            return true
        } else if(board.value?.cols?.get(col)?.colValue == board.value?.secMaxValue){
            points.value = points.value!! + 1
        }
        nextBoard()
        return false
    }

    private fun addTime(){
        if(remainingTime.value!! > level.value!!.timer - level.value!!.bonusTime){
            remainingTime.postValue(level.value!!.timer)
        } else {
            remainingTime.postValue(remainingTime.value!! + level.value!!.bonusTime)
        }
    }

    private fun correctPlay(){
        points.postValue(points.value!! + 2)
        correctAnswers.postValue(correctAnswers.value!! + 1)
        nextLevelProgress.postValue(level.value!!.correctAnswers - correctAnswers.value!!)
        if(correctAnswers.value!! == level.value!!.correctAnswers){
            gameState.postValue(GameStates.WAITING_FOR_LEVEL)
            timer!!.cancel()
            println("totalTime: $totalTime")
        }
        else {
            addTime()
            timer!!.cancel()
            startTimer()
            nextBoard()
        }
    }

    private fun nextBoard(){
        boardData = Board(level.value!!)
    }

    fun newLevel(){
        level.value = level.value!!.nextLevel
        correctAnswers.value = 0
        nextLevelProgress.value = level.value!!.correctAnswers
        remainingTime.value = level.value!!.timer
        nextBoard()
        gameState.value = GameStates.PLAYING
    }

    fun gameOver(){
        gameState.value = GameStates.GAME_OVER
    }

    override fun toString(): String {
        return board.value?.printBoard() + levelData.toString() + gameStateData.toString() + timer.toString()
    }

    override fun createInstance(type: Type?): Game {
        return Game()
    }
}