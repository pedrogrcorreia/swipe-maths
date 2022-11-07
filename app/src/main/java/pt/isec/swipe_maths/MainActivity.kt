package pt.isec.swipe_maths

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var buttonSingle : Button = findViewById(R.id.singlePlayer)
        var buttonMulti : Button = findViewById(R.id.multiPlayer)
        var buttonProfile : Button = findViewById(R.id.userProfile)

        buttonSingle.setOnClickListener(makeSnackbar)
        buttonMulti.setOnClickListener(makeSnackbar)
        buttonProfile.setOnClickListener(makeSnackbar)
    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }
}