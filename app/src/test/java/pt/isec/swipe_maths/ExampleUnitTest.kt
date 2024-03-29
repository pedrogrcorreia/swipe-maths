package pt.isec.swipe_maths

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Column
import pt.isec.swipe_maths.model.board.Line
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.network.Requests
import pt.isec.swipe_maths.network.Server


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun gameTime(){
        val json = JSONObject().apply{
            put("request", Requests.ROW_PLAY)
            put("rowNumber", 2)
            put("game", Client.gson.toJson(GameManager.game, Game::class.java))
        }
        val game = Server.gson.fromJson(json.getString("game"), Game::class.java).apply{
            board.postValue(boardData)
            gameState.postValue(gameStateData)
            level.postValue(levelData)
            remainingTime.postValue(55)
            nextLevelProgress.postValue(nextLevelProgressData)
            points.postValue(pointsData)
        }
        println(game.remainingTimeData)
    }

    @Test
    fun testGame(){
        val game = Game()

        val gson = GsonBuilder()
//            .registerTypeAdapter(Game::class.java, GameSerializer())
            .create()

        game.gameStateData = GameStates.PLAYING

        println(game.gameState.value)
        println(game.gameStateData)

        val json = gson.toJson(game, Game::class.java)


        val gameFromJson = gson.fromJson(json, Game::class.java).apply {
            board.value = boardData
        }


//        gameFromJson.boards()
        println("sent game $game")
        println("json game $gameFromJson")
    }

    @Test
    fun testBoard(){
        val board = Board()

        val gson = GsonBuilder()
            .create()

        val json = gson.toJson(board, Board::class.java)

        val boardFromJson = gson.fromJson(json, Board::class.java)

        println(json)
        println("Board " + board.printBoard())
        println("Board Json: " + boardFromJson.printBoard())
    }

    @Test
    fun testLine(){
        val line = Line()

        val gson = GsonBuilder()
            .create()

        val json = gson.toJson(line, Line::class.java)


        val lineFromJson = gson.fromJson(json, Line::class.java)
        println(json)
        println("line: " + line.printLine())
        println("json line: ${lineFromJson.printLine()}")
    }

    @Test
    fun testCol(){
        val col = Column(1, 2, 3)

        val gson = Gson()

        val json = gson.toJson(col, Column::class.java)

        val colFromJson = gson.fromJson(json, Column::class.java)

        println(json)
        println("col: " + col.printColumn())
        println("json col: " + colFromJson.printColumn())
    }
}