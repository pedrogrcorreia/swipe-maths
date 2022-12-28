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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.databinding.FragmentServerBinding
import pt.isec.swipe_maths.model.Player
import pt.isec.swipe_maths.utils.NetworkFragment
import pt.isec.swipe_maths.network.Server

class ServerFragment : Fragment() {

    lateinit var binding : FragmentServerBinding

    private var server = Server

    var actBase : NetworkFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actBase = context as? NetworkFragment
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

        val players = server.players

        val listAdapter = PlayerListAdapter(players.value!!, requireContext())
        playersList.adapter = listAdapter

        server.players.observe(viewLifecycleOwner){
            listAdapter.notifyDataSetChanged()
        }

        binding.btnStartGame.setOnClickListener {
            if(server.players.value?.size!! >= 1 /*TODO 2*/){
                requireActivity().finish()
                startActivity(GameScreenActivity.getServerModeIntent(requireContext()))
                server.startGame()
            } else {
                Toast.makeText(activity?.applicationContext, "Not enough players", Toast.LENGTH_LONG).show()
            }
        }
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

        server.startServer(strIPAddress)

        binding.ipAddress.text = getString(R.string.ip_address, strIPAddress)

        binding.btnCancel.setOnClickListener {
            server.closeServer()
            actBase!!.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class PlayerListAdapter(val data: List<Player>, val context: Context) : RecyclerView.Adapter<PlayerListAdapter.MyViewHolder>(){
        class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view){
            var name : TextView = view.findViewById(R.id.playerName)
            var photo : ImageView = view.findViewById(R.id.playerPhoto)
            var time : TextView = view.findViewById(R.id.timeLeft)
            var points : TextView = view.findViewById(R.id.points)

            fun update(data : Player, context: Context){
                name.text = data.name
                time.text = ""
                points.text = ""
                Glide.with(context)
                    .load(data.photoUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(photo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListAdapter.MyViewHolder {
            val view = when(viewType) {
                1 -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.player_info_list_1, parent, false)
                }
                else -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.player_info_list_2, parent, false)
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