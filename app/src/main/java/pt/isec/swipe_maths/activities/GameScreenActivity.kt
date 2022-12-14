package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.fragments.IGameBoardFragment
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.fragments.GamePass
import pt.isec.swipe_maths.fragments.INewLevelFragment
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.network.Server
import pt.isec.swipe_maths.views.GameViewModel

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment, INewLevelFragment, GamePass {
    companion object {
        private const val SINGLE_MODE = 0
        private const val ERROR_MODE = 1
        private const val SERVER_MODE = 1
        private const val CLIENT_MODE = 2
        private var mode = 0

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
            }
        }

        fun getSingleModeIntentError(context: Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply{
                putExtra("mode", ERROR_MODE)
            }
        }
    }

    private lateinit var auth : FirebaseAuth

    private lateinit var binding: ActivityGameScreenBinding

    private lateinit var viewModel : GameViewModel

    private var mode : Int = 0

    private var server = Server

    private  var client = Client

    private val game = Game()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        mode = GameScreenActivity.mode

        when(mode){
            SERVER_MODE -> {
                server = Server
                val viewModelFactory = GameViewModel.GameViewModelFactory(server.game)
                viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
            }
            CLIENT_MODE -> {
                client = Client
                val viewModelFactory = GameViewModel.GameViewModelFactory(client.game)
                viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
            }
            SINGLE_MODE -> {
                val viewModelFactory = GameViewModel.GameViewModelFactory(game)
                viewModel = ViewModelProvider(this, viewModelFactory)[GameViewModel::class.java]
            }
        }

        println("\n\ngame on activity \n" + viewModel.game.toString())

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@GameScreenActivity)
                    .setTitle("Exit game")
                    .setMessage("Close game?")
                    .setPositiveButton("Cancel"){ d: DialogInterface, _: Int ->
                        d.dismiss()
                    }
                    .setNegativeButton("Exit"){_: DialogInterface, _: Int ->
                        finish()
                    }.show()
            }
        })

        viewModel.state.observe(this){
            when(it){
                GameStates.GAME_OVER -> {
                    if(mode != ERROR_MODE) {
                        FirestoreUtils.addGame(
                            viewModel.points.value!!,
                            viewModel.totalTime,
                            auth.currentUser!!.email!!
                        )
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Game over")
                        .setMessage("You ran out of time!")
                        .setPositiveButton("Play Again"){ _: DialogInterface, _: Int ->
                            finish()
                            startActivity(this.intent)
                        }
                        .setNegativeButton("Exit"){ _: DialogInterface, _: Int ->
                            finish()
                        }
                        .show()
                }
            }
        }

        viewModel.points.observe(this){
            binding.lblPoints.text = getString(R.string.points, it)
        }
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
        if(viewModel.columnPlay(selectedColumn)){
            Snackbar.make(binding.root, getString(R.string.correct_col, selectedColumn+1), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.GREEN)
            }.show()
        } else {
            Snackbar.make(binding.root, getString(R.string.incorrect_col, selectedColumn+1), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.RED)
            }.show()
        }

        return true
    }

    override fun swipeHorizontal(selectedLine: Int): Boolean {
        if(viewModel.linePlay(selectedLine)){
            Snackbar.make(binding.root, getString(R.string.correct_row, selectedLine+1), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.GREEN)
            }.show()
        } else{
            Snackbar.make(binding.root, getString(R.string.incorrect_row, selectedLine+1), Snackbar.LENGTH_SHORT).apply{
                setTextColor(Color.RED)
            }.show()
        }
        return true
    }

    override fun timesUp(){
        viewModel.nextLevelTimerUp()
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
    }

    override fun onGamePass(data: Game) : Game{
        return viewModel.game
    }

    fun getGame() : Game{
        return if(GameScreenActivity.mode == SERVER_MODE) {
            server.game
        }else if(GameScreenActivity.mode == CLIENT_MODE){
            client.game
        } else {
            game
        }
    }
}