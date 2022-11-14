package pt.isec.swipe_maths.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.model.board.Line

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var buttonSingle : Button = findViewById(R.id.singlePlayer)
        var buttonMulti : Button = findViewById(R.id.multiPlayer)
        var buttonProfile : Button = findViewById(R.id.userProfile)

        buttonSingle.setOnClickListener {
            intent = Intent(this, GameScreenActivity::class.java)
            startActivity(intent)
        }
        buttonMulti.setOnClickListener{
            val line : Line = Line()
            Log.i("Debug", line.printLine())
            Log.i("Debug", line.lineValue().toString())
        }
        buttonProfile.setOnClickListener(makeSnackbar)
    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }
}