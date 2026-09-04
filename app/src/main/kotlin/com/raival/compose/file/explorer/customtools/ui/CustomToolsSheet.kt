package com.raival.compose.file.explorer.customtools.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.customtools.CustomToolRunner
import com.raival.compose.file.explorer.customtools.crop.CropImageTool
import com.raival.compose.file.explorer.customtools.sharing.JuganuaHelper
import com.raival.compose.file.explorer.customtools.sharing.NokoPrintHelper
import com.raival.compose.file.explorer.customtools.sharing.WhatsAppHelper
import com.raival.compose.file.explorer.customtools.preview.ResultDialog

class CustomToolsDialogFragment : DialogFragment() {

    private var imagePath: String = ""

    companion object {

        private const val ARG_IMAGE_PATH = "image_path"
        private const val TAG = "CustomToolsDialog"

        fun show(
            activity: FragmentActivity,
            imagePath: String
        ) {
            if (
                activity.isFinishing ||
                activity.isDestroyed
            ) {
                return
            }

            val manager = activity.supportFragmentManager

            if (
                manager.isStateSaved ||
                manager.findFragmentByTag(TAG) != null
            ) {
                return
            }

            CustomToolsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        ARG_IMAGE_PATH,
                        imagePath
                    )
                }
            }.show(manager, TAG)
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        imagePath =
            arguments
                ?.getString(ARG_IMAGE_PATH)
                .orEmpty()

        // IMPORTANT:
        // Do not call requestFeature() or setStyle()
        // here. This avoids the previous DialogFragment
        // "requestFeature() must be called before adding content"
        // crash.
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {

                MaterialTheme {

                    CustomToolsContent(
                        onDismiss = {
                            dismiss()
                        },

                        onToolClick = { toolId ->
                            handleToolClick(toolId)
                        }
                    )
                }
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
                android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            window.setDimAmount(0.55f)

            window.setGravity(
                Gravity.BOTTOM
            )

            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun handleToolClick(
        toolId: ToolId
    ) {

        if (imagePath.isBlank()) {
            return
        }

        when (toolId) {

            ToolId.ID_CARD -> {

                val output =
                    CustomToolRunner.createIdCard(
                        requireContext(),
                        imagePath
                    )

                if (output != null) {

                    dismiss()

                    ResultDialog.show(
                        requireContext(),
                        output,
                        "ID Card"
                    )
                }
            }

            ToolId.PASSPORT_PHOTO ->
                CropImageTool.show(
                    context = requireContext(),
                    imagePath = imagePath,
                    continueToPassport = true
                )
            ToolId.JUGANUA -> {

                val success =
                    JuganuaHelper.open(
                        requireContext(),
                        imagePath
                    )

                if (success) {
                    dismiss()
                }
            }

            ToolId.COMPRESS_IMAGE -> {

                dismiss()

                ImageCompressionDialog.show(
                    requireContext(),
                    imagePath
                )
            }

            ToolId.CROP_IMAGE -> {

                dismiss()

                CropImageTool.show(
                    requireContext(),
                    imagePath
                )
            }
            ToolId.CONVERT_PDF -> {

                val result =
                    CustomToolRunner.convertPdf(
                        requireContext(),
                        imagePath
                    )

                if (result.success &&
                    result.outputPath != null
                ) {

                    dismiss()

                    ResultDialog.show(
                        requireContext(),
                        result.outputPath,
                        "PDF"
                    )
                }
            }

            ToolId.IMAGE_INFO -> {

                ImageInfoDialog.show(
                    requireContext(),
                    imagePath
                )
            }

            ToolId.WHATSAPP -> {

                val success =
                    WhatsAppHelper.share(
                        requireContext(),
                        imagePath
                    )

                if (success) {
                    dismiss()
                }
            }

            ToolId.NOKOPRINT -> {

                val success =
                    NokoPrintHelper.print(
                        requireContext(),
                        imagePath
                    )

                if (success) {
                    dismiss()
                }
            }
        }
    }
}

object CustomToolsSheet {

    fun show(
        context: android.content.Context,
        imagePath: String
    ) {

        val activity =
            context as? FragmentActivity
                ?: return

        CustomToolsDialogFragment.show(
            activity,
            imagePath
        )
    }
}

@Composable
private fun CustomToolsContent(
    onDismiss: () -> Unit,
    onToolClick: (ToolId) -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),

        tonalElevation = 8.dp,

        shadowElevation = 12.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 18.dp
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 2.dp,
                        bottom = 12.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "Custom Tools",
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text = "Choose an action",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",

                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .clickable {
                                onDismiss()
                            }
                            .padding(9.dp)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp),

                userScrollEnabled = false
            ) {

                items(tools) { item ->

                    ToolCard(
                        item = item,

                        onClick = {
                            onToolClick(item.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    item: CustomToolsItem,
    onClick: () -> Unit
) {

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(16.dp)
                )
                .clickable(
                    onClick = onClick
                ),

        shape =
            RoundedCornerShape(16.dp),

        tonalElevation = 2.dp,

        shadowElevation = 1.dp
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 11.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    painter =
                        painterResource(
                            item.iconRes
                        ),

                    contentDescription =
                        item.title,

                    modifier =
                        Modifier.size(23.dp),

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )
            }

            Spacer(
                modifier = Modifier.size(10.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text = item.title,

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text = item.subtitle,

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines = 1
                )
            }
        }
    }
}

private val tools = listOf(

    CustomToolsItem(
        ToolId.ID_CARD,
        "ID Card",
        "85.6 × 54 mm on A4",
        R.drawable.ic_tool_id_card
    ),

    CustomToolsItem(
        ToolId.PASSPORT_PHOTO,
        "Passport Photo",
        "30 × 40 mm • 6 columns",
        R.drawable.ic_tool_passport
    ),

    CustomToolsItem(
        ToolId.JUGANUA,
        "Open in Juganua",
        "Open image in Juganua",
        R.drawable.ic_tool_juganua
    ),

    CustomToolsItem(
        ToolId.COMPRESS_IMAGE,
        "Compress Image",
        "Reduce image file size",
        R.drawable.ic_tool_compress
    ),

    CustomToolsItem(
        ToolId.CROP_IMAGE,
        "Crop Image",
        "Crop and straighten image",
        R.drawable.ic_tool_crop
    ),

    CustomToolsItem(
        ToolId.CONVERT_PDF,
        "Convert to PDF",
        "Create PDF",
        R.drawable.ic_tool_pdf
    ),

    CustomToolsItem(
        ToolId.IMAGE_INFO,
        "Information",
        "Size • resolution • date",
        R.drawable.ic_tool_info
    ),

    CustomToolsItem(
        ToolId.WHATSAPP,
        "WhatsApp",
        "Share image",
        R.drawable.ic_tool_whatsapp
    ),

    CustomToolsItem(
        ToolId.NOKOPRINT,
        "NokoPrint",
        "Print image",
        R.drawable.ic_tool_print
    )
)



