package pt.isec.swipe_maths.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentNewLevelBinding
import pt.isec.swipe_maths.views.GameViewModel

interface INewLevelFragment{
    fun timesUp()
}

class NewLevelFragment : Fragment() {

    var actBase : INewLevelFragment? = null

    lateinit var binding: FragmentNewLevelBinding

    private val viewModel : GameViewModel by activityViewModels()

    private var countTime : Long = 5000

    private var timer : CountDownTimer? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actBase = context as? INewLevelFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextLevelLabel.text = getString(R.string.nextLevel, viewModel.level.value)

        startTimer()

        binding.btnPause.setOnClickListener {
            timer!!.cancel()
            AlertDialog.Builder(this.requireContext())
                .setTitle("Pause")
                .setMessage("Game is paused.")
                .setPositiveButton("Resume"){ _: DialogInterface, _: Int ->
                    startTimer()
                }
                .show()
        }
    }

    private fun startTimer(){
        timer = object: CountDownTimer(countTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countTime = millisUntilFinished
                binding.nextLevelTimer.text = getString(R.string.nextLevelTimer, millisUntilFinished/1000)
            }
            override fun onFinish() {
                context.let {
                    actBase!!.timesUp()
                    findNavController().navigate(R.id.action_newLevelFragment_to_gameBoardFragment)
                }
            }
        }.start()
    }
}