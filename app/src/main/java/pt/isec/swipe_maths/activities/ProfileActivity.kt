package pt.isec.swipe_maths.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityProfileBinding
import java.net.URL

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding

    private var permissionsGranted = false
        set(value){
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val currentUser = Firebase.auth.currentUser!!

        binding.edName.text =
            Editable.Factory.getInstance().newEditable(currentUser.displayName)

        Glide.with(this)
            .load(currentUser.photoUrl ?: URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg"))
            .into(binding.imageView)

        binding.imageView.setOnClickListener{
            if(permissionsGranted){
                chooseImage()
            } else {
                verifyPermissions()
            }
        }
    }

    private fun verifyPermissions() {
        requestPermissionsLauncher.launch(
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults -> // Map<String,Boolean>
        permissionsGranted = grantResults.values.all { it }
    }

    private fun chooseImage(){
        startActivityForContentResult.launch("image/*")
    }

    private var startActivityForContentResult = registerForActivityResult(
        ActivityResultContracts.GetContent() ) { uri ->

    }
}