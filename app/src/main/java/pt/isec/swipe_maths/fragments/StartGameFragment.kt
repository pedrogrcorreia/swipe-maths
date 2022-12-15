package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.databinding.FragmentStartGameBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.views.GameViewModel

interface GamePass {
    fun onGamePass(data: Game) : Game
}

class StartGameFragment : Fragment() {

    lateinit var binding: FragmentStartGameBinding

    lateinit var viewModel : GameViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: GameScreenActivity = requireActivity() as GameScreenActivity
        val game: Game = activity.getGame()
        val viewModelFactory = GameViewModel.GameViewModelFactory(game)
        viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        println("STATE ON FRAGMENT!" + viewModel.state.value)

        when(viewModel.state.value){
            GameStates.PLAYING -> findNavController().navigate(R.id.action_startGameFragment_to_gameBoardFragment)
        }

        binding.btnStartGame.setOnClickListener{
            findNavController().navigate(R.id.action_startGameFragment_to_gameBoardFragment)
            viewModel.startGame()
        }
    }
}