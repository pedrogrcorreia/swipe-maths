package pt.isec.swipe_maths.activities

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMainBinding
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.utils.SinglePlayerGame
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    lateinit var binding : ActivityMainBinding

    private lateinit var googleSignInClient : GoogleSignInClient

    private var authListener : FirebaseAuth.AuthStateListener? = null

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        updateUI()

        binding.singlePlayer.setOnClickListener {
            startActivity(GameScreenActivity.getSingleModeIntent(this))
        }

        binding.multiPlayer.setOnClickListener{
            val dlg = AlertDialog.Builder(this)
                .setTitle("Multiplayer")
                .setMessage("Want to be server or client")
                .setPositiveButton("Server") { _: DialogInterface, _: Int ->
                    startActivity(GameScreenActivity.getServerModeIntent(this))
                }
                .setNegativeButton("Client") { _: DialogInterface, _: Int ->
                    startActivity(GameScreenActivity.getClientModeIntent(this))
                }
                .create()

            dlg.show()
        }

        binding.userProfile.setOnClickListener(makeSnackbar)

        binding.emailButton.setOnClickListener {
            auth.signInWithEmailAndPassword("pedrogrcorreia@gmail.com", "123456")
        }

        binding.googleButton.setOnClickListener {
            signInWithGoogle.launch(googleSignInClient.signInIntent)
        }

        binding.logoutBtn.setOnClickListener {
            scope.launch {
                val job = launch {
                    if(auth.currentUser?.getIdToken(false)
                            ?.result
                            ?.signInProvider == GoogleAuthProvider.PROVIDER_ID) {
                        googleSignInClient.signOut().await()
                    }
                    auth.signOut()
                    loadingDialog.dismiss()
                }

                if (job.isActive) {
                    runOnUiThread {
                        loadingDialog.show()
                    }
                }
            }
        }

        binding.highScores.setOnClickListener {
            scope.launch {
                val job = launch {
                    val highscoresList = FirestoreUtils.highscoresSinglePlayer()
                    val highscores = ArrayList(highscoresList)
                    startActivity(HighScoresActivity.getIntent(this@MainActivity, highscores))
                    loadingDialog.dismiss()
                }

                if (job.isActive) {
                    runOnUiThread {
                        loadingDialog.show()
                    }
                }
            }
        }

        auth.addAuthStateListener{
            updateUI()
        }

    }

    val signInWithGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try{
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException){
        }
    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        scope.launch {
            val job = launch {
                auth.signInWithCredential(credential).await()
                loadingDialog.dismiss()
            }

            if (job.isActive) {
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }

    }

    private fun updateUI(){
        if(auth.currentUser != null){
            binding.emailButton.visibility = View.INVISIBLE
            binding.googleButton.visibility = View.INVISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
            binding.welcomeTxt.visibility = View.VISIBLE
            binding.welcomeTxt.text = getString(R.string.welcome, auth.currentUser!!.displayName)
        } else {
            binding.emailButton.visibility = View.VISIBLE
            binding.googleButton.visibility = View.VISIBLE
            binding.welcomeTxt.visibility = View.INVISIBLE
            binding.logoutBtn.visibility = View.INVISIBLE
        }
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle("Loading data..")
            .setMessage("Loading data")
            .setView(ProgressBar(this))
            .create()
    }
}