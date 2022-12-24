package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.network.Server

class GameOverFragment : Fragment() {

    var actBase : INewLevelFragment? = null

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

        Server.state.observe(viewLifecycleOwner){
            when(it){
                ConnectionStates.ALL_PLAYERS_FINISHED -> {
                    Server.startNewLevelTimers()
                    startTimer()
                }
                ConnectionStates.ALL_GAME_OVER -> {
                    println("EVERYONE LOST!!!!")
                }
            }
        }

        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    private fun startTimer(){
        timer = object: CountDownTimer(countTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countTime = millisUntilFinished
            }
            override fun onFinish() {
                context.let {
                    actBase!!.timesUp()
                }
            }
        }.start()
    }
}