package pt.isec.swipe_maths.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentServerBinding
import pt.isec.swipe_maths.model.Player
import java.net.URL

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

        val players = arrayListOf(Player("Pedro", URL("https://google.com")), Player("José", URL("https://google.com")))

        playersList.adapter = PlayerListAdapter(players)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class PlayerListAdapter(val data: ArrayList<Player>) : RecyclerView.Adapter<PlayerListAdapter.MyViewHolder>(){
        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view){
            var name : TextView = view.findViewById(R.id.playerName)
            var photo : ImageView = view.findViewById(R.id.playerPhoto)

            fun update(data : Player){
                name.text = data.name
                // put photo!
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.connected_player_list, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.update(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}