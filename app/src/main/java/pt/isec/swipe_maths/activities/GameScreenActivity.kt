package pt.isec.swipe_maths.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import pt.isec.swipe_maths.IGameBoardFragment
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.views.GameViewModel
import kotlin.random.Random

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment {

    lateinit var binding: ActivityGameScreenBinding

    private val game : Game = Game()

    private val viewModel : GameViewModel by viewModels()

    private val gameBoard = game.gameBoard
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        for(i in 0 .. 4){
//            for(j in 0 .. 4){
//                val text = findViewById<TextView>(gameBoard.textArray[i][j])
//                text.text = gameBoard.numbers[i][j].toString()
//            }
//        }
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
        Log.i("Debug", "max value: ${gameBoard.maxValue()}")
        Log.i("Debug", "Col: $selectedColumn")
        return true
    }

    override fun swipeHorizontal(selectedRow: Int): Boolean {
        Log.i("Debug", "Row: $selectedRow")
        return true
    }
}