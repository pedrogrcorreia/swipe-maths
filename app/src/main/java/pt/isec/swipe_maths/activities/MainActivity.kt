package pt.isec.swipe_maths.activities

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMainBinding
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Column
import pt.isec.swipe_maths.model.board.Line
import pt.isec.swipe_maths.model.levels.Levels

class MainActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

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

        binding.userProfile.setOnClickListener{
            if(auth.currentUser == null){
                AlertDialog.Builder(this)
                    .setTitle("Login")
                    .setMessage("You are not logged in")
                    .show()
            }
        }


    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }

    private fun updateUI(){
        if(auth.currentUser != null){
            binding.emailButton.visibility = View.INVISIBLE
            binding.googleButton.visibility = View.INVISIBLE
            binding.welcomeTxt.visibility = View.VISIBLE
        } else {
            binding.emailButton.visibility = View.VISIBLE
            binding.googleButton.visibility = View.VISIBLE
            binding.welcomeTxt.visibility = View.INVISIBLE
            binding.welcomeTxt.text = getString(R.string.welcome, auth.currentUser!!.displayName)
        }
    }
}