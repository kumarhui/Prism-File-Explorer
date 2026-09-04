package com.raival.compose.file.explorer.customtools.preview

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.FileProvider
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.raival.compose.file.explorer.customtools.layout.IdCardLayoutEngine
import com.raival.compose.file.explorer.customtools.sharing.NokoPrintHelper
import java.io.File

class ResultDialog : DialogFragment() {

    private var outputPath = ""
    private var titleText = "Result"

    companion object {

        private const val ARG_OUTPUT = "output"
        private const val ARG_TITLE = "title"
        private const val TAG = "CustomToolsResultDialog"

        fun show(
            context: Context,
            outputPath: String,
            title: String
        ) {

            val activity =
                context as? FragmentActivity
                    ?: return

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

            ResultDialog().apply {

                arguments = Bundle().apply {
                    putString(
                        ARG_OUTPUT,
                        outputPath
                    )

                    putString(
                        ARG_TITLE,
                        title
                    )
                }

            }.show(
                manager,
                TAG
            )
        }

        fun showMultiple(
            context: Context,
            outputPaths: List<String>,
            title: String = "Results"
        ) {
            MultiResultDialog.showMultiple(context, outputPaths, title)
        }

        fun showIdCards(
            context: Context,
            sourcePaths: List<String>,
            outputPaths: List<String>,
            title: String = "ID Card Sheets"
        ) {
            MultiResultDialog.showIdCards(context, sourcePaths, outputPaths, title)
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        outputPath =
            arguments
                ?.getString(ARG_OUTPUT)
                .orEmpty()

        titleText =
            arguments
                ?.getString(ARG_TITLE)
                ?: "Result"
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {

                MaterialTheme {

                    ResultContent(
                        title = titleText,
                        outputPath = outputPath,

                        onDismiss = {
                            dismiss()
                        },

                        onPrint = {
                            printWithNokoPrint()
                        },

                        onSave = {
                            saveToDownloads()
                        },

                        onOpen = {
                            openWithOtherApp()
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

    private fun printWithNokoPrint() {

        if (outputPath.isBlank()) {
            return
        }

        NokoPrintHelper.print(
            requireContext(),
            outputPath
        )
    }

    private fun saveToDownloads() {

        if (outputPath.isBlank()) {
            return
        }

        val source = File(outputPath)

        if (!source.exists()) {
            return
        }

        try {

            val downloads =
                android.os.Environment
                    .getExternalStoragePublicDirectory(
                        android.os.Environment
                            .DIRECTORY_DOWNLOADS
                    )

            if (!downloads.exists()) {
                downloads.mkdirs()
            }

            var destination =
                File(
                    downloads,
                    source.name
                )

            var counter = 1

            while (destination.exists()) {

                destination =
                    File(
                        downloads,
                        "${source.nameWithoutExtension} ($counter).${source.extension}"
                    )

                counter++
            }

            source.copyTo(
                destination,
                overwrite = false
            )

            android.widget.Toast
                .makeText(
                    requireContext(),
                    "Saved to Downloads",
                    android.widget.Toast.LENGTH_SHORT
                )
                .show()

        } catch (_: Exception) {

            android.widget.Toast
                .makeText(
                    requireContext(),
                    "Unable to save file",
                    android.widget.Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    private fun openWithOtherApp() {

        if (outputPath.isBlank()) {
            return
        }

        val file =
            File(outputPath)

        if (!file.exists()) {
            return
        }

        val uri =
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

        val mimeType =
            when {
                file.extension.equals(
                    "pdf",
                    ignoreCase = true
                ) -> "application/pdf"

                else -> "image/*"
            }

        val intent =
            Intent(Intent.ACTION_VIEW).apply {

                setDataAndType(
                    uri,
                    mimeType
                )

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        try {

            startActivity(
                Intent.createChooser(
                    intent,
                    "Open with"
                )
            )

        } catch (_: Exception) {

            android.widget.Toast
                .makeText(
                    requireContext(),
                    "No compatible app found",
                    android.widget.Toast.LENGTH_SHORT
                )
                .show()
        }
    }
}

@Composable
private fun ResultContent(
    title: String,
    outputPath: String,
    onDismiss: () -> Unit,
    onPrint: () -> Unit,
    onSave: () -> Unit,
    onOpen: () -> Unit
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(26.dp),

        tonalElevation = 8.dp,

        shadowElevation = 12.dp
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = title,

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    modifier =
                        Modifier.weight(1f)
                )

                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "Close",

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

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            ResultPreview(
                outputPath = outputPath
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                ResultAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.Print,

                    label = "NokoPrint",

                    onClick = onPrint
                )

                ResultAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.Download,

                    label = "Downloads",

                    onClick = onSave
                )

                ResultAction(
                    modifier =
                        Modifier.weight(1f),

                    icon =
                        Icons.Default.OpenInNew,

                    label = "Open With",

                    onClick = onOpen
                )
            }
        }
    }
}

@Composable
private fun ResultPreview(
    outputPath: String
) {

    val file =
        File(outputPath)

    if (
        !file.exists() ||
        file.extension.equals(
            "pdf",
            ignoreCase = true
        )
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = "PDF document",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        return
    }

    val bitmap =
        BitmapFactory.decodeFile(
            file.absolutePath
        )

    if (bitmap == null) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Text("Preview unavailable")
        }

        return
    }

    Image(
        bitmap = bitmap
            .asImageBitmap(),

        contentDescription = "Result preview",

        modifier =
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(
                    RoundedCornerShape(18.dp)
                ),

        contentScale =
            ContentScale.Fit
    )
}

@Composable
private fun ResultAction(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Surface(
        modifier =
            modifier.clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(16.dp),

        tonalElevation = 2.dp
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 11.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,

                contentDescription = label,

                modifier =
                    Modifier.size(23.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = label,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium
            )
        }
    }
}




/** Public top-level Fragment so FragmentManager can recreate it safely. */
class MultiResultDialog : DialogFragment() {
    private var paths: List<String> = emptyList()
    private var idSourcePaths: List<String> = emptyList()
    private var titleText: String = "Results"
    private var idLayout = IdCardLayoutEngine.Layout.STACKED
    private var pagesContainer: android.widget.LinearLayout? = null
    private var layoutButton: android.widget.Button? = null

    companion object {
        private const val ARG_PATHS = "paths"
        private const val ARG_TITLE = "title"
        private const val ARG_ID_SOURCES = "id_sources"
        private const val ARG_ID_MODE = "id_mode"
        private const val TAG = "CustomToolsMultiResultDialog"

        fun showMultiple(context: Context, outputPaths: List<String>, title: String = "Results") {
            showInternal(context, outputPaths, emptyList(), IdCardLayoutEngine.Layout.STACKED, title)
        }

        fun showIdCards(context: Context, sourcePaths: List<String>, outputPaths: List<String>, title: String = "ID Card Sheets") {
            showInternal(context, outputPaths, sourcePaths, IdCardLayoutEngine.Layout.STACKED, title)
        }

        private fun showInternal(
            context: Context,
            outputPaths: List<String>,
            sourcePaths: List<String>,
            layout: IdCardLayoutEngine.Layout,
            title: String
        ) {
            val activity = context as? FragmentActivity ?: return
            if (activity.isFinishing || activity.isDestroyed || outputPaths.isEmpty()) return
            val manager = activity.supportFragmentManager
            if (manager.isStateSaved || manager.findFragmentByTag(TAG) != null) return
            MultiResultDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PATHS, ArrayList(outputPaths))
                    putStringArrayList(ARG_ID_SOURCES, ArrayList(sourcePaths))
                    putInt(ARG_ID_MODE, layout.ordinal)
                    putString(ARG_TITLE, title)
                }
            }.show(manager, TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paths = arguments?.getStringArrayList(ARG_PATHS)?.toList().orEmpty()
        idSourcePaths = arguments?.getStringArrayList(ARG_ID_SOURCES)?.toList().orEmpty()
        idLayout = IdCardLayoutEngine.Layout.entries.getOrElse(
            arguments?.getInt(ARG_ID_MODE, 0) ?: 0
        ) { IdCardLayoutEngine.Layout.STACKED }
        titleText = arguments?.getString(ARG_TITLE) ?: "Results"
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        val root = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(18, 18, 18, 12)
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        root.addView(android.widget.TextView(context).apply {
            text = titleText
            textSize = 21f
            setTextColor(android.graphics.Color.BLACK)
            setPadding(0, 0, 0, 12)
        })
        val scroll = android.widget.ScrollView(context)
        val pages = android.widget.LinearLayout(context).apply { orientation = android.widget.LinearLayout.VERTICAL }
        pagesContainer = pages
        rebuildPages(context)
        scroll.addView(pages)
        root.addView(scroll, android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        val buttons = android.widget.LinearLayout(context).apply { orientation = android.widget.LinearLayout.HORIZONTAL; gravity = Gravity.END }
        if (idSourcePaths.isNotEmpty()) {
            layoutButton = android.widget.Button(context).apply {
                text = if (idLayout == IdCardLayoutEngine.Layout.STACKED) "Top Row Layout" else "Stacked Layout"
                setOnClickListener {
                    idLayout = if (idLayout == IdCardLayoutEngine.Layout.STACKED) IdCardLayoutEngine.Layout.TOP_ROW else IdCardLayoutEngine.Layout.STACKED
                    text = if (idLayout == IdCardLayoutEngine.Layout.STACKED) "Top Row Layout" else "Stacked Layout"
                    rebuildIdCardPages(context)
                }
            }
            buttons.addView(layoutButton)
        }
        buttons.addView(android.widget.Button(context).apply {
            text = "Print All"
            setOnClickListener { NokoPrintHelper.printMultiple(context, paths) }
        })
        buttons.addView(android.widget.Button(context).apply {
            text = "Close"
            setOnClickListener { dismiss() }
        })
        root.addView(buttons)
        return root
    }

    private fun rebuildPages(context: Context) {
        val pages = pagesContainer ?: return
        pages.removeAllViews()

        paths.forEachIndexed { index, path ->
            val file = File(path)
            if (!file.exists() || !file.isFile) return@forEachIndexed

            pages.addView(android.widget.TextView(context).apply {
                text = "Page ${index + 1} of ${paths.size}"
                textSize = 14f
                setTextColor(android.graphics.Color.DKGRAY)
                setPadding(0, 6, 0, 6)
            })

            val image = android.widget.ImageView(context).apply {
                setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
            }

            pages.addView(
                image,
                android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 14
                }
            )
        }
    }

    private fun rebuildIdCardPages(context: Context) {
        val activity = context as? FragmentActivity ?: return
        if (idSourcePaths.isEmpty()) return

        layoutButton?.isEnabled = false

        activity.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val outputs = mutableListOf<String>()

                idSourcePaths.chunked(2).forEach { group ->
                    val bitmaps = group.mapNotNull { path ->
                        BitmapFactory.decodeFile(path)
                    }

                    if (bitmaps.isEmpty()) return@forEach

                    try {
                        val sheet = IdCardLayoutEngine.createA4Sheet(bitmaps, idLayout)
                        val directory = File(context.cacheDir, "custom_tools")

                        if (!directory.exists() && !directory.mkdirs()) {
                            return@forEach
                        }

                        val file = File(
                            directory,
                            "id_cards_${System.currentTimeMillis()}_${System.nanoTime()}.png"
                        )

                        file.outputStream().use { output ->
                            sheet.compress(
                                android.graphics.Bitmap.CompressFormat.PNG,
                                100,
                                output
                            )
                        }

                        outputs.add(file.absolutePath)
                        sheet.recycle()
                    } finally {
                        bitmaps.forEach { bitmap ->
                            if (!bitmap.isRecycled) bitmap.recycle()
                        }
                    }
                }

                outputs
            }

            if (result.isNotEmpty()) {
                paths = result
                rebuildPages(context)
            }

            layoutButton?.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.94f).toInt(),
                (resources.displayMetrics.heightPixels * 0.90f).toInt()
            )
            window.setGravity(Gravity.CENTER)
        }
    }
}
