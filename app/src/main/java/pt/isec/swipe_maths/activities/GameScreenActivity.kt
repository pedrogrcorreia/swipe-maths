package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.fragments.IGameBoardFragment
import pt.isec.swipe_maths.fragments.INewLevelFragment
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.GameManagerServer
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.network.OnlineGameStates
import pt.isec.swipe_maths.network.Requests
import pt.isec.swipe_maths.network.Server
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.views.GameViewModel


class GameScreenActivity : AppCompatActivity(), IGameBoardFragment, INewLevelFragment {
    companion object {
        const val SINGLE_MODE = 0
        const val ERROR_MODE = 400
        const val SERVER_MODE = 1
        const val CLIENT_MODE = 2
        var mode = 0

        fun getSingleModeIntent(context: Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", SINGLE_MODE)
                mode = SINGLE_MODE
            }
        }

        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", SERVER_MODE)
                mode = SERVER_MODE
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", CLIENT_MODE)
                mode = CLIENT_MODE
            }
        }

        fun getSingleModeIntentError(context: Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply{
                putExtra("mode", ERROR_MODE)
                mode = ERROR_MODE
            }
        }
    }

    private lateinit var auth : FirebaseAuth

    private lateinit var binding: ActivityGameScreenBinding

    private val viewModel : GameViewModel by viewModels()

    private var mode : Int = 0

    private var server = Server

    private  var client = Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        mode = GameScreenActivity.mode

        when(mode){
            SERVER_MODE -> {
                server = Server
                Server.state.observe(this){
                    when(it){
                        ConnectionStates.CONNECTION_ERROR -> {
                            Toast.makeText(
                                this,
                                getString(R.string.error_client),
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                            GameManager.game = Game()
                            startActivity(GameScreenActivity.getSingleModeIntentError(this))
                        }
                    }
                }
            }
            CLIENT_MODE -> {
                client = Client
                client.requestState.observe(this){
                    when(it){
                        Requests.START_GAME, Requests.UPDATE_VIEWS ->
                            viewModel.updateGame(GameManager.game)
                    }
                }
                client.onlineState.observe(this){
                    when(it){
                        OnlineGameStates.START_GAME, OnlineGameStates.PLAYING ->
                            viewModel.updateGame(GameManager.game)
                    }
                }
                client.state.observe(this){
                    when(it) {
                        ConnectionStates.SERVER_ERROR -> {
                            Toast.makeText(
                                this,
                                getString(R.string.error_server),
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                            GameManager.game = Game()
                            startActivity(GameScreenActivity.getSingleModeIntentError(this))
                        }
                    }
                }
            }
            SINGLE_MODE, ERROR_MODE -> {
                val fr = supportFragmentManager.findFragmentById(R.id.fragmentPlayersInfo)
                if (fr != null) {
                    supportFragmentManager.beginTransaction().hide(fr).commit()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@GameScreenActivity)
                    .setTitle(getString(R.string.exit_title_dialog))
                    .setMessage(getString(R.string.close_game_ask))
                    .setPositiveButton(getString(R.string.btn_cancel)){ d: DialogInterface, _: Int ->
                        d.dismiss()
                    }
                    .setNegativeButton(getString(R.string.exit)){ _: DialogInterface, _: Int ->
                        finish()
                    }.show()
            }
        })
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
        if(mode == SINGLE_MODE || mode == ERROR_MODE) {
            if (viewModel.columnPlay(selectedColumn)) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.correct_col, selectedColumn + 1),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.GREEN)
                }.show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.incorrect_col, selectedColumn + 1),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.RED)
                }.show()
            }
        } else if(mode == CLIENT_MODE){
            client.colPlay(selectedColumn)
        } else if(mode == SERVER_MODE){
            server.colPlay(selectedColumn)
        }

        return true
    }

    override fun swipeHorizontal(selectedLine: Int): Boolean {
        if(mode == SINGLE_MODE || mode == ERROR_MODE) {
            if (viewModel.linePlay(selectedLine)) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.correct_row, selectedLine + 1),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.GREEN)
                }.show()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.incorrect_row, selectedLine + 1),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.RED)
                }.show()
            }
        } else if(mode == CLIENT_MODE){
            client.rowPlay(selectedLine)
        } else if(mode == SERVER_MODE){
            server.rowPlay(selectedLine)
        }
        return true
    }

    override fun timesUp(){
        if(GameScreenActivity.mode == SERVER_MODE){
            Server.startNewLevel()
        }
        else if(GameScreenActivity.mode != CLIENT_MODE) {
            viewModel.nextLevelTimerUp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        AlertDialog.Builder(this)
            .setTitle("Exit game")
            .setMessage("Close game?")
            .setPositiveButton("Cancel"){ d: DialogInterface, _: Int ->
                d.dismiss()
            }
            .setNegativeButton("Exit"){_: DialogInterface, _: Int ->
                finish()
            }.show()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mode == SERVER_MODE){
            Server.closeServer()
            GameManagerServer.removeObservers()
            GameManagerServer.finishGame()
        }
        if(mode == CLIENT_MODE){
            Client.closeClient()
        }
        if(mode == SINGLE_MODE){
            GameManager.newGame()
        }
    }

}