package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CompressedMediaResult(
    val mainBytes: ByteArray,
    val mainMime: String,
    val mainFileName: String,
    val thumbBytes: ByteArray,
    val thumbMime: String,
    val thumbFileName: String,
    val previewBitmap: Bitmap
)

object ImageCompressor {
    private const val THUMB_MAX_DIMENSION = 400
    private const val THUMB_QUALITY = 80
    private const val MAIN_MAX_DIMENSION = 1920
    private const val MAIN_QUALITY = 85

    suspend fun processImageUri(context: Context, uri: Uri): Result<CompressedMediaResult> = withContext(Dispatchers.IO) {
        runCatching {
            val contentResolver = context.contentResolver

            var originalName = "story_${System.currentTimeMillis()}.jpg"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) originalName = name
                    }
                }
            }

            // FIX #2: safe baseName
            val baseName = originalName.substringBeforeLast(".")
                .ifBlank { "story_${System.currentTimeMillis()}" }

            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    orientation = ExifInterface(stream)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
            } catch (_: Exception) {}

            val decodedBitmap: Bitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw IllegalStateException("Could not decode image from URI")

            val rotatedBitmap = rotateBitmap(decodedBitmap, orientation)

            val mainBitmap = scaleBitmapToMax(rotatedBitmap, MAIN_MAX_DIMENSION)
            val mainStream = ByteArrayOutputStream()
            mainBitmap.compress(Bitmap.CompressFormat.JPEG, MAIN_QUALITY, mainStream)
            val mainBytes = mainStream.toByteArray()

            val thumbScale = min(1f, THUMB_MAX_DIMENSION.toFloat() / max(rotatedBitmap.width, rotatedBitmap.height).toFloat())
            val thumbW = max(1, (rotatedBitmap.width * thumbScale).roundToInt())
            val thumbH = max(1, (rotatedBitmap.height * thumbScale).roundToInt())
            val thumbBitmap = Bitmap.createScaledBitmap(rotatedBitmap, thumbW, thumbH, true)

            // FIX #1: mime + filename match actual bytes
            @Suppress("DEPRECATION")
            val webpFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }

            val thumbStream = ByteArrayOutputStream()
            val webpOk = try {
                thumbBitmap.compress(webpFormat, THUMB_QUALITY, thumbStream) && thumbStream.size() > 0
            } catch (_: Exception) { false }

            val thumbBytes: ByteArray
            val thumbMime: String
            val thumbFileName: String
            if (webpOk) {
                thumbBytes = thumbStream.toByteArray()
                thumbMime = "image/webp"
                thumbFileName = "$baseName-thumb.webp"
            } else {
                val fallbackStream = ByteArrayOutputStream()
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, THUMB_QUALITY, fallbackStream)
                thumbBytes = fallbackStream.toByteArray()
                thumbMime = "image/jpeg"
                thumbFileName = "$baseName-thumb.jpg"
            }

            CompressedMediaResult(
                mainBytes = mainBytes,
                mainMime = "image/jpeg",
                mainFileName = "$baseName.jpg",
                thumbBytes = thumbBytes,
                thumbMime = thumbMime,
                thumbFileName = thumbFileName,
                previewBitmap = thumbBitmap
            )
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            rotated
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun scaleBitmapToMax(bitmap: Bitmap, maxDim: Int): Bitmap {
        val origMax = max(bitmap.width, bitmap.height)
        if (origMax <= maxDim) return bitmap
        val scale = maxDim.toFloat() / origMax.toFloat()
        val w = max(1, (bitmap.width * scale).roundToInt())
        val h = max(1, (bitmap.height * scale).roundToInt())
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
}
