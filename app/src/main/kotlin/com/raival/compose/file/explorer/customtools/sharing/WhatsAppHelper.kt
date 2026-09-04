package com.raival.compose.file.explorer.customtools.sharing

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object WhatsAppHelper {

    fun share(
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

            val intent =
                Intent(Intent.ACTION_SEND).apply {

                    type = "image/*"

                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    setPackage(
                        "com.whatsapp"
                    )
                }

            context.startActivity(intent)

            true

        } catch (e: Exception) {
            false
        }
    }

    fun shareMultiple(
        context: Context,
        paths: List<String>
    ): Boolean {

        val files = paths
            .map { File(it) }
            .filter { it.exists() && it.isFile }

        if (files.isEmpty()) {
            return false
        }

        return try {

            val uris = ArrayList(
                files.map { file ->
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }
            )

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                type = "*/*"

                putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    uris
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                setPackage(
                    "com.whatsapp"
                )
            }

            context.startActivity(intent)

            true

        } catch (e: Exception) {

            false
        }
    }
}

