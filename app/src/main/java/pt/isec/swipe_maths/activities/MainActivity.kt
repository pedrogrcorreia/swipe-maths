package pt.isec.swipe_maths.activities

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMainBinding
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.utils.NetUtils
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    lateinit var binding : ActivityMainBinding

    private lateinit var googleSignInClient : GoogleSignInClient

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
            if(checkLogin()) {
                startActivity(GameScreenActivity.getSingleModeIntent(this))
            }
        }

        binding.multiPlayer.setOnClickListener{
            if(checkLogin()) {
                intent = Intent(this, MultiplayerActivity::class.java)
                startActivity(intent)
            }
        }

        binding.userProfile.setOnClickListener{
            if(checkLogin()){
                intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

        binding.emailButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.sign_in_form, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.sign_in))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.login), null)
                .show()

            val positiveButton : Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val email : TextView = dialogView.findViewById(R.id.etEmailSignIn)!!
                val password : TextView = dialogView.findViewById(R.id.etPasswordSignIn)!!

                if(email.text.isEmpty() || password.text.isEmpty()){
                    Snackbar.make(dialogView, "All fields must be filled!", Snackbar.LENGTH_LONG).show()
                } else {
                    firebaseAuthWithEmail(email.text.toString(), password.text.toString())
                    dialog.dismiss()
                }
            }
        }

        binding.signUpButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.sign_up_form, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.sign_up))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.sign_up), null)
                .show()

            val positiveButton : Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                val email : String = dialogView.findViewById<TextView?>(R.id.etEmailSignUp).text.toString()
                val password : String = dialogView.findViewById<TextView?>(R.id.etPasswordSignUp).text.toString()
                val confPassword : String = dialogView.findViewById<TextView?>(R.id.etConfPasswordSignUp).text.toString()
                val firstName : String = dialogView.findViewById<TextView?>(R.id.etFirstName).text.toString()
                val lastName : String = dialogView.findViewById<TextView?>(R.id.etLastName).text.toString()
                if(email.isEmpty() || password.isEmpty() ||
                        confPassword.isEmpty() || firstName.isEmpty() ||
                        lastName.isEmpty()){
                    Snackbar.make(dialogView, "All fields must be filled.", Snackbar.LENGTH_LONG).show()
                } else if (confPassword != password){
                    Snackbar.make(dialogView, "Password don't match!", Snackbar.LENGTH_LONG).show()
                } else if (password.length < 6){
                    Snackbar.make(dialogView, "Password must have 6 characters!", Snackbar.LENGTH_LONG).show()
                } else{
                    firebaseSignUpWithEmail(email,
                        password,
                        firstName,
                        lastName)
                    dialog.dismiss()
                }
            }
        }

//        binding.googleButton.setOnClickListener {
//            signInWithGoogle.launch(googleSignInClient.signInIntent)
//        }


        binding.highScores.setOnClickListener {
            startActivity(HighScoresActivity.getIntent(this))
        }

//        auth.addAuthStateListener{
//            updateUI()
//        }

        auth.addIdTokenListener(tokenListener)
    }

    private val tokenListener = FirebaseAuth.IdTokenListener {
        updateUI()
    }

    val signInWithGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try{
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException){
            e.message?.let{ showSnackbarError(it) }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        scope.launch {
            val job = launch {
                try {
                    auth.signInWithCredential(credential).await()
                } catch(e : Exception){
                    e.message?.let { showSnackbarError(it) }
                }

                loadingDialog.dismiss()
            }
            if (job.isActive) {
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }
    }

    private fun firebaseAuthWithEmail(email: String, password: String) {
        scope.launch {
            val job = launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                } catch (e : Exception){
                    e.message?.let { showSnackbarError(it) }
                }
                loadingDialog.dismiss()
            }
            if(job.isActive) {
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }
    }

    private fun firebaseSignUpWithEmail(
        email: String,
        password: String,
        firstName: String,
        lastName: String){
        scope.launch {
            val job = launch {
                var result : AuthResult? = null
                try {
                    result = auth.createUserWithEmailAndPassword(email, password).await()
                    if(result?.user != null){
                        val profileRequest = UserProfileChangeRequest.Builder()
                            .setDisplayName("$firstName $lastName")
                            .setPhotoUri(
                                Uri.parse(getString(R.string.default_pic_uri)))
                            .build()
                        result.user!!.updateProfile(profileRequest).await()
                    }
                    updateUser()
                } catch (e : Exception){
                    e.message?.let { showSnackbarError(it) }
                }
                loadingDialog.dismiss()
            }
            if(job.isActive){
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }
    }

    private fun updateUser(){
        scope.launch {
            val job = launch {
                auth.currentUser!!.getIdToken(true)
                loadingDialog.dismiss()
            }
            if(job.isActive){
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }
    }

    private fun updateUI(){
        if(auth.currentUser != null){
            binding.welcomeTxt.text = getString(R.string.welcome, auth.currentUser!!.displayName)
            Glide.with(this)
                .load(auth.currentUser!!.photoUrl)
                .apply(RequestOptions().circleCrop())
                .into(binding.userPhoto)
            binding.loginLayout.visibility = View.GONE
            binding.cardViewUser.visibility = View.VISIBLE
            binding.userProfile.visibility = View.VISIBLE
        } else {
            binding.loginLayout.visibility = View.VISIBLE
            binding.cardViewUser.visibility = View.GONE
            binding.userProfile.visibility = View.INVISIBLE
        }
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(this))
            .create()
    }

    private fun showSnackbarError(error : String){
        Snackbar.make(this@MainActivity.findViewById(R.id.frLayout),
            getString(R.string.error_message, error),
            Snackbar.LENGTH_LONG).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextColor(getColor(R.color.white))
                setBackgroundTint(getColor(R.color.snackbar_error_bkg))
            }
        }
            .show()
    }

    private fun checkLogin() : Boolean{
        return if(auth.currentUser != null){
            true
        } else {
            showSnackbarError("You must be logged in to play!")
            false
        }
    }

    /**
     * Makes a snackbar displaying that the pressed button is
     * not implemented yet.
     */
    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }
}