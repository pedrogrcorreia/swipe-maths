package pt.isec.swipe_maths

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import kotlin.random.Random

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment {

    lateinit var binding: ActivityGameScreenBinding

    lateinit var gameScreen : GameScreen

    private val gameBoard = GameBoard()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)




        for(i in 0 .. 4){
            for(j in 0 .. 4){
                val text = findViewById<TextView>(gameBoard.textArray[i][j])
                text.text = gameBoard.numbersArray[i][j].toString() + "  "
            }
        }
//        binding.lblLevel
//        binding.sq00.measure(0, 0)
//        val width = binding.sq00.measuredWidth
//        val height = binding.sq00.measuredHeight
//
//        binding.sq22.measure(0, 0)
//        Log.i("Debug", "${binding.sq22.measuredWidth}")
//
//        GameScreen.setValues(width, height)
//        gameScreen = GameScreen(this)
//
//        binding.frBoard.addView(gameScreen)
//        binding.frBoard.setOnClickListener{
//            Log.i("Debug", "user touch!!!")
//        }
    }

    override fun test(): Boolean {
        var col = Random.nextInt(4)
        var row = Random.nextInt(4)
        var number = Random.nextInt(200)
        gameBoard.numbersArray[row][col] = number
        val text = findViewById<TextView>(gameBoard.textArray[row][col])
        text.text = number.toString()
        Log.i("Debug", "Worked!")
        return true
    }
}