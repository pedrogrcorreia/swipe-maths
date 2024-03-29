package pt.isec.swipe_maths.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.network.OnlineGameStates
import pt.isec.swipe_maths.utils.NetworkFragment
import kotlin.concurrent.thread

class ClientFragment : Fragment() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    lateinit var binding: FragmentClientBinding

    lateinit var client: Client

    var actBase: NetworkFragment? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actBase = context as? NetworkFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientBinding.inflate(inflater, container, false)

        client = Client

        val user = Firebase.auth.currentUser

        client.state.observe(viewLifecycleOwner) {
            when (it) {
                ConnectionStates.CONNECTION_ESTABLISHED -> {
                    client.newPlayer(Player(user?.displayName!!, user.photoUrl!!.toString(), user.uid))
                    binding.btnSearch.visibility = View.GONE
                    binding.btnConnect.visibility = View.GONE
                    binding.btnEmulator.visibility = View.GONE
                    binding.edtIpAddress.visibility = View.GONE
                }
                ConnectionStates.SERVER_ERROR -> {
                    Toast.makeText(
                        activity?.applicationContext,
                        "Server closed!",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(GameScreenActivity.getSingleModeIntentError(requireContext()))
                }
            }
        }

        client.onlineState.observe(viewLifecycleOwner) {
            when (it) {
                OnlineGameStates.START_GAME -> {
                    requireActivity().finish()
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

        client.players.observe(viewLifecycleOwner) {
            listAdapter.notifyDataSetChanged()
        }

        binding.btnCancel.setOnClickListener {
            if (client.isConnected) {
                client.closeClient()
            }
            actBase!!.cancel()
        }

        binding.btnSearch.setOnClickListener {
            thread {
                scope.launch {
                    val job = launch {
                        try {
                            val ipAddress = client.contactMulticast()
                            if (ipAddress == null) {

                                activity?.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.error_timeout),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                if (client.state.value == ConnectionStates.NO_CONNECTION || Client.state.value == ConnectionStates.CONNECTION_ERROR) {
                                    val ipAndPort = ipAddress.split(" ")
                                    client.startClient(ipAndPort[0], ipAndPort[1].toInt())
                                }
                            }
                        } catch (e: Exception) {
                            loadingDialog.dismiss()
                        }
                    }
                    if (job.isActive) {
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

        binding.btnEmulator.setOnClickListener {
            client.startClient(serverPort = client.SERVER_PORT_EMULATOR)
        }

        binding.btnConnect.setOnClickListener {
            val strIP = binding.edtIpAddress.text.toString()
            if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                Toast.makeText(requireContext(), R.string.error_message, Toast.LENGTH_LONG).show()
            } else {
                Client.startClient(strIP)
            }
        }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class PlayerListAdapter(val data: List<Player>, val context: Context) :
        RecyclerView.Adapter<PlayerListAdapter.MyViewHolder>() {
        class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById(R.id.playerName)
            var photo: ImageView = view.findViewById(R.id.playerPhoto)

            fun update(data: Player, context: Context) {
                name.text = data.name
                Glide.with(context)
                    .load(data.photoUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(photo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = when (viewType) {
                1 -> {
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.connected_player_list, parent, false)
                }
                else -> {
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.connected_player_list_2, parent, false)
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
}