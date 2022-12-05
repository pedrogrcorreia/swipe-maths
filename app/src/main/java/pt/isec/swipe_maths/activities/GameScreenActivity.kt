package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.accessibility.AccessibilityEventCompat.setAction
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.GameStates
import pt.isec.swipe_maths.fragments.IGameBoardFragment
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityGameScreenBinding
import pt.isec.swipe_maths.fragments.GameBoardFragment
import pt.isec.swipe_maths.fragments.INewLevelFragment
import pt.isec.swipe_maths.fragments.NewLevelFragment
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.utils.NetUtils
import pt.isec.swipe_maths.utils.NetUtils.Companion.SERVER_PORT
import pt.isec.swipe_maths.views.GameViewModel
import kotlin.concurrent.thread

class GameScreenActivity : AppCompatActivity(), IGameBoardFragment, INewLevelFragment {
    companion object {
        private const val SINGLE_MODE = 0
        private const val SERVER_MODE = 1
        private const val CLIENT_MODE = 2

        fun getSingleModeIntent(context: Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", SINGLE_MODE)
            }
        }

        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", SERVER_MODE)
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context, GameScreenActivity::class.java).apply {
                putExtra("mode", CLIENT_MODE)
            }
        }
    }

    private lateinit var auth : FirebaseAuth

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private var dlg: AlertDialog? = null

    private lateinit var binding: ActivityGameScreenBinding

    private val viewModel : GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        loadingDialog.show()
        loadingDialog.dismiss()
        when (intent.getIntExtra("mode", SERVER_MODE)) {
            SERVER_MODE -> startAsServer()
            CLIENT_MODE -> startAsClient()
        }

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

        NetUtils.newClient()

        viewModel.state.observe(this){
            when(it){
                GameStates.GAME_OVER -> {
                    FirestoreUtils.addGame(viewModel.points.value!!, viewModel.totalTime, auth.currentUser!!.email!!)
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

        viewModel.connectionState.observe(this){
            when(it){
                ConnectionStates.CONNECTION_ERROR -> {
                    finish()
                }
                ConnectionStates.WAITING_FOR_PLAYERS -> {
                    loadingDialog.show()
                }
                ConnectionStates.START_GAME -> {
                    loadingDialog.dismiss()
                }
            }
        }
    }


    private fun startAsClient(){
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it == '.')
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        dlg = AlertDialog.Builder(this)
            .setTitle("Client mode")
            .setMessage("IP")
            .setPositiveButton("CONNECT") { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this@GameScreenActivity, "ERRO DO CRL", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    NetUtils.startClient(strIP)
                }
            }
            .setNeutralButton("EMULATOR") { _: DialogInterface, _: Int ->
                NetUtils.startClient("10.0.2.2", SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton("SEARCH") { _: DialogInterface, _: Int ->
                thread {
                    scope.launch {
                        val job = launch {
                            val ipAddress = NetUtils.contactMulticast()
                            if (ipAddress != null) {
                                runOnUiThread{
                                    Toast.makeText(this@GameScreenActivity, getString(R.string.error_timeout), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        if (job.isActive) {
                            runOnUiThread {
                                loadingDialog.show()
                            }
                        }
                    }.invokeOnCompletion {
                        loadingDialog.dismiss()
                    }
                }
            }
            .setCancelable(true)
            .setView(edtBox)
            .create()

        dlg?.show()
    }

    private fun startAsServer(){
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format("Server IP address: %s\nWaiting for a client...",strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this)
            .setTitle("Server mode")
            .setView(ll)
            .setPositiveButton("Start Game"){ _ : DialogInterface, _ : Int ->
                NetUtils.startGame()
            }
            .setOnCancelListener {
                finish()
            }
            .create()

        NetUtils.startServer(strIPAddress)
        dlg?.show()
    }

    override fun swipeVertical(selectedColumn: Int): Boolean {
//        Log.i("Debug", "max value: ${viewModel.getMaxValue()}")
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


    // TODO join this
    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(this))
            .create()
    }

    // TODO join this
    private fun showSnackbarError(error : String){
        Snackbar.make(this@GameScreenActivity.findViewById(R.id.lLayout),
            getString(R.string.error_message, error),
            Snackbar.LENGTH_LONG).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextColor(getColor(R.color.white))
                setBackgroundTint(getColor(R.color.snackbar_error_bkg))
            }
        }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dlg?.dismiss()
    }
}