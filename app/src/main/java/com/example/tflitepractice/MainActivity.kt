package com.example.tflitepractice

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnProcess).setOnClickListener {
            cartoonify()
        }
    }
    private fun cartoonify() {
        val cartoonixModel = CartoonixModel(this)
        val inputImage = BitmapFactory.decodeResource(resources, R.drawable.trump)
        val resizedImage = Bitmap.createScaledBitmap(inputImage, 512, 512, true)
        val outputImage = cartoonixModel.runInference(resizedImage)
        findViewById<ImageView>(R.id.imageView).setImageBitmap(outputImage)
    }
}
