package com.raival.compose.file.explorer.customtools

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.ProgressBar
import android.widget.TextView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        val activity = context as? FragmentActivity ?: return
        val progress = showProgress(context, "Creating ID card sheets…")

        activity.lifecycleScope.launch {
            val generated = withContext(Dispatchers.IO) {
                val outputs = mutableListOf<String>()
                valid.chunked(2).forEach { group ->
                    val bitmaps = group.mapNotNull { BitmapFactory.decodeFile(it) }
                    if (bitmaps.isEmpty()) return@forEach
                    try {
                        val sheet = IdCardLayoutEngine.createA4Sheet(bitmaps)
                        saveIdCardSheet(context, sheet)?.let(outputs::add)
                        sheet.recycle()
                    } finally {
                        bitmaps.forEach { it.recycle() }
                    }
                }
                outputs
            }

            progress.dismiss()

            if (generated.isNotEmpty()) {
                ResultDialog.showIdCards(
                    context,
                    valid,
                    generated,
                    "ID Card Sheets (${generated.size})"
                )
            } else {
                Toast.makeText(context, "Unable to create ID card sheets", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgress(context: Context, message: String): AlertDialog {
        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(48, 32, 48, 32)
        }
        container.addView(ProgressBar(context))
        container.addView(TextView(context).apply {
            text = message
            textSize = 16f
            setPadding(28, 0, 0, 0)
        })
        return AlertDialog.Builder(context)
            .setView(container)
            .setCancelable(false)
            .create()
            .also {
                it.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                it.show()
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
