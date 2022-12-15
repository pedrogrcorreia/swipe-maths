package pt.isec.swipe_maths.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.databinding.FragmentLevelInfoBinding
import pt.isec.swipe_maths.views.GameViewModel

class LevelInfoFragment : Fragment() {

    lateinit var binding: FragmentLevelInfoBinding

    private val viewModel : GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLevelInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.timer.observe(viewLifecycleOwner){
            binding.timer.text = getString(R.string.timer, it)
            binding.timerPB.progress = it
        }

        viewModel.level.observe(viewLifecycleOwner){
            binding.lblLevel.text = getString(R.string.level, it)
            binding.correctAnswersPB.max = viewModel.level.value!!.correctAnswers
        }

        viewModel.correctAnswers.observe(viewLifecycleOwner){
            binding.correctAnswersPB.progress = it
            binding.nextLevelProgress.text = getString(R.string.levelProgress,
                it,
                viewModel.level.value!!.correctAnswers
            )
        }

        viewModel.state.observe(viewLifecycleOwner){
            when(it){
                GameStates.WAITING_FOR_LEVEL -> this.requireView().visibility = View.INVISIBLE
                GameStates.PLAYING -> this.requireView().visibility = View.VISIBLE
                GameStates.WAITING_FOR_START -> this.requireView().visibility = View.INVISIBLE
            }
        }

        binding.timer.setOnClickListener {
            Snackbar.make(binding.root, getString(R.string.timerInfo), Snackbar.LENGTH_SHORT).show()
        }

        binding.nextLevelProgress.setOnClickListener {
            Snackbar.make(binding.root, getString(R.string.levelProgressInfo), Snackbar.LENGTH_SHORT).show()
        }
    }
}