package pt.isec.swipe_maths.fragments

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
import pt.isec.swipe_maths.utils.NetUtils
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

        NetUtils.state.observe(viewLifecycleOwner){
            when(it){
                ConnectionStates.WAITING_FOR_PLAYERS -> {
                    thread {
                        val json = JSONObject()
                        json.put("state", ConnectionStates.CONNECTION_ESTABLISHED)
                        json.put("name", Firebase.auth.currentUser?.displayName)
                        json.put("photo", "https://openai.com/content/images/2021/01/2x-no-mark-1.jpg")
                        NetUtils.sendToServer(json)
                    }
                }
            }
        }

        binding.btnSearch.setOnClickListener {
            thread {
                scope.launch {
                    val job = launch {
                        val ipAddress = NetUtils.contactMulticast()
                        if(ipAddress != null){
                            activity?.runOnUiThread {
                                Toast.makeText(context, getString(R.string.error_timeout), Toast.LENGTH_LONG)
                            }
                        }
                    }
                    if(job.isActive){
                        activity?.runOnUiThread {
                            loadingDialog.show()
                        }
                    }
                }.invokeOnCompletion {
                    loadingDialog.dismiss()
                }

            }
        }


        return binding.root
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(requireContext()))
            .create()
    }
}