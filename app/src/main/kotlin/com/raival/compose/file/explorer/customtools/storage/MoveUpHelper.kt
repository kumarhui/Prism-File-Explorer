package com.raival.compose.file.explorer.customtools.storage

import java.io.File

object MoveUpHelper {

    data class MoveItem(
        val source: File,
        val destination: File,
        val conflict: Boolean
    )

    data class MoveUpPreviewResult(
        val items: List<MoveItem>
    )

    data class MoveUpOperationResult(
        val success: Boolean,
        val movedCount: Int,
        val failedCount: Int
    )

    fun preview(folders: List<String>): MoveUpPreviewResult {
        val reserved = HashSet<String>()
        val items = mutableListOf<MoveItem>()

        folders.map(::File)
            .filter { it.exists() && it.isDirectory }
            .forEach { folder ->
                val parent = folder.parentFile ?: return@forEach
                folder.listFiles().orEmpty().forEach { child ->
                    val destination = uniqueDestination(parent, child.name, reserved)
                    val conflict = destination.name != child.name
                    reserved += destination.absolutePath
                    items += MoveItem(child, destination, conflict)
                }
            }

        return MoveUpPreviewResult(items)
    }

    fun move(
        folders: List<String>,
        overwrite: Boolean = false
    ): MoveUpOperationResult {
        var moved = 0
        var failed = 0

        folders.map(::File)
            .filter { it.exists() && it.isDirectory }
            .forEach { folder ->
                val parent = folder.parentFile ?: return@forEach

                folder.listFiles().orEmpty().forEach { child ->
                    var destination = File(parent, child.name)
                    if (destination.exists()) {
                        if (overwrite) {
                            deleteRecursively(destination)
                        } else {
                            destination = uniqueDestination(parent, child.name, emptySet())
                        }
                    }

                    if (moveFile(child, destination)) moved++ else failed++
                }

                if (folder.listFiles().orEmpty().isEmpty()) {
                    folder.delete()
                }
            }

        return MoveUpOperationResult(
            success = failed == 0,
            movedCount = moved,
            failedCount = failed
        )
    }

    private fun uniqueDestination(
        parent: File,
        name: String,
        reserved: Set<String>
    ): File {
        val original = File(parent, name)
        if (!original.exists() && original.absolutePath !in reserved) return original

        val base = original.nameWithoutExtension
        val extension = original.extension
        var number = 1

        while (true) {
            val candidateName = if (extension.isEmpty()) {
                "${base}_$number"
            } else {
                "${base}_$number.$extension"
            }
            val candidate = File(parent, candidateName)
            if (!candidate.exists() && candidate.absolutePath !in reserved) return candidate
            number++
        }
    }

    private fun moveFile(source: File, destination: File): Boolean {
        if (!source.exists() || destination.exists()) return false
        if (source.renameTo(destination)) return true

        return try {
            copyRecursively(source, destination)
            if (deleteRecursively(source)) true else {
                deleteRecursively(destination)
                false
            }
        } catch (_: Exception) {
            deleteRecursively(destination)
            false
        }
    }

    private fun copyRecursively(source: File, destination: File) {
        if (source.isDirectory) {
            destination.mkdirs()
            source.listFiles().orEmpty().forEach { child ->
                copyRecursively(child, File(destination, child.name))
            }
        } else {
            source.inputStream().use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }

    private fun deleteRecursively(file: File): Boolean {
        var success = true
        if (file.isDirectory) {
            file.listFiles().orEmpty().forEach { if (!deleteRecursively(it)) success = false }
        }
        if (file.exists() && !file.delete()) success = false
        return success
    }
}
