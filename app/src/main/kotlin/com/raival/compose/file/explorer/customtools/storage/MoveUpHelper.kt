package com.raival.compose.file.explorer.customtools.storage

import java.io.File

object MoveUpHelper {

    data class MoveUpItem(
        val source: File,
        val destination: File,
        val conflict: Boolean
    )

    data class MoveUpPreviewResult(
        val items: List<MoveUpItem>
    )

    data class MoveUpOperationResult(
        val success: Boolean,
        val movedCount: Int,
        val failedCount: Int
    )

    /**
     * Creates a preview of what will happen when the contents
     * of the selected folders are moved to their parent folders.
     */
    fun preview(folders: List<File>): MoveUpPreviewResult {

        val items = mutableListOf<MoveUpItem>()

        for (folder in folders) {

            if (!folder.exists() || !folder.isDirectory) {
                continue
            }

            val parent = folder.parentFile ?: continue

            val children = folder.listFiles() ?: emptyArray()

            for (child in children) {

                val destination = getUniqueDestination(
                    parent = parent,
                    originalName = child.name
                )

                items.add(
                    MoveUpItem(
                        source = child,
                        destination = destination,
                        conflict = destination.name != child.name
                    )
                )
            }
        }

        return MoveUpPreviewResult(
            items = items
        )
    }

    /**
     * Moves the contents of the selected folders into their
     * respective parent folders.
     *
     * Existing files are never overwritten.
     */
    fun move(
        folders: List<File>,
        overwrite: Boolean = false
    ): MoveUpOperationResult {

        var movedCount = 0
        var failedCount = 0

        for (folder in folders) {

            if (!folder.exists() || !folder.isDirectory) {
                continue
            }

            val parent = folder.parentFile

            if (parent == null || !parent.exists()) {
                continue
            }

            val children = folder.listFiles() ?: emptyArray()

            for (child in children) {

                val destination = if (overwrite) {
                    File(parent, child.name)
                } else {
                    getUniqueDestination(
                        parent = parent,
                        originalName = child.name
                    )
                }

                val success = moveFile(
                    source = child,
                    destination = destination
                )

                if (success) {
                    movedCount++
                } else {
                    failedCount++
                }
            }

            // Delete the now-empty selected folder.
            if (folder.exists()) {
                val remaining = folder.listFiles()

                if (remaining == null || remaining.isEmpty()) {
                    folder.delete()
                }
            }
        }

        return MoveUpOperationResult(
            success = failedCount == 0,
            movedCount = movedCount,
            failedCount = failedCount
        )
    }

    private fun moveFile(
        source: File,
        destination: File
    ): Boolean {

        if (!source.exists()) {
            return false
        }

        if (destination.exists()) {
            return false
        }

        // Fast path.
        if (source.renameTo(destination)) {
            return true
        }

        // Fallback for cases where renameTo() fails.
        return try {

            if (source.isDirectory) {
                copyDirectory(
                    source = source,
                    destination = destination
                )

                deleteRecursively(source)

            } else {
                source.inputStream().use { input ->
                    destination.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (!source.delete()) {
                    destination.delete()
                    return false
                }
            }

            true

        } catch (_: Exception) {

            if (destination.exists()) {
                destination.deleteRecursively()
            }

            false
        }
    }

    private fun copyDirectory(
        source: File,
        destination: File
    ) {

        if (!destination.exists()) {
            destination.mkdirs()
        }

        val children = source.listFiles() ?: return

        for (child in children) {

            val target = File(
                destination,
                child.name
            )

            if (child.isDirectory) {
                copyDirectory(
                    source = child,
                    destination = target
                )
            } else {
                child.inputStream().use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            val children = file.listFiles()

            if (children != null) {
                for (child in children) {
                    deleteRecursively(child)
                }
            }
        }

        file.delete()
    }

    /**
     * Generates a non-conflicting destination name.
     *
     * Example:
     * photo.jpg
     * photo_1.jpg
     * photo_2.jpg
     */
    private fun getUniqueDestination(
        parent: File,
        originalName: String
    ): File {

        val first = File(parent, originalName)

        if (!first.exists()) {
            return first
        }

        val dotIndex = originalName.lastIndexOf('.')

        val baseName: String
        val extension: String

        if (dotIndex > 0) {
            baseName = originalName.substring(0, dotIndex)
            extension = originalName.substring(dotIndex)
        } else {
            baseName = originalName
            extension = ""
        }

        var counter = 1

        while (true) {

            val candidate = File(
                parent,
                "${baseName}_${counter}${extension}"
            )

            if (!candidate.exists()) {
                return candidate
            }

            counter++
        }
    }
}