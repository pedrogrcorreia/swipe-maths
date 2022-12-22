package pt.isec.swipe_maths.model

import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.network.Requests
import pt.isec.swipe_maths.network.Server

object GameManagerServer {

    var games : MutableList<Game> = mutableListOf(GameManager.game)
    var boardsList = mutableListOf<Board>()

    fun addNewPlayer(player: Player){
        games.add(Game(player))
        games.last().apply {
            this.boardData = boardsList.last()
        }
    }

    fun newBoard(board: Board){
        boardsList.add(board)
    }

    fun rowPlay(row: Int, player: Player): Boolean{
        val playerGame = games.find { it.player == player } ?: return false
        var result : Boolean
        playerGame.apply { plays++ }
        if(playerGame.plays == boardsList.size){
            newBoard(Board(GameManager.game.levelData))
            result = playerGame.isCorrectLine(row, true, boardsList.last())
        } else{
            result = playerGame.isCorrectLine(row, true, boardsList[playerGame.plays])
        }
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        Server.updateViews(json)
        verifyLevelFinish()
        return result
    }

    fun colPlay(col: Int, player: Player): Boolean{
        val playerGame = games.find { it.player == player } ?: return false
        var result : Boolean
        playerGame.apply { plays++ }
        if(playerGame.plays == boardsList.size){
            newBoard(Board(GameManager.game.levelData))
            result = playerGame.isCorrectColumn(col, true, boardsList.last())
        } else{
            result = playerGame.isCorrectColumn(col, true, boardsList[playerGame.plays])
        }
        val json = JSONObject().apply {
            put("request", Requests.UPDATE_VIEWS)
        }
        Server.updateViews(json)
        verifyLevelFinish()
        return result
    }


    fun rowPlayServer(row: Int, player: Player): Boolean{
        var result: Boolean
        GameManager.game.apply {
            plays++
        }
        println("Server played a row!")
        if(GameManager.game.plays == boardsList.size){
            newBoard(Board(GameManager.game.levelData))
            result = GameManager.game.isCorrectLine(row, true, boardsList.last())
        }else{
            result = GameManager.game.isCorrectLine(row, true, boardsList[GameManager.game.plays])
        }
        verifyLevelFinish()
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
        verifyLevelFinish()
        return result
    }

    // TODO add five points to first
    fun verifyLevelFinish(){
        GameManager.games.postValue(games)
        for(game in games){
            println("${game.player.name} ${game.gameStateData}")
            if(game.gameStateData != GameStates.WAITING_FOR_LEVEL){
                return
            }
        }
        Server.state.postValue(ConnectionStates.ALL_PLAYERS_FINISHED)
    }

    fun resetNewLevelBoards(){
        for(game in games){
            game.boardData = boardsList.last()
        }
    }

    fun watchTimers(){
        for(game in games){
            game.remainingTime.observeForever{
                if(game.player != Player.mySelf) {
                    Server.updateTime()
                    GameManager.games.postValue(games)
                }
            }
        }
    }
}