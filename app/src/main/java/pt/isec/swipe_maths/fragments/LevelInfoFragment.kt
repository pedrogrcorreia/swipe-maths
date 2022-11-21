package pt.isec.swipe_maths.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import pt.isec.swipe_maths.R
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
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.timer.observe(viewLifecycleOwner){
            binding.timer.text = getString(R.string.timer, it)
        }

        viewModel.level.observe(viewLifecycleOwner){
            binding.lblLevel.text = getString(R.string.level, it)
        }

        viewModel.nextLevelProgress.observe(viewLifecycleOwner){
            binding.correctAnswers.text = getString(R.string.answers, it)
        }
    }
}