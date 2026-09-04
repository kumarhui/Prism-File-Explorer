package com.raival.compose.file.explorer.customtools.passport

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume

enum class PassportBackground(
    val title: String,
    val startColor: Int,
    val endColor: Int? = null
) {

    SKY_BLUE(
        "Sky Blue",
        0xFF87CEEB.toInt()
    ),

    LIGHT_PINK(
        "Light Pink",
        0xFFFFD1DC.toInt()
    ),

    WHITE(
        "White",
        Color.WHITE
    ),

    BLUE(
        "Blue",
        0xFFB8E7FF.toInt(),
        0xFF4FA3D1.toInt()
    ),

    PINK(
        "Pink",
        0xFFFFD6E7.toInt(),
        0xFFB9DFFF.toInt()
    )
}

object PassportPhotoProcessor {

    suspend fun removeBackground(
        bitmap: Bitmap
    ): Bitmap? =
        withContext(Dispatchers.Default) {

            try {

                val options =
                    SubjectSegmenterOptions
                        .Builder()
                        .enableForegroundBitmap()
                        .build()

                val segmenter =
                    SubjectSegmentation
                        .getClient(options)

                val input =
                    InputImage.fromBitmap(
                        bitmap,
                        0
                    )

                val foreground =
                    suspendCancellableCoroutine<Bitmap?> { continuation ->

                        segmenter
                            .process(input)
                            .addOnSuccessListener { result ->

                                continuation.resume(
                                    result.foregroundBitmap
                                )
                            }
                            .addOnFailureListener {

                                continuation.resume(null)
                            }
                    }

                segmenter.close()

                foreground

            } catch (_: Exception) {

                null
            }
        }

    fun applyBackground(
        foreground: Bitmap,
        background: PassportBackground
    ): Bitmap {

        val output =
            Bitmap.createBitmap(
                foreground.width,
                foreground.height,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(output)

        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        if (background.endColor == null) {

            paint.color =
                background.startColor

        } else {

            paint.shader =
                LinearGradient(
                    0f,
                    0f,
                    output.width.toFloat(),
                    output.height.toFloat(),
                    background.startColor,
                    background.endColor,
                    Shader.TileMode.CLAMP
                )
        }

        canvas.drawRect(
            0f,
            0f,
            output.width.toFloat(),
            output.height.toFloat(),
            paint
        )

        paint.shader = null

        canvas.drawBitmap(
            foreground,
            0f,
            0f,
            Paint(
                Paint.ANTI_ALIAS_FLAG or
                    Paint.FILTER_BITMAP_FLAG
            )
        )

        return output
    }
}

