package pt.isec.swipe_maths.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMultiplayerBinding
import pt.isec.swipe_maths.fragments.ClientFragment
import pt.isec.swipe_maths.fragments.ServerFragment
import pt.isec.swipe_maths.network.Client
import pt.isec.swipe_maths.network.Server
import pt.isec.swipe_maths.utils.NetworkFragment

class MultiplayerActivity : AppCompatActivity(), NetworkFragment {

    private lateinit var binding : ActivityMultiplayerBinding

    var mainFragment : Fragment = Fragment()

    private var server = false
    private var client = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClient.setOnClickListener {
            client = true
            mainFragment = ClientFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
        }

        binding.btnServer.setOnClickListener {
            server = true
            mainFragment = ServerFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(client){
                    Client.closeClient()
                } else if(server){
                    Server.closeServer()
                }
                finish()
            }
        })
    }

    override fun cancel() {
        server = false
        client = false
        supportFragmentManager.beginTransaction().remove(mainFragment).commit()
        binding.btnClient.isEnabled = true
        binding.btnServer.isEnabled = true
    }

    override fun onSupportNavigateUp(): Boolean {
        if(client){
            Client.closeClient()
        } else if(server){
            Server.closeServer()
        }
        return true
    }
}