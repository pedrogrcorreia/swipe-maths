package pt.isec.swipe_maths.model

import pt.isec.swipe_maths.model.board.Board

object GameManager {
    var game = Game()

    var games : MutableMap<Player, Game> = mutableMapOf(Pair(Player.mySelf, game))

    var boardsList = mutableListOf<Board>()

    fun addNewPlayer(player: Player){
        games[player] = Game().apply {
            board.postValue(game.boardData)
            gameState.postValue(game.gameStateData)
            level.postValue(game.levelData)
            remainingTime.postValue(game.remainingTimeData)
            nextLevelProgress.postValue(game.nextLevelProgressData)
            points.postValue(game.pointsData)
            correctAnswers.postValue(game.correctAnswersData)
        }
    }

    fun newBoard(board: Board){
        boardsList.add(board)
    }

    fun rowPlay(row: Int, player: Player): Boolean{
        var result = false
        games[player]!!.apply { 
            plays++
        }
        if(games[player]!!.plays == boardsList.size){
            newBoard(Board(game.levelData))
            result = games[player]!!.isCorrectLine(row, true, boardsList.last())
        } else{
            result = games[player]!!.isCorrectLine(row, true, boardsList[games[player]!!.plays])
        }
        return result
    }
    fun rowPlayServer(row: Int, player: Player): Boolean{
        var result: Boolean
        game.apply {
            plays++
        }
        println("Board list size: ${boardsList.size}")
        println("Game plays: ${game.plays}")
        if(game.plays == boardsList.size){
            newBoard(Board(game.levelData))
            result = game.isCorrectLine(row, true, boardsList.last())
        }else{
            result = game.isCorrectLine(row, true, boardsList[game.plays])
        }
//        println(boardsList.last().printBoard())
        return result
    }
}