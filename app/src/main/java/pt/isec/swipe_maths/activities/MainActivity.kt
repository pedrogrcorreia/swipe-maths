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
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.model.board.Board
import pt.isec.swipe_maths.model.board.Column
import pt.isec.swipe_maths.model.board.Line
import pt.isec.swipe_maths.model.levels.Levels

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSingle : Button = findViewById(R.id.singlePlayer)
        val buttonMulti : Button = findViewById(R.id.multiPlayer)
        val buttonProfile : Button = findViewById(R.id.userProfile)

        buttonSingle.setOnClickListener {
            startActivity(GameScreenActivity.getSingleModeIntent(this))
        }
        buttonMulti.setOnClickListener{
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
        buttonProfile.setOnClickListener(makeSnackbar)
    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }
}