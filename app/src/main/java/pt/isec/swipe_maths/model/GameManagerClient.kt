package pt.isec.swipe_maths.model

object GameManagerClient {
    var games : MutableList<Game> = mutableListOf()

    fun newPlayer(game: Game){
        games.add(game.apply {
            applyGameChanges(game)
        })
    }

    fun updatePlayer(game: Game){
        val newGame = games.find { it.player == game.player }.apply {
            this?.applyGameChanges(game)
        }
        if(newGame?.player?.uid == Player.mySelf.uid){
            GameManager.game = game.apply {
                applyGameChanges(game)
            }
        }
        GameManager.games.postValue(games)
    }

    fun finishGame(){
        games.clear()
    }
}