package pt.isec.swipe_maths

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding

class GameScreenActivity : AppCompatActivity() {

    lateinit var binding: ActivityGameScreenBinding

    lateinit var gameScreen : GameScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.debugText.measure(0, 0)
        val width = binding.debugText.measuredWidth
        val height = binding.debugText.measuredHeight

        GameScreen.setValues(width, height)
        gameScreen = GameScreen(this)


        binding.frBoard.addView(gameScreen)
    }
}