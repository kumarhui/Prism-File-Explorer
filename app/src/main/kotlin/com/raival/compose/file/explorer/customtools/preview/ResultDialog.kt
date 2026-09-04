package com.raival.compose.file.explorer.customtools.preview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.raival.compose.file.explorer.customtools.layout.A4Page
import com.raival.compose.file.explorer.customtools.layout.IdCardLayoutEngine
import com.raival.compose.file.explorer.customtools.sharing.NokoPrintHelper
import com.raival.compose.file.explorer.customtools.storage.DownloadStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.activity.compose.LocalActivity

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

            ResultDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_OUTPUT, outputPath)
                    putString(ARG_TITLE, title)
                }
            }.show(manager, TAG)
        }

        fun showMultiple(
            context: Context,
            outputPaths: List<String>,
            title: String = "Results"
        ) {
            MultiResultDialog.showMultiple(
                context,
                outputPaths,
                title
            )
        }

        fun showIdCards(
            context: Context,
            sourcePaths: List<String>,
            outputPaths: List<String>,
            title: String = "ID Card Sheets"
        ) {
            MultiResultDialog.showIdCards(
                context,
                sourcePaths,
                outputPaths,
                title
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {

        return ComposeView(requireContext()).apply {

            setContent {

                MaterialTheme {

                    SingleResultContent(
                        title = titleText,
                        outputPath = outputPath,

                        onDismiss = {
                            dismiss()
                        },

                        onPrint = {
                            NokoPrintHelper.print(
                                requireContext(),
                                outputPath
                            )
                        },

                        onSave = {
                            saveFile(outputPath)
                        },

                        onShare = {
                            shareFile(outputPath)
                        },

                        onOpen = {
                            openFile(outputPath)
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

            window.setDimAmount(0.60f)

            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.96f).toInt(),
                (resources.displayMetrics.heightPixels * 0.92f).toInt()
            )
        }
    }

    private fun saveFile(path: String) {

        val file = File(path)

        if (!file.exists()) {
            return
        }

        try {

            val sourceFile = File(path)

            val savedPath =
                DownloadStorageHelper.saveToDownloads(
                    requireContext(),
                    path,
                    sourceFile.name,
                    when {
                        sourceFile.extension.equals("pdf", ignoreCase = true) ->
                            "application/pdf"

                        else ->
                            "image/*"
                    }
                )

            Toast.makeText(
                requireContext(),
                if (savedPath != null)
                    "Saved to Downloads"
                else
                    "Unable to save file",
                Toast.LENGTH_SHORT
            ).show()

        } catch (_: Exception) {

            Toast.makeText(
                requireContext(),
                "Unable to save file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareFile(path: String) {

        val file = File(path)

        if (!file.exists()) {
            return
        }

        try {

            val uri =
                androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

            val intent =
                Intent(Intent.ACTION_SEND).apply {

                    type = when {
                        file.extension.equals(
                            "pdf",
                            ignoreCase = true
                        ) -> "application/pdf"

                        else -> "image/*"
                    }

                    putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Share result"
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                requireContext(),
                "Unable to share file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openFile(path: String) {

        val file = File(path)

        if (!file.exists()) {
            return
        }

        try {

            val uri =
                androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

            val mime =
                if (
                    file.extension.equals(
                        "pdf",
                        ignoreCase = true
                    )
                ) {
                    "application/pdf"
                } else {
                    "image/*"
                }

            val intent =
                Intent(Intent.ACTION_VIEW).apply {

                    setDataAndType(
                        uri,
                        mime
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Open with"
                )
            )

        } catch (_: Exception) {

            Toast.makeText(
                requireContext(),
                "No compatible app found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


/* ============================================================
   SINGLE RESULT
   ============================================================ */

@Composable
private fun SingleResultContent(
    title: String,
    outputPath: String,
    onDismiss: () -> Unit,
    onPrint: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            ResultTopBar(
                title = title,
                subtitle = "Created successfully",
                onBack = onDismiss,
                showBack = false
            )

            ResultImage(
                path = outputPath,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 8.dp
                    )
            )

            ResultBottomBar(
                onSave = onSave,
                onShare = onShare,
                onPrint = onPrint,
                onOpen = onOpen
            )
        }
    }
}


/* ============================================================
   MULTI RESULT
   ============================================================ */

class MultiResultDialog : DialogFragment() {

    private var paths: List<String> = emptyList()
    private var idSourcePaths: List<String> = emptyList()
    private var titleText = "Results"

    private var idLayout =
        IdCardLayoutEngine.Layout.STACKED

    companion object {

        private const val ARG_PATHS = "paths"
        private const val ARG_TITLE = "title"
        private const val ARG_ID_SOURCES = "id_sources"
        private const val ARG_ID_LAYOUT = "id_layout"

        private const val TAG =
            "CustomToolsMultiResultDialog"

        fun showMultiple(
            context: Context,
            outputPaths: List<String>,
            title: String = "Results"
        ) {
            showInternal(
                context = context,
                outputPaths = outputPaths,
                sourcePaths = emptyList(),
                layout = IdCardLayoutEngine.Layout.STACKED,
                title = title
            )
        }

        fun showIdCards(
            context: Context,
            sourcePaths: List<String>,
            outputPaths: List<String>,
            title: String = "ID Card Sheets"
        ) {
            showInternal(
                context = context,
                outputPaths = outputPaths,
                sourcePaths = sourcePaths,
                layout = IdCardLayoutEngine.Layout.STACKED,
                title = title
            )
        }

        private fun showInternal(
            context: Context,
            outputPaths: List<String>,
            sourcePaths: List<String>,
            layout: IdCardLayoutEngine.Layout,
            title: String
        ) {

            val activity =
                context as? FragmentActivity ?: return

            if (
                activity.isFinishing ||
                activity.isDestroyed ||
                outputPaths.isEmpty()
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

            MultiResultDialog().apply {

                arguments = Bundle().apply {

                    putStringArrayList(
                        ARG_PATHS,
                        ArrayList(outputPaths)
                    )

                    putStringArrayList(
                        ARG_ID_SOURCES,
                        ArrayList(sourcePaths)
                    )

                    putInt(
                        ARG_ID_LAYOUT,
                        layout.ordinal
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
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        paths =
            arguments
                ?.getStringArrayList(ARG_PATHS)
                ?.toList()
                .orEmpty()

        idSourcePaths =
            arguments
                ?.getStringArrayList(ARG_ID_SOURCES)
                ?.toList()
                .orEmpty()

        val layoutIndex =
            arguments
                ?.getInt(ARG_ID_LAYOUT, 0)
                ?: 0

        idLayout =
            IdCardLayoutEngine.Layout.entries.getOrElse(
                layoutIndex
            ) {
                IdCardLayoutEngine.Layout.STACKED
            }

        titleText =
            arguments
                ?.getString(ARG_TITLE)
                ?: "Results"
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {

        return ComposeView(requireContext()).apply {

            setContent {

                MaterialTheme {

                    MultiResultContent(
                        title = titleText,
                        initialPaths = paths,
                        idSourcePaths = idSourcePaths,
                        initialLayout = idLayout,

                        onDismiss = {
                            dismiss()
                        },

                        onPrintAll = { currentPaths ->
                            NokoPrintHelper.printMultiple(
                                requireContext(),
                                currentPaths
                            )
                        },

                        onSaveAll = { currentPaths ->
                            currentPaths.forEach {
                                saveToDownloads(it)
                            }
                        },

                        onShareAll = { currentPaths ->
                            shareMultiple(currentPaths)
                        },

                        onOpen = { path ->
                            openFile(path)
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

            window.setDimAmount(0.60f)

            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.96f).toInt(),
                (resources.displayMetrics.heightPixels * 0.92f).toInt()
            )
        }
    }

    private fun saveToDownloads(path: String) {

        try {

            val sourceFile = File(path)

            DownloadStorageHelper.saveToDownloads(
                requireContext(),
                path,
                sourceFile.name,
                when {
                    sourceFile.extension.equals("pdf", ignoreCase = true) ->
                        "application/pdf"

                    else ->
                        "image/*"
                }
            )

        } catch (_: Exception) {
        }
    }

    private fun shareMultiple(
        paths: List<String>
    ) {

        val files =
            paths
                .map { File(it) }
                .filter { it.exists() }

        if (files.isEmpty()) {
            return
        }

        try {

            val uris =
                ArrayList(
                    files.map {
                        androidx.core.content.FileProvider
                            .getUriForFile(
                                requireContext(),
                                "${requireContext().packageName}.provider",
                                it
                            )
                    }
                )

            val intent =
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {

                    type = "image/*"

                    putParcelableArrayListExtra(
                        Intent.EXTRA_STREAM,
                        uris
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Share results"
                )
            )

        } catch (_: Exception) {
        }
    }

    private fun openFile(path: String) {

        val file = File(path)

        if (!file.exists()) {
            return
        }

        try {

            val uri =
                androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

            val mime =
                if (
                    file.extension.equals(
                        "pdf",
                        ignoreCase = true
                    )
                ) {
                    "application/pdf"
                } else {
                    "image/*"
                }

            val intent =
                Intent(Intent.ACTION_VIEW).apply {

                    setDataAndType(
                        uri,
                        mime
                    )

                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

            startActivity(
                Intent.createChooser(
                    intent,
                    "Open with"
                )
            )

        } catch (_: Exception) {
        }
    }
}


/* ============================================================
   MULTI RESULT COMPOSE UI
   ============================================================ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MultiResultContent(
    title: String,
    initialPaths: List<String>,
    idSourcePaths: List<String>,
    initialLayout: IdCardLayoutEngine.Layout,
    onDismiss: () -> Unit,
    onPrintAll: (List<String>) -> Unit,
    onSaveAll: (List<String>) -> Unit,
    onShareAll: (List<String>) -> Unit,
    onOpen: (String) -> Unit
) {

    val activity = LocalActivity.current

    var paths by remember {
        mutableStateOf(initialPaths)
    }

    var currentLayout by remember {
        mutableStateOf(initialLayout)
    }

    var isChangingLayout by remember {
        mutableStateOf(false)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { paths.size }
        )

    val scope =
        rememberCoroutineScope()

    LaunchedEffect(paths.size) {

        if (paths.isNotEmpty() &&
            pagerState.currentPage >= paths.size
        ) {
            pagerState.scrollToPage(
                paths.lastIndex
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            /* ---------- TOP BAR ---------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 10.dp,
                        end = 8.dp,
                        top = 8.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onDismiss
                ) {

                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge
                    )

                    Text(
                        text =
                            "${paths.size} ${
                                if (paths.size == 1)
                                    "sheet"
                                else
                                    "sheets"
                            }",

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

                Box {

                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {

                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Save All")
                            },
                            onClick = {
                                menuExpanded = false
                                onSaveAll(paths)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Share All")
                            },
                            onClick = {
                                menuExpanded = false
                                onShareAll(paths)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Open Current")
                            },
                            onClick = {
                                menuExpanded = false

                                if (paths.isNotEmpty()) {
                                    onOpen(
                                        paths[
                                            pagerState.currentPage
                                        ]
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            Divider()

            /* ---------- ID CARD LAYOUT ---------- */

            if (idSourcePaths.isNotEmpty()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        ),
                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    OutlinedButton(
                        enabled = !isChangingLayout,
                        onClick = {

                            val hostActivity = activity ?: return@OutlinedButton

                            val newLayout =
                                if (currentLayout == IdCardLayoutEngine.Layout.STACKED) {
                                    IdCardLayoutEngine.Layout.TOP_ROW
                                } else {
                                    IdCardLayoutEngine.Layout.STACKED
                                }

                            currentLayout = newLayout
                            isChangingLayout = true

                            kotlinx.coroutines.CoroutineScope(
                                kotlinx.coroutines.Dispatchers.Main
                            ).launch {

                                val newPaths = withContext(
                                    kotlinx.coroutines.Dispatchers.IO
                                ) {
                                    generateIdCardSheets(
                                        hostActivity,
                                        idSourcePaths,
                                        newLayout
                                    )
                                }

                                if (newPaths.isNotEmpty()) {
                                    paths = newPaths
                                    pagerState.scrollToPage(0)
                                }

                                isChangingLayout = false
                            }
                        }
                    ) {

                        if (isChangingLayout) {

                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )

                            Spacer(
                                Modifier.width(8.dp)
                            )
                        } else {

                            Icon(
                                if (
                                    currentLayout ==
                                    IdCardLayoutEngine.Layout.STACKED
                                ) {
                                    Icons.Default.ViewAgenda
                                } else {
                                    Icons.Default.Flip
                                },
                                contentDescription = null
                            )

                            Spacer(
                                Modifier.width(8.dp)
                            )
                        }

                        Text(
                            if (
                                currentLayout ==
                                IdCardLayoutEngine.Layout.STACKED
                            ) {
                                "Top Row Layout"
                            } else {
                                "Stacked Layout"
                            }
                        )
                    }
                }
            }

            /* ---------- PREVIEW ---------- */

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                if (paths.isEmpty()) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {

                        Text(
                            "No results available",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge
                        )
                    }

                } else {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->

                        ResultImage(
                            path = paths[page],
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 10.dp
                                )
                        )
                    }
                }
            }

            /* ---------- PAGE INDICATOR ---------- */

            if (paths.size > 1) {

                Text(
                    text =
                        "Page ${
                            pagerState.currentPage + 1
                        } of ${paths.size}",

                    modifier =
                        Modifier.fillMaxWidth(),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    textAlign =
                        androidx.compose.ui.text.style
                            .TextAlign.Center
                )

                Spacer(
                    Modifier.height(6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    repeat(paths.size.coerceAtMost(10)) { index ->

                        val selected =
                            index ==
                                    pagerState.currentPage

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(
                                    if (selected)
                                        8.dp
                                    else
                                        6.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (selected)
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                    else
                                        MaterialTheme
                                            .colorScheme
                                            .outlineVariant
                                )
                                .clickable {

                                    scope.launch {
                                        pagerState.animateScrollToPage(
                                            index
                                        )
                                    }
                                }
                        )
                    }
                }

                Spacer(
                    Modifier.height(8.dp)
                )
            }

            /* ---------- BOTTOM ACTIONS ---------- */

            MultiResultBottomBar(
                onSave = {
                    onSaveAll(paths)
                },

                onShare = {
                    onShareAll(paths)
                },

                onPrint = {
                    onPrintAll(paths)
                }
            )
        }
    }
}


/* ============================================================
   PREVIEW IMAGE
   ============================================================ */

@Composable
private fun ResultImage(
    path: String,
    modifier: Modifier = Modifier
) {

    val bitmapState =
        produceState<Bitmap?>(
            initialValue = null,
            key1 = path
        ) {

            value =
                withContext(Dispatchers.IO) {

                    val file =
                        File(path)

                    if (
                        !file.exists() ||
                        file.extension.equals(
                            "pdf",
                            ignoreCase = true
                        )
                    ) {
                        null
                    } else {
                        BitmapFactory.decodeFile(
                            file.absolutePath
                        )
                    }
                }
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        if (bitmapState.value == null) {

            CircularProgressIndicator(
                modifier = Modifier.size(42.dp),
                strokeWidth = 3.dp
            )

        } else {

            Image(
                bitmap =
                    bitmapState
                        .value!!
                        .asImageBitmap(),

                contentDescription =
                    "Result preview",

                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(18.dp)
                        ),

                contentScale =
                    ContentScale.Fit
            )
        }
    }
}


/* ============================================================
   TOP BAR
   ============================================================ */

@Composable
private fun ResultTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    showBack: Boolean
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBack
        ) {

            Icon(
                if (showBack)
                    Icons.Default.ArrowBack
                else
                    Icons.Default.Close,

                contentDescription = "Close"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Text(
                text = subtitle,
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
    }
}


/* ============================================================
   SINGLE RESULT ACTIONS
   ============================================================ */

@Composable
private fun ResultBottomBar(
    onSave: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    onOpen: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onSave
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Save"
                )
            }

            IconButton(
                onClick = onShare
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }

            IconButton(
                onClick = onPrint
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print"
                )
            }

            IconButton(
                onClick = onOpen
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Open"
                )
            }
        }
    }
}


/* ============================================================
   MULTI RESULT ACTIONS
   ============================================================ */

@Composable
private fun MultiResultBottomBar(
    onSave: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit
) {

    Surface(
        tonalElevation = 3.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onSave
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Save All"
                )
            }

            IconButton(
                onClick = onShare
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share"
                )
            }

            IconButton(
                onClick = onPrint
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print"
                )
            }
        }
    }
}


/* ============================================================
   ACTION BUTTON
   ============================================================ */

@Composable
private fun ResultAction(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier.clickable {
            onClick()
        },

        shape =
            RoundedCornerShape(16.dp),

        tonalElevation = 2.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 10.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier =
                    Modifier.size(22.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                Modifier.height(4.dp)
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


/* ============================================================
   ID CARD GENERATOR FOR LAYOUT TOGGLE
   ============================================================ */

private fun generateIdCardSheets(
    context: Context,
    sourcePaths: List<String>,
    layout: IdCardLayoutEngine.Layout
): List<String> {

    val validSources =
        sourcePaths
            .map { File(it) }
            .filter {
                it.exists() &&
                        it.isFile &&
                        it.extension.lowercase() in
                        setOf(
                            "jpg",
                            "jpeg",
                            "png",
                            "webp"
                        )
            }

    if (validSources.isEmpty()) {
        return emptyList()
    }

    val outputDirectory =
        File(
            context.cacheDir,
            "custom_tools"
        )

    if (
        !outputDirectory.exists() &&
        !outputDirectory.mkdirs()
    ) {
        return emptyList()
    }

    val outputs =
        mutableListOf<String>()

    validSources
        .chunked(2)
        .forEachIndexed { index, group ->

            val bitmaps =
                group.mapNotNull {
                    BitmapFactory.decodeFile(
                        it.absolutePath
                    )
                }

            if (bitmaps.isEmpty()) {
                return@forEachIndexed
            }

            try {

                val sheet =
                    IdCardLayoutEngine.createA4Sheet(
                        bitmaps,
                        layout
                    )

                val file =
                    File(
                        outputDirectory,
                        "id_cards_${layout.name.lowercase()}_${
                            System.currentTimeMillis()
                        }_${index}.png"
                    )

                file.outputStream().use { output ->

                    sheet.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        output
                    )
                }

                outputs.add(
                    file.absolutePath
                )

                sheet.recycle()

            } finally {

                bitmaps.forEach { bitmap ->

                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
            }
        }

    return outputs
}
