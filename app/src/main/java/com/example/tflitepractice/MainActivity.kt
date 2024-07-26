package com.example.tflitepractice

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var selectedImageBitmap: Bitmap
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)
        findViewById<Button>(R.id.btnProcess).apply {
            setOnClickListener {
                cartoonify()
            }
            isEnabled = false
        }

        imageView.setOnClickListener {
            if (checkAndRequestPermissions()) {
                openImageChooser()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_PERMISSION_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser()
            } else {
                Toast.makeText(this, "Permissions are required to use this feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImageChooser() {
        // Allow user to choose image source
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val chooserIntent = Intent.createChooser(pickPhoto, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePicture))
        startActivityForResult(chooserIntent, REQUEST_IMAGE_PICK)
    }

    private fun cartoonify() {
        progressBar.visibility = View.VISIBLE
        imageView.isEnabled = false
        findViewById<Button>(R.id.btnProcess).isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            val outputImage = processImage(selectedImageBitmap)
            progressBar.visibility = View.GONE
            imageView.isEnabled = true
            findViewById<Button>(R.id.btnProcess).isEnabled = true
            imageView.setImageBitmap(outputImage)
        }
    }

    private suspend fun processImage(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val cartoonixModel = CartoonixModel(this@MainActivity)
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
        cartoonixModel.runInference(resizedImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.let {
                        if (it.data != null) {
                            val selectedImage: Uri = it.data!!
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                            selectedImageBitmap = bitmap
                            imageView.setImageBitmap(bitmap)
                            findViewById<Button>(R.id.btnProcess).isEnabled = true
                        } else if (it.extras != null && it.extras!!.get("data") != null) {
                            val bitmap = it.extras!!.get("data") as Bitmap
                            selectedImageBitmap = bitmap
                            imageView.setImageBitmap(bitmap)
                            findViewById<Button>(R.id.btnProcess).isEnabled = true
                        }
                    }
                }
            }
        }
    }
}
