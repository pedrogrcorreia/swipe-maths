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
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.network.OnlineGameStates
import pt.isec.swipe_maths.network.Server
import pt.isec.swipe_maths.utils.FirestoreUtils

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

        Server.onlineState.observe(viewLifecycleOwner){
            when(it){
                OnlineGameStates.ALL_FINISHED_LEVEL -> {
                    Server.startNewLevelTimers()
                    startTimer()
                }
                OnlineGameStates.ALL_GAME_OVER -> {
                    FirestoreUtils.addGames(
                        GameManager.games.value!!
                    )
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