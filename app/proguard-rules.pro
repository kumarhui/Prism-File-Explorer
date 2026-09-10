# ============================================================
# Application
# ============================================================

-keep class com.raival.compose.file.explorer.** { *; }

# ============================================================
# TM4E / TextMate
# ============================================================

-keep class org.eclipse.tm4e.** { *; }
-keep class org.joni.** { *; }

# ============================================================
# Existing project rules
# ============================================================

-keep class android.content.** { *; }
-keep class com.android.apksig.** { *; }

-keepnames interface * { *; }


# ============================================================
# PDFBox optional dependencies
# ============================================================

# PDFBox-Android optional JPEG2000 support.
# jp2-android is intentionally not included.
-dontwarn com.gemalto.jp2.**

# PDFBox-Android optional SLF4J binding.
-dontwarn org.slf4j.impl.StaticLoggerBinder