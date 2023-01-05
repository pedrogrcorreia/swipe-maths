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
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.activities.GameScreenActivity.Companion.CLIENT_MODE
import pt.isec.swipe_maths.activities.GameScreenActivity.Companion.SERVER_MODE
import pt.isec.swipe_maths.databinding.FragmentGameOverBinding
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.GameManagerClient
import pt.isec.swipe_maths.model.GameManagerServer
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.network.OnlineGameStates
import pt.isec.swipe_maths.network.Server
import pt.isec.swipe_maths.utils.FirestoreUtils

class GameOverFragment : Fragment() {

    var actBase: INewLevelFragment? = null

    private var countTime: Long = 5000

    private var timer: CountDownTimer? = null

    private lateinit var binding: FragmentGameOverBinding

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
        binding = FragmentGameOverBinding.inflate(inflater, container, false)

        if (GameScreenActivity.mode == SERVER_MODE || GameScreenActivity.mode == CLIENT_MODE) {
            binding.btnPlayAgain.visibility = View.GONE
        } else {
            binding.btnExit.isEnabled = true
            binding.btnPlayAgain.isEnabled = true
        }

        Server.onlineState.observe(viewLifecycleOwner) {
            when (it) {
                OnlineGameStates.ALL_FINISHED_LEVEL -> {
                    Server.startNewLevelTimers()
                    startTimer()
                }
                OnlineGameStates.ALL_GAME_OVER -> {
                    binding.btnExit.isEnabled = true
                    binding.txtGameOver.visibility = View.VISIBLE
                    FirestoreUtils.addGames(
                        GameManager.games.value!!
                    )
                }
            }
        }

        Client.onlineState.observe(viewLifecycleOwner) {
            when (it) {
                OnlineGameStates.ALL_GAME_OVER -> {
                    binding.btnExit.isEnabled = true
                    binding.txtGameOver.visibility = View.VISIBLE
                }
            }
        }

        if(GameScreenActivity.mode == GameScreenActivity.SINGLE_MODE){
            FirestoreUtils.addGame(GameManager.game)
        }

        binding.btnExit.setOnClickListener {
            requireActivity().finish()
            GameManager.newGame()
            GameManagerServer.finishGame()
            GameManagerClient.finishGame()
        }

        binding.btnPlayAgain.setOnClickListener {
            requireActivity().finish()
            GameManager.newGame()
            startActivity(requireActivity().intent)
        }
        return binding.root
    }

    private fun startTimer() {
        timer = object : CountDownTimer(countTime, 1000) {
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