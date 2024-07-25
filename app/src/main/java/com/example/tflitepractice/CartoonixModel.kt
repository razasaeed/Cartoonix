package com.example.tflitepractice

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CartoonixModel(context: Context) {
    private val modelPath = "cartoonix.tflite"
    private var interpreter: Interpreter

    init {
        val assetManager = context.assets
        val model = assetManager.open(modelPath).readBytes()
        val modelBuffer = ByteBuffer.allocateDirect(model.size)
        modelBuffer.order(ByteOrder.nativeOrder())
        modelBuffer.put(model)
        val options = Interpreter.Options()
        interpreter = Interpreter(modelBuffer, options)
    }

    fun runInference(inputImage: Bitmap): Bitmap {
        // Preprocess the input image
        val tensorImage = TensorImage.fromBitmap(inputImage)
        val resizeOp = ResizeOp(512, 512, ResizeOp.ResizeMethod.BILINEAR)
        tensorImage.load(resizeOp.apply(tensorImage).bitmap)

        // Convert the image to the format expected by the model
        val inputBuffer = convertBitmapToByteBuffer(tensorImage.bitmap)

        // Prepare the output buffer
        val outputBuffer = ByteBuffer.allocateDirect(512 * 512 * 3 * 4)
        outputBuffer.order(ByteOrder.nativeOrder())

        // Run the model
        interpreter.run(inputBuffer, outputBuffer)

        // Convert outputBuffer to Bitmap
        val outputImage = convertByteBufferToBitmap(outputBuffer)

        return outputImage
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * 512 * 512 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(512 * 512)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in intValues) {
            byteBuffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 127.5f)  // R
            byteBuffer.putFloat(((pixel shr 8 and 0xFF) - 127.5f) / 127.5f)   // G
            byteBuffer.putFloat(((pixel and 0xFF) - 127.5f) / 127.5f)        // B
        }
        return byteBuffer
    }

    private fun convertByteBufferToBitmap(buffer: ByteBuffer): Bitmap {
        val outputImage = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val intValues = IntArray(512 * 512)
        buffer.rewind()
        for (i in intValues.indices) {
            val r = (buffer.float * 127.5f + 127.5f).toInt()
            val g = (buffer.float * 127.5f + 127.5f).toInt()
            val b = (buffer.float * 127.5f + 127.5f).toInt()
            intValues[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }
        outputImage.setPixels(intValues, 0, 512, 0, 0, 512, 512)
        return outputImage
    }
}