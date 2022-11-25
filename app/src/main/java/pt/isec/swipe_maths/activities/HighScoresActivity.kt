package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.utils.SinglePlayerGame

class HighScoresActivity : AppCompatActivity() {

    companion object{
        fun getIntent(context: Context): Intent{
            return Intent(context, HighScoresActivity::class.java)
        }
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    lateinit var highscores : ArrayList<SinglePlayerGame>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            val job = launch {
                highscores = ArrayList(FirestoreUtils.highscoresSinglePlayer())
            }
            if(job.isActive){
                runOnUiThread {
                    loadingDialog.show()
                }
            }
        }.invokeOnCompletion {
            runOnUiThread {
                var highScoresList = findViewById<RecyclerView>(R.id.highScoresList)
                highScoresList.layoutManager = LinearLayoutManager(
                    this@HighScoresActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                highScoresList.adapter = HighScoresListAdapter(highscores)
                loadingDialog.dismiss()
            }
        }
    }

    class HighScoresListAdapter(val data: ArrayList<SinglePlayerGame>) : RecyclerView.Adapter<HighScoresListAdapter.MyViewHolder>(){
        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
            var username : TextView = view.findViewById(R.id.username)
            var score : TextView = view.findViewById(R.id.score)
            var totalTime : TextView = view.findViewById(R.id.totalTime)

            fun update(data : SinglePlayerGame) {
                username.text = data.username
                score.text = data.score.toString()
                totalTime.text = data.time.toString()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = when(viewType) {
                1 -> LayoutInflater.from(parent.context).inflate(R.layout.highscore_list_1,parent,false)
                else -> LayoutInflater.from(parent.context).inflate(R.layout.highscore_list_2,parent,false)

            }
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.update(data[position])
        }

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(position: Int): Int = when(val boolean = false) {
            position % 2 == 0 -> 1
            else -> 0
        }
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(this))
            .create()
    }
}