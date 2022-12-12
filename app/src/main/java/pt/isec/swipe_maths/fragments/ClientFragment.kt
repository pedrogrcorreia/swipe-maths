package pt.isec.swipe_maths.fragments

import android.content.Context
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.activities.GameScreenActivity
import pt.isec.swipe_maths.databinding.FragmentClientBinding
import pt.isec.swipe_maths.model.Player
import pt.isec.swipe_maths.utils.Client
import kotlin.concurrent.thread

class ClientFragment : Fragment() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    lateinit var binding : FragmentClientBinding

    lateinit var client : Client

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientBinding.inflate(inflater, container, false)

        client = Client()

        client.state.observe(viewLifecycleOwner){
            when(it){
                ConnectionStates.CONNECTION_ESTABLISHED -> {
                    thread {
                        val json = JSONObject()
                        json.put("state", ConnectionStates.RETRIEVING_CLIENT_INFO)
                        json.put("name", Firebase.auth.currentUser?.displayName)
                        json.put("photo", Firebase.auth.currentUser?.photoUrl)
                        client.sendToServer(json)
                    }
                    binding.btnSearch.visibility = View.GONE
                    binding.btnConnect.visibility = View.GONE
                    binding.btnEmulator.visibility = View.GONE
                    binding.edtIpAddress.visibility = View.GONE
                }
                ConnectionStates.SERVER_ERROR ->{
                    Toast.makeText(activity?.applicationContext, "Server closed!", Toast.LENGTH_LONG).show()
                    startActivity(GameScreenActivity.getSingleModeIntentError(requireContext()))
                }
                ConnectionStates.START_GAME -> {
                    activity?.finish()
                    startActivity(GameScreenActivity.getClientModeIntent(requireContext()))
                }
            }
        }

        var playersList = binding.rvList

        playersList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        val players = client.players

        val listAdapter = PlayerListAdapter(players.value!!, requireContext())
        playersList.adapter = listAdapter

        client.players.observe(viewLifecycleOwner){
            listAdapter.notifyDataSetChanged()
            for(player in it){
                println(player)
            }
        }

        binding.btnSearch.setOnClickListener {
            thread {
                scope.launch {
                    val job = launch {
                        try {
                            val ipAddress = client.contactMulticast()
                            if (ipAddress != null) {
                                if(client.state.value == ConnectionStates.CONNECTION_ERROR) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            getString(R.string.error_timeout),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    if(client.state.value == ConnectionStates.NO_CONNECTION) {
                                        val ipAndPort = ipAddress.split(" ")
                                        println(ipAndPort)
                                        client.startClient(ipAndPort[0], ipAndPort[1].toInt())
                                    }
                                }
                            }
                        } catch(e : Exception){
                            println(e.message)
                            loadingDialog.dismiss()
                        }
                    }
                    if(job.isActive){
                        activity?.runOnUiThread {
                            loadingDialog.show()
                        }
                    }
                }.invokeOnCompletion {
                    activity?.runOnUiThread {
                        loadingDialog.dismiss()
                    }
                }

            }
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if(client.isConnected) {
            client.closeClient()
        }
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
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

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(requireContext()))
            .create()
    }
}