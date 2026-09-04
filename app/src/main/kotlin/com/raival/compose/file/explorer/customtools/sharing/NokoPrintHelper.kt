package com.raival.compose.file.explorer.customtools.sharing

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object NokoPrintHelper {

    private const val PACKAGE =
        "com.nokoprint"

    fun printMultiple(context: Context, paths: List<String>): Boolean {
        val files = paths
            .map { File(it) }
            .filter { it.exists() && it.isFile }

        if (files.isEmpty()) return false

        return try {
            val uris = ArrayList(
                files.map {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        it
                    )
                }
            )

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(PACKAGE)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    fun print(
        context: Context,
        path: String
    ): Boolean {

        val file = File(path)

        if (!file.exists()) {
            return false
        }

        return try {

            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

            val mimeType =
                when {
                    path.endsWith(
                        ".pdf",
                        true
                    ) ->
                        "application/pdf"

                    path.endsWith(
                        ".png",
                        true
                    ) ->
                        "image/png"

                    path.endsWith(
                        ".webp",
                        true
                    ) ->
                        "image/webp"

                    else ->
                        "image/jpeg"
                }

            val intent =
                Intent(Intent.ACTION_SEND).apply {

                    type = mimeType

                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    setPackage(PACKAGE)
                }

            context.startActivity(intent)

            true

        } catch (e: Exception) {
            false
        }
    }
}

