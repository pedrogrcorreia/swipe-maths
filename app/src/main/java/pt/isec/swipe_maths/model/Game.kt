package pt.isec.swipe_maths.model

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.levels.Levels

class Game(var player: Player = Player.mySelf) {
    @Transient
    var level: MutableLiveData<Levels> = MutableLiveData(Levels.Easy)

    var levelData: Levels = Levels.Easy
        set(value) {
            field = value
            level.postValue(value)
        }

    @Transient
    var board: MutableLiveData<Board> = MutableLiveData(level.value?.let { Board(it) })

    var boardData: Board = Board()
        set(value) {
            field = value
            board.postValue(value)
        }

    @Transient
    var remainingTime: MutableLiveData<Int> = MutableLiveData(level.value!!.timer)

    var remainingTimeData: Int = level.value!!.timer
        set(value) {
            field = value
            remainingTime.postValue(value)
        }

    @Transient
    var correctAnswers: MutableLiveData<Int> = MutableLiveData(0)

    var correctAnswersData: Int = 0
        set(value) {
            field = value
            correctAnswers.postValue(value)
        }

    @Transient
    var gameState: MutableLiveData<GameStates> = MutableLiveData(GameStates.WAITING_FOR_START)
        set(value) {
            field = value
            gameStateData = value.value!!
        }

    var gameStateData: GameStates = GameStates.WAITING_FOR_START
        set(value) {
            field = value
            gameState.postValue(value)
        }

    @Transient
    var nextLevelProgress: MutableLiveData<Int> = MutableLiveData(level.value?.correctAnswers)

    var nextLevelProgressData: Int = level.value!!.correctAnswers
        set(value) {
            field = value
            nextLevelProgress.postValue(value)
        }

    @Transient
    var points: MutableLiveData<Int> = MutableLiveData(0)

    var pointsData: Int = 0
        set(value) {
            field = value
            points.postValue(value)
        }

    @Transient
    private var timer: CountDownTimer? = null

    var totalTime: Int = 0

    var plays: Int = 0

    private fun startTimer() {
        Handler(Looper.getMainLooper()).post {
            timer = object : CountDownTimer((remainingTimeData * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    totalTime++
                    remainingTimeData = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    gameOver()
                }
            }.start()
        }
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

    fun startTime() {
        gameStateData = GameStates.PLAYING
        startTimer()
    }

    fun isCorrectLine(
        line: Int,
        fromServer: Boolean = false,
        nextBoard: Board = Board(levelData)
    ): Boolean {
        if (boardData.lines[line].lineValue == boardData.maxValue) {
            if (!fromServer) {
                correctPlay()
            } else {
                correctPlay()
            }
            return true
        } else if (boardData.lines[line].lineValue == boardData.secMaxValue) {
            pointsData = (pointsData + 1)
        }
        nextBoard(nextBoard)
        return false
    }

    fun isCorrectColumn(
        col: Int,
        fromServer: Boolean = false,
        nextBoard: Board = Board(levelData)
    ): Boolean {
        if (boardData.cols[col].colValue == boardData.maxValue) {
            if (!fromServer) {
                correctPlay()
            } else {
                correctPlay()
            }
            nextBoard(nextBoard)
            return true
        } else if (boardData.cols[col].colValue == boardData.secMaxValue) {
            pointsData = (pointsData + 1)
        }
        nextBoard(nextBoard)
        return false
    }

    private fun addTime() {
        if (remainingTimeData > levelData.timer - levelData.bonusTime) {
            remainingTimeData = (levelData.timer)
        } else {
            remainingTimeData = (remainingTimeData + levelData.bonusTime)
        }
    }

    private fun correctPlay(nextBoard: Board = Board(levelData)) {
        pointsData = points.value!! + 2
        correctAnswersData = (correctAnswers.value!! + 1)
        nextLevelProgressData = (level.value!!.correctAnswers - correctAnswers.value!!)
        if (correctAnswersData == levelData.correctAnswers) {
            gameStateData = GameStates.WAITING_FOR_LEVEL
            timer!!.cancel()
            println("totalTime: $totalTime")
        } else {
            addTime()
            timer!!.cancel()
            startTimer()
            nextBoard(nextBoard)
        }
    }


    private fun nextBoard(board: Board) {
        boardData = board
    }

    fun newLevel() {
        levelData = levelData.nextLevel
        correctAnswersData = 0
        nextLevelProgressData = levelData.correctAnswers
        remainingTimeData = levelData.timer
        nextBoard(Board(levelData))
        gameStateData = GameStates.PLAYING
    }

    fun gameOver() {
        gameStateData = GameStates.GAME_OVER
    }

    override fun toString(): String {
        var string = ""
        string += "Board: ${board.value?.printBoard()} BoardData: ${boardData.printBoard()}\n "
        string += "GameState: ${gameState.value!!} GameStateData: $gameStateData\n"
        string += "Level: ${level.value}\n LevelData: $levelData"
        string += "Points: ${points.value}\n PointsData: $pointsData"
        string += "Remaining Time: ${remainingTime.value} Remaining Time Data: $remainingTimeData\n"
        string += "Correct answers: ${correctAnswers.value} Correct Answers Data: $correctAnswersData\n"
        string += "Player: $player"
        return string
    }

    fun applyGameChanges(game: Game){
        board.postValue(game.boardData)
        gameState.postValue(game.gameStateData)
        level.postValue(game.levelData)
        remainingTime.postValue(game.remainingTimeData)
        nextLevelProgress.postValue(game.nextLevelProgressData)
        points.postValue(game.pointsData)
        correctAnswers.postValue(game.correctAnswersData)
    }
}