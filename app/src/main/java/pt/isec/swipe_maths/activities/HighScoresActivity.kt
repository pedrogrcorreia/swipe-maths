package pt.isec.swipe_maths.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityHighScoresBinding
import pt.isec.swipe_maths.fragments.TopScoreSinglePlayer
import pt.isec.swipe_maths.fragments.TopScoreMultiPlayer
import pt.isec.swipe_maths.utils.SinglePlayerGame


class HighScoresActivity : AppCompatActivity() {

    companion object{
        fun getIntent(context: Context): Intent{
            return Intent(context, HighScoresActivity::class.java)
        }
    }

    lateinit var binding : ActivityHighScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ViewPagerAdapter(this)

        binding.pager.adapter = adapter;

        TabLayoutMediator(
            binding.tabs, binding.pager
        ) { tab, position -> when(position){
            0 -> tab.text = getString(R.string.single)
            1 -> tab.text = getString(R.string.multi)
        } }.attach()
    }

    class ViewPagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            when(position){
                0 -> return TopScoreSinglePlayer()
                1 -> return TopScoreMultiPlayer()
                else -> return TopScoreSinglePlayer()
            }
        }

    }
}