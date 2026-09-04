package com.raival.compose.file.explorer.customtools.ui

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.raival.compose.file.explorer.customtools.conversion.ImageConverter
import com.raival.compose.file.explorer.customtools.conversion.PdfConverter
import com.raival.compose.file.explorer.customtools.preview.ResultDialog
import java.io.File

class ImageConversionDialog : DialogFragment() {

    private var imagePath = ""

    companion object {
        private const val ARG_IMAGE_PATH = "image_path"
        private const val TAG = "ImageConversionDialog"

        fun show(
            context: Context,
            imagePath: String
        ) {
            val activity = context as? FragmentActivity ?: return

            if (activity.isFinishing || activity.isDestroyed) return

            val manager = activity.supportFragmentManager

            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) {
                return
            }

            ImageConversionDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_PATH, imagePath)
                }
            }.show(manager, TAG)
        }

        fun showPdf(
            context: Context,
            imagePaths: List<String>
        ) {
            val validPaths = imagePaths.filter {
                val file = File(it)
                file.exists() && file.isFile
            }

            if (validPaths.isEmpty()) {
                Toast.makeText(
                    context,
                    "No valid files selected",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // PdfConverter currently exposes single-image conversion.
            // Keep the conversion path safe and deterministic here.
            val source = validPaths.first()

            try {
                val result = PdfConverter.convert(context, source)

                if (result.success && !result.outputPath.isNullOrBlank()) {
                    ResultDialog.show(
                        context,
                        result.outputPath,
                        if (validPaths.size == 1) {
                            "Converted PDF"
                        } else {
                            "Converted PDF (first selected image)"
                        }
                    )
                } else {
                    Toast.makeText(
                        context,
                        result.error ?: "PDF conversion failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: "Unable to convert to PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun showImage(
            context: Context,
            imagePaths: List<String>
        ) {
            val validPaths = imagePaths.filter {
                val file = File(it)
                file.exists() && file.isFile
            }

            if (validPaths.isEmpty()) {
                Toast.makeText(
                    context,
                    "No valid files selected",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            show(context, validPaths.first())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePath = arguments?.getString(ARG_IMAGE_PATH).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ConversionContent(
                        onDismiss = { dismiss() },
                        onConvert = { format -> convertImage(format) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(0.55f)
            window.setGravity(Gravity.CENTER)
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.90f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun convertImage(format: ImageConverter.Format) {
        if (imagePath.isBlank()) return

        try {
            val result = ImageConverter.convert(
                context = requireContext(),
                inputPath = imagePath,
                format = format,
                quality = 95
            )

            if (result.success) {
                val outputPath = result.outputPath

                if (!outputPath.isNullOrBlank()) {
                    dismiss()

                    ResultDialog.show(
                        requireContext(),
                        outputPath,
                        "Converted Image"
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    result.error ?: "Conversion failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                e.message ?: "Unable to convert image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

private data class ImageFormatOption(
    val title: String,
    val subtitle: String,
    val format: ImageConverter.Format
)

private val imageFormatOptions = listOf(
    ImageFormatOption(
        title = "JPEG",
        subtitle = "Best compatibility",
        format = ImageConverter.Format.JPEG
    ),
    ImageFormatOption(
        title = "PNG",
        subtitle = "Lossless quality",
        format = ImageConverter.Format.PNG
    ),
    ImageFormatOption(
        title = "WEBP",
        subtitle = "Smaller file size",
        format = ImageConverter.Format.WEBP
    )
)

@Composable
private fun ConversionContent(
    onDismiss: () -> Unit,
    onConvert: (ImageConverter.Format) -> Unit
) {
    var selected by remember {
        mutableStateOf(ImageConverter.Format.JPEG)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Convert Image",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.padding(2.dp))

            Text(
                text = "Choose output format",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.padding(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(imageFormatOptions) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = option.format }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == option.format,
                            onClick = { selected = option.format }
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        ) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Convert",
                    modifier = Modifier
                        .clickable { onConvert(selected) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
