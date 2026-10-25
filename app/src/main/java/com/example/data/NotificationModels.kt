package com.example.data

import com.squareup.moshi.Json
import java.net.URLEncoder

data class NotificationActor(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "profileImageUrl") val profileImageUrl: String? = null
)

data class AppNotification(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String = "job_created",
    @Json(name = "title") val title: String = "",
    @Json(name = "body") val body: String? = null,
    @Json(name = "link") val link: String? = null,
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "actorId") val actorId: String? = null,
    @Json(name = "actor") val actor: NotificationActor? = null,
    @Json(name = "entityType") val entityType: String? = null,
    @Json(name = "entityId") val entityId: String? = null,
    @Json(name = "isRead") val isRead: Boolean = false,
    @Json(name = "createdAt") val createdAt: String = ""
)

fun getDefaultNotificationAvatar(name: String?, seed: String): String {
    val colors = listOf("1877F2", "45BD62", "F3425F", "F7B928", "9360F7")
    val hash = seed.fold(0) { acc, c -> acc + c.code }
    val c = colors[Math.abs(hash) % colors.size]
    val init = (name?.trim()?.take(1) ?: "U").uppercase()
    val encoded = try { URLEncoder.encode(init, "UTF-8") } catch (_: Exception) { init }
    return "https://ui-avatars.com/api/?name=$encoded&background=$c&color=fff&size=128&bold=true&rounded=true"
}

fun parseNotificationTimeMs(iso: String?): Long {
    if (iso.isNullOrBlank()) return 0L
    return try {
        val sanitized = iso.trim().replace(" ", "T")
        val withZ = if (!sanitized.endsWith("Z") && !sanitized.contains("+")) "${sanitized}Z" else sanitized
        java.time.Instant.parse(withZ).toEpochMilli()
    } catch (_: Exception) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            sdf.parse(iso)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}

fun formatNotificationRelativeTime(iso: String?): String {
    val timeMs = parseNotificationTimeMs(iso)
    if (timeMs == 0L) return "Just now"
    val diff = System.currentTimeMillis() - timeMs
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000L}m"
        diff < 86_400_000L -> "${diff / 3_600_000L}h"
        diff < 604_800_000L -> "${diff / 86_400_000L}d"
        else -> {
            val date = java.util.Date(timeMs)
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
            sdf.format(date)
        }
    }
}
