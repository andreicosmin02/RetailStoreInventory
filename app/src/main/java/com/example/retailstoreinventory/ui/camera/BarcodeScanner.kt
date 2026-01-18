package com.example.retailstoreinventory.ui.camera

import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class BarcodeScanner {
    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                BarcodeFormat.CODE_128,
            ),
            DecodeHintType.TRY_HARDER to true
        )
        setHints(hints)
    }

    fun processImage(image: ImageProxy): String? {
        return try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            val width = image.width
            val height = image.height

            // CameraX ImageProxy in YUV format needs to be rotated 90 degrees
            // because camera captures in landscape but we're in portrait mode
            val rotatedData = rotateYuv90(data, width, height)

            // Create luminance source from rotated data
            val source = PlanarYUVLuminanceSource(
                rotatedData,
                height,  // width becomes height after rotation
                width,   // height becomes width after rotation
                0,
                0,
                height,
                width,
                false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decodeWithState(bitmap)
            result.text
        } catch (e: Exception) {
            null
        } finally {
            reader.reset()
            image.close()
        }
    }

    /**
     * Rotate YUV data 90 degrees clockwise.
     * Camera captures in landscape, but our UI is in portrait.
     */
    private fun rotateYuv90(data: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotated[x * height + (height - y - 1)] = data[y * width + x]
            }
        }
        return rotated
    }
}