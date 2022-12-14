package pt.isec.swipe_maths

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameSerializer
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Column
import pt.isec.swipe_maths.model.board.Line


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
    fun testGame(){
        val game = Game()

        val gson = GsonBuilder()
//            .registerTypeAdapter(Game::class.java, GameSerializer())
            .create()

        val json = gson.toJson(game, Game::class.java)


        val gameFromJson = gson.fromJson(json, Game::class.java)
//        gameFromJson.boards()
        println(json)
        println("game board " + game.board.value?.printBoard())
        println("json game board " + gameFromJson.board.value?.printBoard())
        println("game level " + game.level.value)
        println("json game level " + gameFromJson.level.value )
        println("game board " + game.boardData?.printBoard())
        println("json game board " + gameFromJson.boardData?.printBoard())
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