package pt.isec.swipe_maths.fragments

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import pt.isec.swipe_maths.ConnectionStates
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.FragmentClientBinding
import pt.isec.swipe_maths.utils.Client
import kotlin.concurrent.thread

class ClientFragment : Fragment() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    lateinit var binding : FragmentClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientBinding.inflate(inflater, container, false)

        Client.state.observe(viewLifecycleOwner){
            when(it){
                ConnectionStates.CONNECTION_ESTABLISHED -> {
                    thread {
                        val json = JSONObject()
                        json.put("state", ConnectionStates.RETRIEVING_CLIENT_INFO)
                        json.put("name", Firebase.auth.currentUser?.displayName)
                        json.put("photo", "https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")
                        Client.sendToServer(json)
                    }
//                    binding.btnSearch.visibility = View.GONE
                    binding.btnConnect.visibility = View.GONE
                    binding.btnEmulator.visibility = View.GONE
                    binding.edtIpAddress.visibility = View.GONE
                }
            }
        }

        binding.btnSearch.setOnClickListener {
            thread {
                scope.launch {
                    val job = launch {
                        try {
                            val ipAddress = Client.contactMulticast()
                            if (ipAddress != null) {
                                if(Client.state.value == ConnectionStates.CONNECTION_ERROR) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            context,
                                            getString(R.string.error_timeout),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    if(Client.state.value == ConnectionStates.NO_CONNECTION) {
                                        val ipAndPort = ipAddress.split(" ")
                                        println(ipAndPort)
                                        Client.startClient(ipAndPort[0], ipAndPort[1].toInt())
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
        // TODO only close if it's connected
        Client.closeClient()
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(requireContext()))
            .create()
    }
}