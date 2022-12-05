package pt.isec.swipe_maths.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityMultiplayerBinding
import pt.isec.swipe_maths.fragments.ClientFragment

class MultiplayerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMultiplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClient.setOnClickListener {
            var mainFragment: ClientFragment = ClientFragment()
            supportFragmentManager.beginTransaction().replace(R.id.mmLayout, mainFragment).commit()
        }
    }
}