package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.OpenInNewOff
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.customtools.CustomToolRunner
import com.raival.compose.file.explorer.customtools.pdfunlocker.PdfUnlockerDialog
import com.raival.compose.file.explorer.customtools.preview.CustomRenameDialog
import com.raival.compose.file.explorer.customtools.preview.PrintPreviewDialog
import com.raival.compose.file.explorer.customtools.preview.WhatsAppPreviewDialog
import com.raival.compose.file.explorer.customtools.ui.ImageConversionDialog
import com.raival.compose.file.explorer.customtools.ui.PassportPhotoConfigDialog
import com.raival.compose.file.explorer.customtools.ui.PdfPageExtractionDialog
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.DefaultOpeningMethods
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkBundleFileType
import com.raival.compose.file.explorer.screen.main.tab.files.task.ApksMergeTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.ApksMergeTaskParameters
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileIcon
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRow

@Composable
fun FileOptionsMenuDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current

        val targetFiles = tab.selectedFiles.map { it.value }.toList()
        val targetContentHolder = tab.targetFile!!

        val selectedFilesCount = targetFiles.size
        val isMultipleSelection = selectedFilesCount > 1
        val isSingleFile = !isMultipleSelection && targetContentHolder.isFile()
        val isSingleFolder = !isMultipleSelection && targetContentHolder.isFolder

        var hasFolders = false
        tab.selectedFiles.forEach {
            if (it.component2().isFolder) {
                hasFolders = true
                return@forEach
            }
        }

        BottomSheetDialog(onDismissRequest = { tab.toggleFileOptionsMenu(null) }) {
            var details by remember {
                mutableStateOf(
                    if (selectedFilesCount > 1) {
                        "and %d more".format(selectedFilesCount - 1)
                    } else {
                        emptyString
                    }
                )
            }

            LaunchedEffect(Unit) {
                if (details.isEmpty()) details = targetContentHolder.getDetails()
            }

            ItemRow(
                title = targetContentHolder.displayName,
                subtitle = details,
                ignoreSizePreferences = true,
                icon = {
                    FileIcon(
                        contentHolder = targetContentHolder,
                        ignoreSizePreferences = true
                    )
                }
            )

            Space(size = 6.dp)
            HorizontalDivider()
            Space(size = 6.dp)

            Row {
                // Delete
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleDeleteConfirmationDialog(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Cut
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.unselectAllFiles()
                        globalClass.taskManager.addTask(
                            CopyTask(
                                targetFiles,
                                deleteSourceFiles = true
                            )
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null)
                }

                // Copy
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.unselectAllFiles()
                        globalClass.taskManager.addTask(
                            CopyTask(
                                targetFiles,
                                deleteSourceFiles = false
                            )
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.FileCopy, contentDescription = null)
                }

                // Rename
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleRenameDialog(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatColorText,
                        contentDescription = null
                    )
                }

                // Share
                if (!hasFolders && targetContentHolder is LocalFileHolder) {
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismissRequest()
                            tab.shareSelectedFiles(context)
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                    }
                }

                // Properties
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleFilePropertiesDialog(true)
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                }
            }

            Space(size = 6.dp)
            HorizontalDivider()
            Space(size = 8.dp)

            val printableExtensions = setOf(
                "jpg",
                "jpeg",
                "png",
                "webp",
                "pdf"
            )

            val printableFiles = targetFiles.filter { fileHolder ->
                fileHolder is LocalFileHolder &&
                        fileHolder.file.extension.lowercase() in printableExtensions
            }

            val selectedImageFiles = targetFiles
                .filterIsInstance<LocalFileHolder>()
                .filter {
                    it.file.extension.lowercase() in setOf(
                        "jpg", "jpeg", "png", "webp"
                    )
                }

            val selectedPdfFiles = targetFiles
                .filterIsInstance<LocalFileHolder>()
                .filter {
                    it.file.extension.equals("pdf", ignoreCase = true)
                }

            // Quick Actions: Print, WhatsApp, Custom Rename, PDF Convert, and Open With
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. NokoPrint (Print)
                if (printableFiles.isNotEmpty()) {
                    SmallActionIcon(
                        icon = Icons.Rounded.Print,
                        contentDescription = stringResource(R.string.print),
                        iconTint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        onClick = {
                            onDismissRequest()
                            if (printableFiles.size == 1) {
                                PrintPreviewDialog.show(
                                    context,
                                    (printableFiles.first() as LocalFileHolder).uniquePath
                                )
                            } else {
                                PrintPreviewDialog.showMultiple(
                                    context,
                                    printableFiles.map {
                                        (it as LocalFileHolder).uniquePath
                                    }
                                )
                            }
                        }
                    )

                    // 2. WhatsApp (Green)
                    SmallActionIcon(
                        icon = Icons.Rounded.Message,
                        contentDescription = stringResource(R.string.send_to_whatsapp),
                        iconTint = Color(0xFF25D366),
                        containerColor = Color(0xFF25D366).copy(alpha = 0.12f),
                        onClick = {
                            onDismissRequest()
                            val whatsappFiles = printableFiles.map {
                                (it as LocalFileHolder).uniquePath
                            }
                            if (whatsappFiles.isNotEmpty()) {
                                WhatsAppPreviewDialog.show(
                                    context,
                                    whatsappFiles
                                )
                            }
                        }
                    )
                }

                // 3. Custom Rename
                if (targetFiles.isNotEmpty()) {
                    SmallActionIcon(
                        icon = Icons.Rounded.DriveFileRenameOutline,
                        contentDescription = stringResource(R.string.custom_rename),
                        iconTint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        onClick = {
                            onDismissRequest()
                            val renameFiles = targetFiles
                                .filterIsInstance<LocalFileHolder>()
                                .map { it.uniquePath }

                            if (renameFiles.isNotEmpty()) {
                                CustomRenameDialog.show(
                                    context,
                                    renameFiles
                                )
                            }
                        }
                    )
                }

                // 4. Convert to PDF
                if (selectedImageFiles.isNotEmpty() &&
                    selectedPdfFiles.isEmpty() &&
                    selectedImageFiles.size == targetFiles.size
                ) {
                    SmallActionIcon(
                        icon = Icons.Rounded.PictureAsPdf,
                        contentDescription = stringResource(R.string.convert_to_pdf),
                        iconTint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        onClick = {
                            onDismissRequest()
                            ImageConversionDialog.showPdf(
                                context,
                                selectedImageFiles.map { it.uniquePath }
                            )
                        }
                    )
                }

                // 5. Open With
                if (isSingleFile && targetContentHolder is LocalFileHolder) {
                    SmallActionIcon(
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = stringResource(R.string.open_with),
                        iconTint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        onClick = {
                            onDismissRequest()
                            tab.toggleOpenWithDialog(true)
                        }
                    )
                }
            }

            Space(size = 8.dp)
            HorizontalDivider()

            // 1. PASSPORT PHOTO MAKER
            val passportImageFiles = targetFiles
                .filterIsInstance<LocalFileHolder>()
                .filter {
                    it.file.extension.lowercase() in setOf(
                        "jpg", "jpeg", "png", "webp"
                    )
                }

            if (passportImageFiles.isNotEmpty() &&
                passportImageFiles.size == targetFiles.size
            ) {
                FileOption(
                    Icons.Rounded.Badge,
                    stringResource(R.string.passport_photo_maker)
                ) {
                    onDismissRequest()
                    PassportPhotoConfigDialog.show(
                        context,
                        passportImageFiles.map { it.uniquePath }
                    )
                }
            }

            // 2. PDF UNLOCKER (Single PDF file)
            val singlePdfFile =
                selectedFilesCount == 1 &&
                        targetContentHolder is LocalFileHolder &&
                        targetContentHolder.file.extension.equals("pdf", ignoreCase = true)

            if (singlePdfFile) {
                FileOption(
                    Icons.Default.LockOpen,
                    stringResource(R.string.pdf_unlocker)
                ) {
                    onDismissRequest()
                    PdfUnlockerDialog.show(
                        context,
                        Uri.fromFile(targetContentHolder.file)
                    )
                }
            }

            // 3. ID CARD MAKER
            val idCardImageFiles = targetFiles
                .filterIsInstance<LocalFileHolder>()
                .filter {
                    it.file.extension.lowercase() in setOf(
                        "jpg", "jpeg", "png", "webp"
                    )
                }

            if (idCardImageFiles.isNotEmpty() &&
                idCardImageFiles.size == targetFiles.size
            ) {
                FileOption(
                    Icons.Rounded.Badge,
                    stringResource(R.string.id_card_maker)
                ) {
                    onDismissRequest()
                    CustomToolRunner.createIdCards(
                        context,
                        idCardImageFiles.map { it.uniquePath }
                    )
                }
            }

            // Convert to Image (PDFs to Image)
            val selectedPdfFilesForImage = targetFiles
                .filterIsInstance<LocalFileHolder>()
                .filter {
                    it.file.extension.equals("pdf", ignoreCase = true)
                }

            if (selectedPdfFilesForImage.isNotEmpty() &&
                selectedPdfFilesForImage.size == targetFiles.size
            ) {
                FileOption(
                    Icons.Rounded.Image,
                    stringResource(R.string.convert_to_image)
                ) {
                    onDismissRequest()
                    PdfPageExtractionDialog.show(
                        context,
                        selectedPdfFilesForImage.map { it.uniquePath }
                    )
                }
            }

            if (isSingleFolder) {
                FileOption(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.open_in_new_tab)
                ) {
                    onDismissRequest()
                    tab.requestNewTab(FilesTab(targetContentHolder))
                    tab.unselectAllFiles()
                }
            }

            if (tab.activeFolder is VirtualFileHolder && (tab.activeFolder as VirtualFileHolder).type == VirtualFileHolder.BOOKMARKS) {
                FileOption(
                    Icons.Rounded.BookmarkAdd,
                    stringResource(R.string.remove_from_bookmarks)
                ) {
                    onDismissRequest()
                    val toRemove = targetFiles.map { it.uniquePath }
                    val newBookmarks = globalClass.preferencesManager.bookmarks.filter {
                        !toRemove.contains(it)
                    }
                    globalClass.preferencesManager.bookmarks = newBookmarks.toSet()
                    tab.unselectAllFiles()
                    tab.reloadFiles()
                }
            }

            if (tab.activeFolder is LocalFileHolder ||
                (tab.activeFolder is VirtualFileHolder && (tab.activeFolder as VirtualFileHolder).type isNot VirtualFileHolder.BOOKMARKS)
            ) {
                FileOption(Icons.Rounded.BookmarkAdd, stringResource(R.string.add_to_bookmarks)) {
                    onDismissRequest()
                    globalClass.preferencesManager.bookmarks += targetFiles.map { it.uniquePath }
                    globalClass.showMsg(R.string.added_to_bookmarks)
                    tab.unselectAllFiles()
                }
                val pinnedFiles by remember {
                    mutableStateOf(
                        globalClass.preferencesManager.pinnedFiles
                    )
                }
                if (targetFiles.map { it.uniquePath }.toSet() == pinnedFiles) {
                    FileOption(
                        Icons.Rounded.PushPin,
                        stringResource(R.string.unpin_from_home_tab)
                    ) {
                        onDismissRequest()
                        val oldSet = globalClass.preferencesManager.pinnedFiles
                        globalClass.preferencesManager.pinnedFiles = oldSet - pinnedFiles
                        globalClass.showMsg(R.string.done)
                        tab.unselectAllFiles()
                    }
                } else {
                    FileOption(Icons.Rounded.PushPin, stringResource(R.string.pin_to_home_tab)) {
                        onDismissRequest()
                        val oldSet = globalClass.preferencesManager.pinnedFiles
                        globalClass.preferencesManager.pinnedFiles =
                            oldSet + targetFiles.map { it.uniquePath }
                        globalClass.showMsg(R.string.done)
                        tab.unselectAllFiles()
                    }
                }
            }

            if (isRequestPinShortcutSupported(context) && tab.activeFolder is LocalFileHolder && (isSingleFile || isSingleFolder)) {
                FileOption(Icons.Rounded.Home, stringResource(R.string.add_to_home_screen)) {
                    onDismissRequest()
                    tab.addToHomeScreen(context, targetContentHolder as LocalFileHolder)
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile && targetContentHolder is LocalFileHolder) {
                FileOption(Icons.Rounded.EditNote, stringResource(R.string.edit_with_text_editor)) {
                    onDismissRequest()
                    globalClass.textEditorManager.openTextEditor(targetContentHolder, context)
                    tab.unselectAllFiles()
                }

                if (apkBundleFileType.contains(targetContentHolder.file.extension)) {
                    FileOption(Icons.Rounded.Merge, stringResource(R.string.convert_to_apk)) {
                        onDismissRequest()
                        globalClass.taskManager.addTaskAndRun(
                            ApksMergeTask(targetContentHolder),
                            ApksMergeTaskParameters(
                                globalClass.preferencesManager.signMergedApkBundleFiles
                            )
                        )
                        tab.unselectAllFiles()
                    }
                }
            }

            if (tab.activeFolder !is ZipFileHolder) {
                FileOption(Icons.Rounded.Compress, stringResource(R.string.compress)) {
                    CompressTask(targetFiles).let { task ->
                        globalClass.taskManager.addTask(task, false)
                        tab.toggleCompressTaskDialog(task)
                    }
                    onDismissRequest()
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile && targetContentHolder is LocalFileHolder) {
                FileOption(
                    Icons.Rounded.OpenInNewOff,
                    stringResource(R.string.remove_default_opening_method)
                ) {
                    fromJson<DefaultOpeningMethods>(globalClass.preferencesManager.defaultOpeningMethods)?.let {
                        globalClass.preferencesManager.defaultOpeningMethods =
                            DefaultOpeningMethods(
                                it.openingMethods.filter { it.extension != targetContentHolder.file.extension }
                            ).toJson()
                    }
                    onDismissRequest()
                }
            }
        }
    }
}

@Composable
private fun SmallActionIcon(
    icon: ImageVector,
    contentDescription: String,
    iconTint: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun FileOption(
    icon: ImageVector,
    text: String,
    highlight: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(21.dp),
            imageVector = icon,
            tint = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight,
            contentDescription = null
        )
        Space(size = 12.dp)
        Text(
            text = text,
            color = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight
        )
    }
}