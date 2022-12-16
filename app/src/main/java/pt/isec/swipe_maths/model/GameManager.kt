package pt.isec.swipe_maths.model

object GameManager {
    var game = Game()

    var games : MutableMap<Player, Game> = mutableMapOf(Pair(Player.mySelf, game))

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
        println(games)
    }
}