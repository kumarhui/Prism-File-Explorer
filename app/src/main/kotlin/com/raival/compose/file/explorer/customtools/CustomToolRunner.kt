package com.raival.compose.file.explorer.customtools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.raival.compose.file.explorer.customtools.conversion.PdfConverter
import com.raival.compose.file.explorer.customtools.layout.IdCardLayoutEngine
import com.raival.compose.file.explorer.customtools.preview.ResultDialog
import java.io.File

object CustomToolRunner {

    fun createIdCard(context: Context, imagePath: String): String? {
        val sourceFile = File(imagePath)
        if (!sourceFile.exists() || !sourceFile.isFile) return null

        val source = BitmapFactory.decodeFile(sourceFile.absolutePath)
            ?: return null

        return try {
            val output = IdCardLayoutEngine.createA4Sheet(source)
            saveIdCardSheet(context, output)
        } finally {
            source.recycle()
        }
    }

    fun createIdCards(context: Context, imagePaths: List<String>) {
        val valid = imagePaths.filter {
            val f = File(it)
            f.exists() && f.isFile
        }

        if (valid.isEmpty()) {
            Toast.makeText(context, "No valid images selected", Toast.LENGTH_SHORT).show()
            return
        }

        val generated = mutableListOf<String>()

        valid.chunked(3).forEach { group ->
            val bitmaps = group.mapNotNull { BitmapFactory.decodeFile(it) }
            if (bitmaps.isEmpty()) return@forEach

            try {
                val sheet = IdCardLayoutEngine.createA4Sheet(bitmaps)
                saveIdCardSheet(context, sheet)?.let(generated::add)
                sheet.recycle()
            } finally {
                bitmaps.forEach { it.recycle() }
            }
        }

        if (generated.isNotEmpty()) {
            ResultDialog.show(
                context,
                generated.first(),
                if (generated.size == 1)
                    "ID Card Sheet"
                else
                    "Created ${generated.size} ID Card Sheets"
            )
        }
    }

    private fun saveIdCardSheet(context: Context, bitmap: Bitmap): String? {
        val directory = File(context.cacheDir, "custom_tools")
        if (!directory.exists() && !directory.mkdirs()) return null

        val file = File(
            directory,
            "id_cards_${System.currentTimeMillis()}_${System.nanoTime()}.png"
        )

        return try {
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun convertPdf(context: Context, imagePath: String) =
        PdfConverter.convert(context, imagePath)
}
