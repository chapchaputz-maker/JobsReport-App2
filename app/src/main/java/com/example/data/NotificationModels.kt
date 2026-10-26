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

/**
 * Formats a notification so the sender / actor's name is prominently displayed.
 * (e.g. "Jastin Beda shared new Job story" or "Company posted a new job: ...")
 * Returns Pair(actorName, displayTitle).
 */
fun formatNotificationDisplay(
    item: AppNotification,
    fallbackUserName: String? = null
): Pair<String, String> {
    val actorName = item.actor?.name?.trim()?.takeIf { it.isNotBlank() }
        ?: item.actor?.username?.trim()?.takeIf { it.isNotBlank() }
        ?: fallbackUserName?.trim()?.takeIf { it.isNotBlank() }
        ?: ""

    val rawTitle = item.title.trim()
    val isStory = item.type.contains("story", ignoreCase = true) ||
            item.entityType?.equals("story", ignoreCase = true) == true ||
            item.link?.contains("/stories", ignoreCase = true) == true ||
            item.link?.contains("/story", ignoreCase = true) == true ||
            rawTitle.contains("story", ignoreCase = true) ||
            rawTitle.contains("stoty", ignoreCase = true)

    val isJob = item.type.contains("job", ignoreCase = true) ||
            item.entityType?.equals("job", ignoreCase = true) == true ||
            item.link?.contains("/job", ignoreCase = true) == true ||
            item.link?.contains("/careerredirect", ignoreCase = true) == true

    val displayTitle = when {
        // Story notification with actor name
        isStory && actorName.isNotBlank() -> {
            when {
                rawTitle.startsWith(actorName, ignoreCase = true) -> rawTitle
                rawTitle.startsWith("shared", ignoreCase = true) -> "$actorName $rawTitle"
                rawTitle.isBlank() ||
                    rawTitle.equals("new job story", ignoreCase = true) ||
                    rawTitle.equals("new story", ignoreCase = true) ||
                    rawTitle.equals("story created", ignoreCase = true) -> "$actorName shared new Job story"
                else -> "$actorName shared new Job story: $rawTitle"
            }
        }
        // Story notification without actor name
        isStory -> {
            if (rawTitle.isBlank()) "Shared new Job story" else rawTitle
        }
        // Job notification with actor/company name
        isJob && actorName.isNotBlank() -> {
            when {
                rawTitle.startsWith(actorName, ignoreCase = true) -> rawTitle
                rawTitle.startsWith("posted", ignoreCase = true) || rawTitle.startsWith("shared", ignoreCase = true) -> "$actorName $rawTitle"
                rawTitle.isBlank() -> "$actorName posted a new job opportunity"
                else -> "$actorName posted a new job: $rawTitle"
            }
        }
        // Generic action verbs (e.g. "shared...", "posted...", "applied...")
        actorName.isNotBlank() && (
            rawTitle.startsWith("shared", ignoreCase = true) ||
            rawTitle.startsWith("posted", ignoreCase = true) ||
            rawTitle.startsWith("applied", ignoreCase = true) ||
            rawTitle.startsWith("commented", ignoreCase = true) ||
            rawTitle.startsWith("liked", ignoreCase = true)
        ) -> {
            if (rawTitle.startsWith(actorName, ignoreCase = true)) rawTitle else "$actorName $rawTitle"
        }
        rawTitle.isNotBlank() -> rawTitle
        !item.body.isNullOrBlank() -> item.body.trim()
        actorName.isNotBlank() -> "$actorName sent a notification"
        else -> "Notification"
    }

    return Pair(actorName, displayTitle)
}

