package com.example.tflitepractice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        imageView = findViewById(R.id.imageView)
        findViewById<Button>(R.id.btnProcess).setOnClickListener {
            cartoonify()
        }
    }
    private fun cartoonify() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val outputImage = processImage()
            // Hide the progress bar and show the result
            progressBar.visibility = View.GONE
            imageView.setImageBitmap(outputImage)
        }
    }

    private suspend fun processImage(): Bitmap = withContext(Dispatchers.Default) {
        val cartoonixModel = CartoonixModel(this@MainActivity)
        val inputImage = BitmapFactory.decodeResource(resources, R.drawable.trump)
        val resizedImage = Bitmap.createScaledBitmap(inputImage, 512, 512, true)
        cartoonixModel.runInference(resizedImage)
    }
}
