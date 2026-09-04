package com.raival.compose.file.explorer.customtools.conversion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

object PdfPageExtractor {

    enum class Destination {
        SAME_FOLDER,
        NEW_FOLDER
    }

    data class Result(
        val success: Boolean,
        val outputDirectory: File?,
        val pageFiles: List<File>,
        val error: String? = null
    )

    fun extract(
        context: Context,
        pdfPath: String,
        destination: Destination,
        newFolderName: String? = null
    ): Result {
        val pdfFile = File(pdfPath)

        if (!pdfFile.exists() || !pdfFile.isFile) {
            return Result(false, null, emptyList(), "PDF file not found")
        }

        if (!pdfFile.extension.equals("pdf", ignoreCase = true)) {
            return Result(false, null, emptyList(), "Selected file is not a PDF")
        }

        val parent = pdfFile.parentFile
            ?: return Result(false, null, emptyList(), "PDF parent folder not found")

        val outputDirectory = when (destination) {
            Destination.SAME_FOLDER -> parent
            Destination.NEW_FOLDER -> {
                val safeName = newFolderName
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: "${pdfFile.nameWithoutExtension}_pages"

                File(parent, safeName).apply {
                    if (!exists()) mkdirs()
                }
            }
        }

        return try {
            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            ).use { descriptor ->

                PdfRenderer(descriptor).use { renderer ->
                    val output = mutableListOf<File>()

                    for (index in 0 until renderer.pageCount) {
                        renderer.openPage(index).use { page ->
                            val width = (page.width * 2).coerceAtLeast(1)
                            val height = (page.height * 2).coerceAtLeast(1)

                            val bitmap = Bitmap.createBitmap(
                                width,
                                height,
                                Bitmap.Config.ARGB_8888
                            )

                            bitmap.eraseColor(android.graphics.Color.WHITE)

                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )

                            val file = File(
                                outputDirectory,
                                "page_${(index + 1).toString().padStart(3, '0')}.png"
                            )

                            file.outputStream().use { stream ->
                                bitmap.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    stream
                                )
                            }

                            bitmap.recycle()
                            output += file
                        }
                    }

                    Result(
                        success = true,
                        outputDirectory = outputDirectory,
                        pageFiles = output
                    )
                }
            }
        } catch (e: Exception) {
            Result(
                success = false,
                outputDirectory = outputDirectory,
                pageFiles = emptyList(),
                error = e.message ?: "Unable to extract PDF pages"
            )
        }
    }
}
