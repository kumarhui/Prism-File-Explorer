package com.raival.compose.file.explorer.customtools.preview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Send
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
import com.raival.compose.file.explorer.customtools.sharing.WhatsAppHelper
import java.io.File
import android.graphics.BitmapFactory

class WhatsAppPreviewDialog : DialogFragment() {

    companion object {

        private const val ARG_FILE_PATHS = "file_paths"

        fun show(
            context: Context,
            filePaths: List<String>
        ) {
            val activity = context as? FragmentActivity ?: return

            if (
                activity.isFinishing ||
                activity.isDestroyed ||
                filePaths.isEmpty()
            ) {
                return
            }

            WhatsAppPreviewDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(
                        ARG_FILE_PATHS,
                        ArrayList(filePaths)
                    )
                }
            }.show(
                activity.supportFragmentManager,
                "CustomToolsWhatsAppPreview"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val filePaths = arguments
            ?.getStringArrayList(ARG_FILE_PATHS)
            ?.toList()
            ?: emptyList()

        return ComposeView(requireContext()).apply {

            setContent {

                WhatsAppPreviewContent(
                    filePaths = filePaths,
                    onDismiss = {
                        dismiss()
                    },
                    onSend = {

                        val success = if (filePaths.size == 1) {

                            WhatsAppHelper.share(
                                requireContext(),
                                filePaths.first()
                            )

                        } else {

                            WhatsAppHelper.shareMultiple(
                                requireContext(),
                                filePaths
                            )
                        }

                        if (success) {
                            dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Unable to open WhatsApp",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WhatsAppPreviewContent(
    filePaths: List<String>,
    onDismiss: () -> Unit,
    onSend: () -> Unit
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
                text = if (filePaths.size == 1) {
                    "Send to WhatsApp"
                } else {
                    "Send ${filePaths.size} Files"
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        onDismiss()
                    }
                    .padding(9.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {

            items(
                items = filePaths,
                key = { it }
            ) { path ->

                WhatsAppFileItem(path)
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = onSend,
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                if (filePaths.size == 1) {
                    "Send to WhatsApp"
                } else {
                    "Send All (${filePaths.size})"
                }
            )
        }
    }
}

@Composable
private fun WhatsAppFileItem(
    path: String
) {

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

            val bitmap = remember(path) {
                BitmapFactory.decodeFile(path)
            }

            if (bitmap != null) {

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = file.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(
                            RoundedCornerShape(8.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}