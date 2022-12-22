package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentPlayersInfoBinding
import pt.isec.swipe_maths.model.Game
import pt.isec.swipe_maths.model.GameManager
import pt.isec.swipe_maths.model.GameManagerClient

class PlayersInfoFragment : Fragment() {

    lateinit var binding: FragmentPlayersInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayersInfoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        var gamesList = binding.playerInfoList

        gamesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val games = GameManager.games

        val listAdapter = PlayerInfoListAdapter(games.value!!, requireContext())

        gamesList.adapter = listAdapter

        GameManager.games.observe(viewLifecycleOwner){
            listAdapter.notifyDataSetChanged()
        }

        return binding.root
    }


    class PlayerInfoListAdapter(val data: List<Game>, val context: Context) :
        RecyclerView.Adapter<PlayerInfoListAdapter.MyViewHolder>() {
        class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById(R.id.playerName)
            var score: TextView = view.findViewById(R.id.score)
            var timeLeft: TextView = view.findViewById(R.id.timeLeft)
            var photo: ImageView = view.findViewById(R.id.playerPhoto)
            fun update(data: Game, context: Context) {
                name.text = data.player.name
                score.text = data.pointsData.toString()
                timeLeft.text = data.remainingTimeData.toString()
                Glide.with(context)
                    .load(data.player.photoUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(photo)

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = when (viewType) {
                1 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.player_info_list_1, parent, false)
                else -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.player_info_list_2, parent, false)
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
}