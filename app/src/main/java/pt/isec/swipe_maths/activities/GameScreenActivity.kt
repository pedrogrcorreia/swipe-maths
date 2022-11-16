package pt.isec.swipe_maths.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import com.google.android.material.snackbar.Snackbar
import pt.isec.swipe_maths.fragments.IGameBoardFragment
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.views.GameViewModel

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment {

    private lateinit var binding: ActivityGameScreenBinding

    private val game : Game = Game()

    private val viewModel : GameViewModel by viewModels {
        GameViewModel.GameViewModelFactory(game)
    }

    override fun getDefaultViewModelProviderFactory(): GameViewModel.GameViewModelFactory {
        return GameViewModel.GameViewModelFactory(game)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.lblLevel.text = getString(R.string.level, game.level)

        game.startTime()
        viewModel.timer.observe(this){
            binding.timer.text = getString(R.string.timer, it)
        }

        viewModel.level.observe(this){
            binding.lblLevel.text = getString(R.string.level, it)
        }

        viewModel.correctAnswers.observe(this){
            binding.lblAnswers.text = getString(R.string.answers, it)
        }

        Log.i("Debug", game.board.printBoard())
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
//        Log.i("Debug", "max value: ${viewModel.getMaxValue()}")
        if(game.isCorrectColumn(selectedColumn)){
            Snackbar.make(binding.root, getString(R.string.correct_col, selectedColumn), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.GREEN)
            }.show()
        } else {
            Snackbar.make(binding.root, getString(R.string.incorrect_col, selectedColumn), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.RED)
            }.show()
        }

        return true
    }

    override fun swipeHorizontal(selectedRow: Int): Boolean {
        if(game.isCorrectLine(selectedRow)){
            Snackbar.make(binding.root, getString(R.string.correct_row, selectedRow), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.GREEN)
            }.show()
        } else{
            Snackbar.make(binding.root, getString(R.string.incorrect_row, selectedRow), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.RED)
            }.show()
        }
        return true
    }
}