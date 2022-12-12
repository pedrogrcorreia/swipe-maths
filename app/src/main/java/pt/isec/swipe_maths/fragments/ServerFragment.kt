package pt.isec.swipe_maths.fragments

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentServerBinding
import pt.isec.swipe_maths.model.Player
import pt.isec.swipe_maths.utils.Server

class ServerFragment : Fragment() {

    lateinit var binding : FragmentServerBinding

    lateinit var server: Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServerBinding.inflate(inflater, container, false)

        server = Server()

        var playersList = binding.rvList

        playersList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        val players = server.players //arrayListOf(Player("Pedro", URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")), Player("José", URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")))

        val listAdapter = PlayerListAdapter(players.value!!, requireContext())
        playersList.adapter = listAdapter

        server.players.observe(viewLifecycleOwner){
            listAdapter.notifyDataSetChanged()
            val json = JSONObject()
            json.put("state", ConnectionStates.UPDATE_PLAYERS_LIST)
            json.put("players", Player.playersToJson(players.value!!))
            println(players.value!!)
            server.sendToClients(json)
        }



//        thread {
//            while(true) {
//                players.add(
//                    Player(
//                        "xyz",
//                        URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")
//                    )
//                )
//                Thread.sleep(3000)
//                activity?.runOnUiThread {
//                    println(players.size)
//                    listAdapter.notifyDataSetChanged()
//                }
//            }
//        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val wifiManager = requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        binding.ipAddress.text = getString(R.string.ip_address, strIPAddress)

        server.startServer(strIPAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("OnDestroy!!")
        server.closeServer()
    }

    class PlayerListAdapter(val data: List<Player>, val context: Context) : RecyclerView.Adapter<PlayerListAdapter.MyViewHolder>(){
        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view){
            var name : TextView = view.findViewById(R.id.playerName)
            var photo : ImageView = view.findViewById(R.id.playerPhoto)

            fun update(data : Player, context: Context){
                name.text = data.name
                Glide.with(context)
                    .load(data.photoUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(photo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListAdapter.MyViewHolder {
            val view = when(viewType) {
                1 -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.connected_player_list, parent, false)
                }
                else -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.connected_player_list_2, parent, false)
                }
            }
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.update(data[position], context)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemViewType(position: Int): Int = when(val boolean = false) {
            position % 2 == 0 -> 1
            else -> 0
        }
    }
}