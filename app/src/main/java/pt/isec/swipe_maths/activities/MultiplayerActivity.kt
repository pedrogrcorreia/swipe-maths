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

class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMultiplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var mainFragment : Fragment = Fragment()

        binding.btnCancel.isEnabled = false

        binding.btnClient.setOnClickListener {
            mainFragment = ClientFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
            binding.btnCancel.isEnabled = true
        }

        binding.btnServer.setOnClickListener {
            mainFragment = ServerFragment()
            supportFragmentManager.beginTransaction().add(R.id.mmLayout, mainFragment).commit()
            binding.btnClient.isEnabled = false
            binding.btnServer.isEnabled = false
            binding.btnCancel.isEnabled = true
        }

        binding.btnCancel.setOnClickListener{
            supportFragmentManager.beginTransaction().remove(mainFragment).commit()
            binding.btnClient.isEnabled = true
            binding.btnServer.isEnabled = true
            binding.btnCancel.isEnabled = false
        }
    }
}