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
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentServerBinding
import pt.isec.swipe_maths.model.Player
import java.net.URL
import kotlin.concurrent.thread

class ServerFragment : Fragment() {

    lateinit var binding : FragmentServerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServerBinding.inflate(inflater, container, false)

        var playersList = binding.rvList

        playersList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        val players = arrayListOf(Player("Pedro", URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")), Player("José", URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")))

        val listAdapter = PlayerListAdapter(players, requireContext())
        playersList.adapter = listAdapter

        thread {
            while(true) {
                players.add(
                    Player(
                        "xyz",
                        URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")
                    )
                )
                Thread.sleep(3000)
                activity?.runOnUiThread {
                    println(players.size)
                    listAdapter.notifyDataSetChanged()
                }
            }
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class PlayerListAdapter(val data: ArrayList<Player>, val context: Context) : RecyclerView.Adapter<PlayerListAdapter.MyViewHolder>(){
        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view){
            var name : TextView = view.findViewById(R.id.playerName)
            var photo : ImageView = view.findViewById(R.id.playerPhoto)

            fun update(data : Player, context: Context){
                name.text = data.name
                Glide.with(context)
                    .load(data.photoUrl)
                    .into(photo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.connected_player_list, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.update(data[position], context)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}