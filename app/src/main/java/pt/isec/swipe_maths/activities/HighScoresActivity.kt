package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityHighScoresBinding
import pt.isec.swipe_maths.fragments.TopScoreSinglePlayer
import pt.isec.swipe_maths.fragments.TopScoreMultiPlayer
import pt.isec.swipe_maths.utils.SinglePlayerGame


class HighScoresActivity : AppCompatActivity() {

    companion object{
        fun getIntent(context: Context): Intent{
            return Intent(context, HighScoresActivity::class.java)
        }
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    lateinit var highscores : ArrayList<SinglePlayerGame>

    lateinit var binding : ActivityHighScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ViewPagerAdapter(this)

        binding.pager.adapter = adapter;

        TabLayoutMediator(
            binding.tabs, binding.pager
        ) { tab, position -> when(position){
            0 -> tab.text = "Single Player"
            1 -> tab.text = "Multi Player"
        } }.attach()
    }

    override fun onStart() {
        super.onStart()
//        scope.launch {
//            val job = launch {
//                highscores = ArrayList(FirestoreUtils.highscoresMultiPlayer())
//            }
//            if(job.isActive){
//                runOnUiThread {
//                    loadingDialog.show()
//                }
//            }
//        }.invokeOnCompletion {
//            runOnUiThread {
//                var highScoresList = findViewById<RecyclerView>(R.id.highScoresList)
//                highScoresList.layoutManager = LinearLayoutManager(
//                    this@HighScoresActivity,
//                    LinearLayoutManager.VERTICAL,
//                    false
//                )
//                highScoresList.adapter = HighScoresListAdapter(highscores)
//                loadingDialog.dismiss()
//            }
//        }
    }

//    class HighScoresListAdapter(val data: ArrayList<SinglePlayerGame>) : RecyclerView.Adapter<HighScoresListAdapter.MyViewHolder>(){
//        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view) {
//            var username : TextView = view.findViewById(R.id.username)
//            var score : TextView = view.findViewById(R.id.score)
//            var totalTime : TextView = view.findViewById(R.id.totalTime)
//
//            fun update(data : SinglePlayerGame) {
//                username.text = data.username
//                score.text = data.score.toString()
//                totalTime.text = data.time.toString()
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//            val view = when(viewType) {
//                1 -> LayoutInflater.from(parent.context).inflate(R.layout.highscore_list_1,parent,false)
//                else -> LayoutInflater.from(parent.context).inflate(R.layout.highscore_list_2,parent,false)
//
//            }
//            return MyViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//            holder.update(data[position])
//        }
//
//        override fun getItemCount(): Int = data.size
//
//        override fun getItemViewType(position: Int): Int = when(val boolean = false) {
//            position % 2 == 0 -> 1
//            else -> 0
//        }
//    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(this))
            .create()
    }

    class ViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            when(position){
                0 -> return TopScoreSinglePlayer()
                1 -> return TopScoreMultiPlayer()
                else -> return TopScoreSinglePlayer()
            }
        }

    }
}