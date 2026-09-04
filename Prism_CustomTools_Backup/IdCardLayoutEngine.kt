package com.raival.compose.file.explorer.customtools.layout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object IdCardLayoutEngine {

    const val CARD_WIDTH_MM = 85.60f
    const val CARD_HEIGHT_MM = 53.98f

    /** Exactly two ID cards per A4 sheet. */
    fun createA4Sheet(sources: List<Bitmap>): Bitmap {
        require(sources.isNotEmpty()) { "At least one image is required" }

        val pageWidth = A4Page.WIDTH_PX
        val pageHeight = A4Page.HEIGHT_PX
        val result = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        val cardWidth = A4Page.mmToPx(CARD_WIDTH_MM)
        val cardHeight = A4Page.mmToPx(CARD_HEIGHT_MM)
        val gap = A4Page.mmToPx(8f)
        val totalHeight = cardHeight * 2f + gap
        val left = (pageWidth - cardWidth) / 2f
        val startTop = (pageHeight - totalHeight) / 2f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        sources.take(2).forEachIndexed { index, source ->
            val top = startTop + index * (cardHeight + gap)
            val destination = RectF(left, top, left + cardWidth, top + cardHeight)

            canvas.drawBitmap(source, null, destination, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = Color.LTGRAY
            canvas.drawRect(destination, paint)
            paint.style = Paint.Style.FILL
        }

        return result
    }

    fun createA4Sheet(source: Bitmap): Bitmap = createA4Sheet(listOf(source))
}
