package pt.isec.swipe_maths.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import pt.isec.swipe_maths.IGameBoardFragment
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.views.GameViewModel

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment {

    lateinit var binding: ActivityGameScreenBinding

    private val game : Game = Game()

    private val viewModel : GameViewModel by viewModels {
        GameViewModel.GameViewModelFactory(game.board)
    }

    override fun getDefaultViewModelProviderFactory(): GameViewModel.GameViewModelFactory {
        return GameViewModel.GameViewModelFactory(game.board)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lblLevel.text = game.level.name

        binding.timer.text = viewModel.getLines()[0].numbers[0].toString()
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
//        Log.i("Debug", "max value: ${viewModel.getMaxValue()}")
        viewModel.updateBoard(Board())
        Log.i("Debug", "Col: $selectedColumn")
        return true
    }

    override fun swipeHorizontal(selectedRow: Int): Boolean {
        Log.i("Debug", "Row: $selectedRow")
        return true
    }
}