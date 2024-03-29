package pt.isec.swipe_maths.model

import android.app.Activity
import androidx.lifecycle.Observer
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.network.Requests
import pt.isec.swipe_maths.network.Server

object GameManagerServer {

    var games : MutableList<Game> = mutableListOf(GameManager.game)
    var boardsList = mutableListOf<Board>()

    var currentLevel = GameManager.game.levelData

    private var levelFinished = false

    fun addNewPlayer(player: Player){
        games.add(Game(player))
        games.last().apply {
            this.boardData = boardsList.last()
        }
    }

    fun removePlayer(playerName: String?){
        val playerGame = games.find { it.player.name == playerName }
        games.remove(playerGame)
    }

    fun newBoard(board: Board){
        boardsList.add(board)
    }

    fun rowPlay(row: Int, player: Player): Boolean{
        val playerGame = games.find { it.player.uid == player.uid } ?: return false
        var result : Boolean
        playerGame.apply { plays++ }
        if(playerGame.plays == boardsList.size){
            newBoard(Board(playerGame.levelData))
            result = playerGame.isCorrectLine(row, true, boardsList.last())
        } else{
            result = playerGame.isCorrectLine(row, true, boardsList[playerGame.plays])
        }
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        Server.updateViews(json)
        return result
    }

    fun colPlay(col: Int, player: Player): Boolean{
        val playerGame = games.find { it.player.uid == player.uid } ?: return false
        var result : Boolean
        playerGame.apply { plays++ }
        if(playerGame.plays == boardsList.size){
            newBoard(Board(playerGame.levelData))
            result = playerGame.isCorrectColumn(col, true, boardsList.last())
        } else{
            result = playerGame.isCorrectColumn(col, true, boardsList[playerGame.plays])
        }
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        Server.updateViews(json)
        return result
    }


    fun rowPlayServer(row: Int, player: Player): Boolean{
        var result: Boolean
        GameManager.game.apply {
            plays++
        }
        if(GameManager.game.plays == boardsList.size){
            newBoard(Board(GameManager.game.levelData))
            result = GameManager.game.isCorrectLine(row, true, boardsList.last())
        }else{
            result = GameManager.game.isCorrectLine(row, true, boardsList[GameManager.game.plays])
        }
        return result
    }

    fun colPlayServer(col: Int, player: Player): Boolean{
        var result: Boolean
        GameManager.game.apply {
            plays++
        }
        if(GameManager.game.plays == boardsList.size){
            newBoard(Board(GameManager.game.levelData))
            result = GameManager.game.isCorrectColumn(col, true, boardsList.last())
        }else{
            result = GameManager.game.isCorrectColumn(col, true, boardsList[GameManager.game.plays])
        }
        return result
    }

    private fun verifyLevelFinish() : Boolean {
        var over = true
        GameManager.games.postValue(games)
        for(game in games){
            if(game.gameStateData == GameStates.WAITING_FOR_LEVEL){
                if(!levelFinished){
                    game.apply {
                        pointsData += 5
                    }
                    levelFinished = true
                }
            }
            if(game.gameStateData == GameStates.GAME_OVER){
                continue
            }
            if(game.gameStateData != GameStates.WAITING_FOR_LEVEL){
                over = false
            }
            currentLevel = game.levelData
        }
//        Server.state.postValue(ConnectionStates.ALL_PLAYERS_FINISHED)
//        verifyGameOver()
        return over
    }

    private fun verifyGameOver() : Boolean {
        GameManager.games.postValue(games)
        for(game in games){
            if(game.gameStateData != GameStates.GAME_OVER){
                return false
            }
        }
//        Server.state.postValue(ConnectionStates.ALL_GAME_OVER)
        return true
    }

    fun resetNewLevelBoards(){
        for(game in games){
            game.boardData = boardsList.last()
        }
        levelFinished = false
    }

    val timeObserver: Observer<Int> = Observer {
        Server.updateTime()
        GameManager.games.postValue(games)
    }



    val gameStateObserver : Observer<GameStates> = Observer {
        if(it == GameStates.WAITING_FOR_LEVEL){
            if(verifyLevelFinish()){
                if(!verifyGameOver()){
                    Server.levelFinished()
                }
            }
        }
        if(it == GameStates.GAME_OVER){
            if(verifyGameOver()){
                Server.gameOver()
            } else {
                if(verifyLevelFinish()){
                    Server.levelFinished()
                }
            }
        }
    }

    fun watchTimers(){
        for(game in games){
            game.remainingTime.observeForever(timeObserver)

            game.gameState.observeForever(gameStateObserver)
        }
    }

    fun removeObservers(){
        for(game in games){
            game.remainingTime.removeObserver(timeObserver)
            game.gameState.removeObserver(gameStateObserver)
        }
    }

    fun finishGame(){
        games.clear()
        GameManager.newGame()
        games = mutableListOf(GameManager.game)
        GameManager.games.postValue(games)
        boardsList.clear()
        currentLevel = GameManager.game.levelData
    }
}