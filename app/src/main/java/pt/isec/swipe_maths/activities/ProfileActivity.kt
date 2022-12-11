package pt.isec.swipe_maths.activities

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import pt.isec.swipe_maths.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream
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

        updatePhoto(currentUser.photoUrl)

        binding.imageView.setOnClickListener{
            chooseImage()
        }

        verifyPermissions()
    }

    private fun updatePhoto(imagePath: Uri?){
        println("Image path: $imagePath")
        val requestOptions = RequestOptions()
        Glide.with(this)
            .load(imagePath?.path ?: URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg"))
            .apply(requestOptions.circleCrop())
            .into(binding.imageView)
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
            // TODO Upload to storage
        val imagePath = uri?.let { createFileFromUri(this, uri) }
        println(imagePath)
        updatePhoto(imagePath?.toUri())
    }

    private fun getTempFilename(context: Context,
                        prefix: String = "image", extension : String = ".png") : String =
        File.createTempFile(
            prefix, extension,
            context.externalCacheDir
        ).absolutePath

    private fun createFileFromUri(
        context: Context,
        uri : Uri,
        filename : String = getTempFilename(context)
    ) : String {
        FileOutputStream(filename).use { outputStream ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return filename
    }
}