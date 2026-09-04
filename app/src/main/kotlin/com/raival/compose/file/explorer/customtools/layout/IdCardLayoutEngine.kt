package com.raival.compose.file.explorer.customtools.layout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object IdCardLayoutEngine {

    // ISO/IEC 7810 ID-1
    const val CARD_WIDTH_MM = 85.60f
    const val CARD_HEIGHT_MM = 53.98f

    fun createA4Sheet(
        source: Bitmap
    ): Bitmap {

        val pageWidth =
            A4Page.WIDTH_PX

        val pageHeight =
            A4Page.HEIGHT_PX

        val result =
            Bitmap.createBitmap(
                pageWidth,
                pageHeight,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(result)

        canvas.drawColor(Color.WHITE)

        val cardWidth =
            A4Page.mmToPx(
                CARD_WIDTH_MM
            )

        val cardHeight =
            A4Page.mmToPx(
                CARD_HEIGHT_MM
            )

        val left =
            (pageWidth - cardWidth) / 2f

        val top =
            (pageHeight - cardHeight) / 2f

        val destination =
            RectF(
                left,
                top,
                left + cardWidth,
                top + cardHeight
            )

        val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG or
                    Paint.FILTER_BITMAP_FLAG
            )

        canvas.drawBitmap(
            source,
            null,
            destination,
            paint
        )

        // Thin outline showing the physical card boundary.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.LTGRAY

        canvas.drawRect(
            destination,
            paint
        )

        return result
    }
}

