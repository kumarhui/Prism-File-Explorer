package com.raival.compose.file.explorer.customtools.preview

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.raival.compose.file.explorer.customtools.conversion.PrintPdfCombiner
import com.raival.compose.file.explorer.customtools.sharing.NokoPrintHelper
import java.io.File

class PrintPreviewDialog : DialogFragment() {

    private var filePath = ""

    companion object {

        private const val ARG_FILE_PATH = "file_path"
        private const val ARG_FILE_PATHS = "file_paths"
        private const val TAG = "CustomToolsPrintPreview"

        fun show(
            context: Context,
            filePath: String
        ) {

            val activity =
                context as? FragmentActivity ?: return

            if (
                activity.isFinishing ||
                activity.isDestroyed
            ) {
                return
            }

            val manager =
                activity.supportFragmentManager

            if (
                manager.isStateSaved ||
                manager.findFragmentByTag(TAG) != null
            ) {
                return
            }

            PrintPreviewDialog().apply {

                arguments = android.os.Bundle().apply {
                    putString(
                        ARG_FILE_PATH,
                        filePath
                    )
                }

            }.show(
                manager,
                TAG
            )
        }

        fun showMultiple(
            context: Context,
            filePaths: List<String>
        ) {
            if (context !is FragmentActivity) return

            PrintPreviewDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(
                        ARG_FILE_PATHS,
                        ArrayList(filePaths)
                    )
                }
            }.show(
                context.supportFragmentManager,
                "CustomToolsPrintPreviewMultiple"
            )
        }
    }

    override fun onCreate(
        savedInstanceState: android.os.Bundle?
    ) {
        super.onCreate(savedInstanceState)

        filePath =
            arguments
                ?.getString(ARG_FILE_PATH)
                .orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val singlePath = arguments?.getString(ARG_FILE_PATH)

        val multiplePaths = arguments
            ?.getStringArrayList(ARG_FILE_PATHS)
            ?.toList()
            ?: emptyList()

        val filePaths = when {
            multiplePaths.isNotEmpty() -> multiplePaths
            !singlePath.isNullOrEmpty() -> listOf(singlePath)
            else -> emptyList()
        }

        return ComposeView(requireContext()).apply {
            setContent {
                PrintPreviewContent(
                    filePaths = filePaths,
                    onDismiss = {
                        dismiss()
                    },
                    onPrint = {
                        val context = requireContext()

                        if (filePaths.size == 1) {

                            val success = NokoPrintHelper.print(
                                context,
                                filePaths.first()
                            )

                            if (success) {
                                dismiss()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Unable to open NokoPrint",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } else {

                            val outputDir = File(
                                context.cacheDir,
                                "print"
                            )

                            if (!outputDir.exists()) {
                                outputDir.mkdirs()
                            }

                            val outputFile = File(
                                outputDir,
                                "Combined_Print_${System.currentTimeMillis()}.pdf"
                            )

                            val combined = PrintPdfCombiner.combine(
                                filePaths = filePaths,
                                outputFile = outputFile
                            )

                            if (combined) {

                                val success = NokoPrintHelper.print(
                                    context,
                                    outputFile.absolutePath
                                )

                                if (success) {
                                    dismiss()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Unable to open NokoPrint",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {

                                Toast.makeText(
                                    context,
                                    "Unable to create combined PDF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onStart() {

        super.onStart()

        dialog?.window?.let { window ->

            window.setBackgroundDrawableResource(
                android.R.color.transparent
            )

            window.addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            window.setDimAmount(0.55f)

            window.setGravity(
                Gravity.CENTER
            )

            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.94f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun printFile() {

        if (filePath.isBlank()) {
            return
        }

        val success =
            NokoPrintHelper.print(
                requireContext(),
                filePath
            )

        if (success) {
            dismiss()
        } else {
            android.widget.Toast
                .makeText(
                    requireContext(),
                    "Unable to open NokoPrint",
                    android.widget.Toast.LENGTH_SHORT
                )
                .show()
        }
    }
}

@Composable
private fun PrintPreviewContent(
    filePaths: List<String>,
    onDismiss: () -> Unit,
    onPrint: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (filePaths.size == 1)
                    "Print Preview"
                else
                    "Print ${filePaths.size} Files",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable { onDismiss() }
                    .padding(9.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(filePaths) { path ->
                PrintFilePreviewItem(path)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onPrint,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                if (filePaths.size == 1)
                    "Print"
                else
                    "Print All (${filePaths.size})"
            )
        }
    }
}

@Composable
private fun PrintFilePreviewItem(path: String) {
    val file = File(path)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (
                file.extension.lowercase() in
                setOf("jpg", "jpeg", "png", "webp")
            ) {
                val bitmap = remember(path) {
                    BitmapFactory.decodeFile(path)
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = file.name,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (file.extension.lowercase() == "pdf")
                        "PDF"
                    else
                        "Image",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FilePreview(
    file: File
) {

    val isPdf =
        file.extension.equals(
            "pdf",
            ignoreCase = true
        )

    if (isPdf) {

        PdfPreview(file)

    } else {

        ImagePreview(file)
    }
}

@Composable
private fun ImagePreview(
    file: File
) {

    val bitmap = remember(file.absolutePath) {
        BitmapFactory.decodeFile(
            file.absolutePath
        )
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = file.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                ),
            contentScale = ContentScale.Fit
        )

    } else {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {

            Text("Unable to preview image")
        }
    }
}

@Composable
private fun PdfPreview(
    file: File
) {

    val bitmap = remember(file.absolutePath) {

        try {

            val descriptor =
                ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )

            val renderer =
                PdfRenderer(descriptor)

            if (renderer.pageCount <= 0) {
                renderer.close()
                descriptor.close()
                null
            } else {

                val page =
                    renderer.openPage(0)

                val width =
                    page.width * 2

                val height =
                    page.height * 2

                val bitmap =
                    android.graphics.Bitmap.createBitmap(
                        width,
                        height,
                        android.graphics.Bitmap.Config.ARGB_8888
                    )

                bitmap.eraseColor(
                    android.graphics.Color.WHITE
                )

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                page.close()
                renderer.close()
                descriptor.close()

                bitmap
            }

        } catch (_: Exception) {

            null
        }
    }

    if (bitmap != null) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = file.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                ),
            contentScale = ContentScale.Fit
        )

    } else {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {

            Text("Unable to preview PDF")
        }
    }
}