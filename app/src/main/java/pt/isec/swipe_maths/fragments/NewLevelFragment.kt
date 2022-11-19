package pt.isec.swipe_maths.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentNewLevelBinding
import pt.isec.swipe_maths.views.GameViewModel

class NewLevelFragment : Fragment() {

    lateinit var binding: FragmentNewLevelBinding

    private val viewModel : GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var countTime : Int = 5000

    val timer: CountDownTimer = object: CountDownTimer(countTime.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            countTime = millisUntilFinished.toInt()
            binding.nextLevelTimer.text = getString(R.string.nextLevelTimer, millisUntilFinished/1000)
        }
        override fun onFinish() {
            findNavController().navigate(R.id.action_newLevelFragment_to_gameBoardFragment)
        }
    }.start()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewLevelBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextLevelLabel.text = getString(R.string.nextLevel, viewModel.level)

        binding.btnPause.setOnClickListener {
            timer.cancel()
        }
    }
}