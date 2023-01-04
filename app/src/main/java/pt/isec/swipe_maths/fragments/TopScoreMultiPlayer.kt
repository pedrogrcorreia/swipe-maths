package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentTopScoreMultiPlayerBinding
import pt.isec.swipe_maths.utils.FirestoreUtils
import pt.isec.swipe_maths.utils.OnlineGame
import pt.isec.swipe_maths.utils.SinglePlayerGame
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TopScoreMultiPlayer : Fragment() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private lateinit var binding : FragmentTopScoreMultiPlayerBinding

    private lateinit var highscores : ArrayList<OnlineGame>

    private lateinit var highscoresSingle : ArrayList<SinglePlayerGame>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var pointsOrder = true
        set(value) {
            field = value
            if(value){
                binding.points.setBackgroundResource(R.color.selected_text)
                binding.timeLeft.setBackgroundResource(R.color.white)
            } else{
                binding.points.setBackgroundResource(R.color.white)
                binding.timeLeft.setBackgroundResource(R.color.selected_text)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopScoreMultiPlayerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        pointsOrder = true
        binding.points.setOnClickListener {
            if(pointsOrder){
                return@setOnClickListener
            } else{
                scope.launch {
                    val job = launch {
                        highscores = ArrayList(FirestoreUtils.highscoresMultiPlayer())
                    }
                    if (job.isActive) {
                        requireActivity().runOnUiThread {
                            loadingDialog.show()
                        }
                    }
                }.invokeOnCompletion {
                    requireActivity().runOnUiThread {
                        var highScoresList = binding.rvSinglePlayer
                        highScoresList.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        highScoresList.adapter =
                            HighScoresListAdapter(highscores, requireContext()){
                                gameId -> createRecyclerDialog(gameId)
                            }
                        loadingDialog.dismiss()
                        pointsOrder = true
                    }
                }
            }
        }

        binding.timeLeft.setOnClickListener {
            if(!pointsOrder){
                return@setOnClickListener
            } else{
                scope.launch {
                    val job = launch {
                        highscores = ArrayList(FirestoreUtils.highscoresMultiPlayerTimeOrder())
                    }
                    if (job.isActive) {
                        requireActivity().runOnUiThread {
                            loadingDialog.show()
                        }
                    }
                }.invokeOnCompletion {
                    requireActivity().runOnUiThread {
                        var highScoresList = binding.rvSinglePlayer
                        highScoresList.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        highScoresList.adapter =
                            HighScoresListAdapter(highscores, requireContext()){
                                gameId -> createRecyclerDialog(gameId)
                            }
                        loadingDialog.dismiss()
                        pointsOrder = false
                    }
                }
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        scope.launch {
            val job = launch {
                highscores = ArrayList(FirestoreUtils.highscoresMultiPlayer())
            }
            if (job.isActive) {
                requireActivity().runOnUiThread {
                    loadingDialog.show()
                }
            }
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                var highScoresList = binding.rvSinglePlayer
                highScoresList.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                highScoresList.adapter =
                    HighScoresListAdapter(highscores, requireContext()){
                        gameId -> createRecyclerDialog(gameId)
                    }
                loadingDialog.dismiss()
            }
        }
    }


    class HighScoresListAdapter(val data: ArrayList<OnlineGame>, val context: Context, private val listener: (String) -> Unit) :
        RecyclerView.Adapter<HighScoresListAdapter.MyViewHolder>() {
        class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            var username: TextView = view.findViewById(R.id.playerName)
            var score: TextView = view.findViewById(R.id.points)
            var totalTime: TextView = view.findViewById(R.id.timeLeft)
            var profilePic: ImageView = view.findViewById(R.id.playerPhoto)

            fun update(data: OnlineGame, context: Context) {
                val milliseconds = data.gameDate!!.seconds * 1000 + data.gameDate.nanoseconds / 1000000
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK)
                val netDate = Date(milliseconds)
                username.text = sdf.format(netDate)
                score.text = context.resources.getString(R.string.points_list, data.totalScore)
                totalTime.text = context.resources.getString(R.string.time_left_list, data.totalTime)
                Glide.with(context)
                    .load(context.resources.getString(R.string.default_online_uri))
                    .apply(RequestOptions().circleCrop())
                    .into(profilePic)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = when (viewType) {
                1 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.player_info_list_1, parent, false)
                else -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.player_info_list_2, parent, false)

            }
            view.setOnClickListener {
                listener(data[viewType].gameId!!.toString())
            }
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.update(data[position], context)
        }

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(position: Int): Int = when (val boolean = false) {
            position % 2 == 0 -> 1
            else -> 0
        }
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(requireContext()))
            .create()
    }

    private fun createRecyclerDialog(gameId: String){
        scope.launch {
            val job = launch {
                highscoresSingle = ArrayList(FirestoreUtils.getMultiplayerGame(gameId))
            }
            if (job.isActive) {
                requireActivity().runOnUiThread {
                    loadingDialog.show()
                }
            }
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                val dialog : View = layoutInflater.inflate(R.layout.dialog_recycler_view, null)

                var highScoresList = dialog.findViewById<RecyclerView>(R.id.rvDialog)
                highScoresList.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                highScoresList.adapter =
                    TopScoreSinglePlayer.HighScoresListAdapter(highscoresSingle, requireContext())
                val alertDialog = AlertDialog.Builder(this@TopScoreMultiPlayer.requireContext())
                    .setTitle(getString(R.string.dialog_title_online_game))
                    .setView(dialog)
                    .setCancelable(true)
                    .create()

                alertDialog.show()

                alertDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

                loadingDialog.dismiss()
            }
        }
    }
}