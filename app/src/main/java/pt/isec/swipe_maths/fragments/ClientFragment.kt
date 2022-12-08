package pt.isec.swipe_maths.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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