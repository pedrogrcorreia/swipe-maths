package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.databinding.FragmentStartGameBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.views.GameViewModel

class StartGameFragment : Fragment() {

    lateinit var binding: FragmentStartGameBinding

    private val viewModel by lazy{
        ViewModelProvider(requireActivity())[GameViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        viewModel.state.observe(viewLifecycleOwner){
            when(it){
                GameStates.PLAYING -> {
                    val currentDestinationIsStart = this.findNavController().currentDestination == this.findNavController().findDestination(R.id.startGameFragment)
                    val currentDestinationIsBoard = this.findNavController().currentDestination == this.findNavController().findDestination(R.id.gameBoardFragment)

                    if(currentDestinationIsStart && !currentDestinationIsBoard){
                        findNavController().navigate(R.id.action_startGameFragment_to_gameBoardFragment)
                    }
                }
            }
        }
        binding.btnStartGame.setOnClickListener{
            findNavController().navigate(R.id.action_startGameFragment_to_gameBoardFragment)
            viewModel.startGame()
        }
    }
}