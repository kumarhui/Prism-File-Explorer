package com.raival.compose.file.explorer.customtools.layout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object IdCardLayoutEngine {

    enum class Layout {
        STACKED,
        TOP_ROW
    }

    const val CARD_WIDTH_MM = 85.60f
    const val CARD_HEIGHT_MM = 53.98f

    /**
     * Creates an A4 sheet with a maximum of 2 ID cards.
     *
     * STACKED:
     *   Card 1
     *   Card 2
     *
     * TOP_ROW:
     *   Card 1    Card 2
     */
    fun createA4Sheet(
        sources: List<Bitmap>,
        layout: Layout = Layout.STACKED
    ): Bitmap {

        require(sources.isNotEmpty()) {
            "At least one image is required"
        }

        val pageWidth = A4Page.WIDTH_PX
        val pageHeight = A4Page.HEIGHT_PX

        val result = Bitmap.createBitmap(
            pageWidth,
            pageHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        val cardWidth = A4Page.mmToPx(CARD_WIDTH_MM)
        val cardHeight = A4Page.mmToPx(CARD_HEIGHT_MM)
        val gap = A4Page.mmToPx(8f)

        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG or
                    Paint.FILTER_BITMAP_FLAG
        )

        sources
            .take(2)
            .forEachIndexed { index, source ->

                val destination = when (layout) {

                    Layout.STACKED -> {

                        val totalHeight =
                            cardHeight * 2f + gap

                        val left =
                            (pageWidth - cardWidth) / 2f

                        val startTop =
                            (pageHeight - totalHeight) / 2f

                        val top =
                            startTop +
                                    index * (cardHeight + gap)

                        RectF(
                            left,
                            top,
                            left + cardWidth,
                            top + cardHeight
                        )
                    }

                    Layout.TOP_ROW -> {

                        val totalWidth =
                            cardWidth * 2f + gap

                        val left =
                            (pageWidth - totalWidth) / 2f

                        // 20 mm from the top of A4
                        val top =
                            A4Page.mmToPx(20f)

                        val x =
                            left +
                                    index * (cardWidth + gap)

                        RectF(
                            x,
                            top,
                            x + cardWidth,
                            top + cardHeight
                        )
                    }
                }

                canvas.drawBitmap(
                    source,
                    null,
                    destination,
                    paint
                )

                // Thin cutting border
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = Color.LTGRAY

                canvas.drawRect(
                    destination,
                    paint
                )

                paint.style = Paint.Style.FILL
            }

        return result
    }

    /**
     * Convenience overload for a single ID card.
     */
    fun createA4Sheet(
        source: Bitmap,
        layout: Layout = Layout.STACKED
    ): Bitmap {
        return createA4Sheet(
            listOf(source),
            layout
        )
    }
}