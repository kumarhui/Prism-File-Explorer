package com.raival.compose.file.explorer.customtools.ui

import androidx.annotation.DrawableRes

data class CustomToolsItem(
    val id: ToolId,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
)

enum class ToolId {
    PASSPORT_PHOTO,
    PDF_UNLOCKER,
    ID_CARD,
    JUGANUA,
    COMPRESS_IMAGE,
    CROP_IMAGE,
    CONVERT_PDF,
    IMAGE_INFO,
    WHATSAPP,
    NOKOPRINT
}

