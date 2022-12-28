package pt.isec.swipe_maths.model

import androidx.lifecycle.MutableLiveData
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.network.Server

object GameManager {
    var game = Game()

    var games : MutableLiveData<MutableList<Game>> = MutableLiveData(mutableListOf(game))

    fun newGame(){
        game = Game()
    }
}