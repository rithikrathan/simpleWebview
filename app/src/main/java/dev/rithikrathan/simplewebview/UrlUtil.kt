package dev.rithikrathan.simplewebview

import android.net.Uri

fun normalizeUrl(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val withScheme = if (trimmed.contains("://")) trimmed else "https://$trimmed"
    return withScheme.takeIf { it.startsWith("http://") || it.startsWith("https://") }
}

fun hostOf(url: String): String? = runCatching { Uri.parse(url).host }.getOrNull()
