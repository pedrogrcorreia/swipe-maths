package pt.isec.swipe_maths.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMultiplayerBinding
import pt.isec.swipe_maths.fragments.ClientFragment
import pt.isec.swipe_maths.fragments.ServerFragment
import pt.isec.swipe_maths.utils.NetworkFragment

class MultiplayerActivity : AppCompatActivity(), NetworkFragment {

    private lateinit var binding : ActivityMultiplayerBinding

    var mainFragment : Fragment = Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClient.setOnClickListener {
            mainFragment = ClientFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
        }

        binding.btnServer.setOnClickListener {
            mainFragment = ServerFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
        }
    }

    override fun cancel() {
        supportFragmentManager.beginTransaction().remove(mainFragment).commit()
        binding.btnClient.isEnabled = true
        binding.btnServer.isEnabled = true
    }
}