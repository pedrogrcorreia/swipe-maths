package pt.isec.swipe_maths.activities

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.isec.swipe_maths.R
import pt.isec.swipe_maths.databinding.ActivityProfileBinding
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding

    lateinit var storage : FirebaseStorage

    lateinit var currentUser : FirebaseUser

    private var permissionsGranted = false
        set(value){
            field = value
        }

    private var newPhotoUrl : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        storage = Firebase.storage

        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)

        currentUser = Firebase.auth.currentUser!!

        binding.edName.text =
            Editable.Factory.getInstance().newEditable(currentUser.displayName)

        updatePhoto(currentUser.photoUrl)

        binding.imageView.setOnClickListener{
            chooseImage()
        }

        binding.button.setOnClickListener {
            loadingDialog.show()
            val profileRequest = UserProfileChangeRequest.Builder()
                .setDisplayName("${binding.edName.text}")
                .setPhotoUri(newPhotoUrl ?: currentUser.photoUrl)
                .build()
            currentUser.updateProfile(profileRequest).addOnSuccessListener {
                Toast.makeText(applicationContext, "Update successfully", Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "Update failed", Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }.addOnCompleteListener {
                checkNewInfo()
            }

        }

        verifyPermissions()
    }

    private fun updatePhoto(imagePath: Uri?){
        println("Image path: $imagePath")
        val requestOptions = RequestOptions().circleCrop().placeholder(R.drawable.circular_progress_bar)
        Glide.with(this)
            .load(imagePath ?: URL("https://openai.com/content/images/2021/01/2x-no-mark-1.jpg"))
            .apply(requestOptions)
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
        if(uri != null) {
            val storageRef = storage.reference

            val imageUri: Uri? = uri
            val sd = getFileName(applicationContext, imageUri!!)

            val uploadTask = storageRef.child("file/$sd").putFile(imageUri)

            uploadTask.addOnSuccessListener {
                storageRef.child("file/$sd").downloadUrl.addOnSuccessListener {
                    newPhotoUrl = it
                    updatePhoto(it)
                    checkNewInfo()
                }
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if(cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 1)
                    }
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }

    private val loadingDialog: AlertDialog by lazy {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.loading_title))
            .setMessage(getString(R.string.loading_message))
            .setView(ProgressBar(this))
            .create()
    }

    private fun checkNewInfo(){
        if(currentUser.photoUrl != newPhotoUrl){
            binding.tvWarning.visibility = View.VISIBLE
        } else {
            binding.tvWarning.visibility = View.INVISIBLE
        }
    }
}