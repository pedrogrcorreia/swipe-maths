package pt.isec.swipe_maths.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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

        var buttonSingle : Button = findViewById(R.id.singlePlayer)
        var buttonMulti : Button = findViewById(R.id.multiPlayer)
        var buttonProfile : Button = findViewById(R.id.userProfile)

        buttonSingle.setOnClickListener {
            intent = Intent(this, GameScreenActivity::class.java)
            startActivity(intent)
        }
        buttonMulti.setOnClickListener{
//            val firstLine = Line()
//            val secLine = Line()
//            val thirdLine = Line()
//
//            val firstCol = Column(firstLine.numbers[0], secLine.numbers[0], thirdLine.numbers[0])
//            val secCol = Column(firstLine.numbers[1], secLine.numbers[1], thirdLine.numbers[1])
//            val thirdCol = Column(firstLine.numbers[2], secLine.numbers[2], thirdLine.numbers[2])
//
//            Log.i("Debug", "Line 1: " + firstLine.printLine())
//            Log.i("Debug", "Line 2: " + secLine.printLine())
//            Log.i("Debug", "Line 3: " + thirdLine.printLine())
//
//            Log.i("Debug","Col 1: " + firstCol.printColumn())
//            Log.i("Debug","Col 2: " + secCol.printColumn())
//            Log.i("Debug","Col 3: " + thirdCol.printColumn())
//
//            Log.i("Debug", "Col 1 value : " + firstCol.colValue())
            var board : Board = Board(Levels.Expert)
            Log.i("Debug", board.printBoard())
        }
        buttonProfile.setOnClickListener(makeSnackbar)
    }

    private val makeSnackbar = View.OnClickListener {
        Snackbar.make(it, "${(it as Button).text}: ${getString(R.string.todo)}", Snackbar.LENGTH_LONG).show()
    }
}