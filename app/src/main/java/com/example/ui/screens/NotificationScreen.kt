package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppNotification
import com.example.data.formatNotificationRelativeTime
import com.example.data.getDefaultNotificationAvatar
import com.example.data.parseNotificationTimeMs
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BgSurface
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple
import kotlinx.coroutines.delay

@Composable
fun NotificationScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    // Initial load when screen opens
    LaunchedEffect(viewModel.userToken) {
        if (viewModel.userToken != null) {
            viewModel.loadNotifications(0)
        }
    }

    // Auto-dismiss toast after 2.5s
    LaunchedEffect(viewModel.notificationToastMessage) {
        if (viewModel.notificationToastMessage != null) {
            delay(2500)
            viewModel.notificationToastMessage = null
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchOpen by remember { mutableStateOf(false) }

    // Filter notifications based on search query
    val filteredNotifications by remember(viewModel.notificationsList, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                viewModel.notificationsList
            } else {
                val q = searchQuery.trim().lowercase()
                viewModel.notificationsList.filter { n ->
                    n.title.lowercase().contains(q) ||
                            (n.body ?: "").lowercase().contains(q) ||
                            (n.actor?.name ?: "").lowercase().contains(q)
                }
            }
        }
    }

    // Grouping: New (<2 hours ago or unread), Today (since midnight), Earlier
    val groupedSections by remember(filteredNotifications) {
        derivedStateOf {
            val newN = mutableListOf<AppNotification>()
            val todayN = mutableListOf<AppNotification>()
            val earlierN = mutableListOf<AppNotification>()

            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val startOfTodayMs = cal.timeInMillis
            val twoHoursAgoMs = now - (2 * 3600 * 1000L)

            filteredNotifications.forEach { n ->
                val t = parseNotificationTimeMs(n.createdAt)
                if (!n.isRead || t >= twoHoursAgoMs) {
                    newN.add(n)
                } else if (t >= startOfTodayMs) {
                    todayN.add(n)
                } else {
                    earlierN.add(n)
                }
            }

            Triple(newN, todayN, earlierN)
        }
    }

    val (newList, todayList, earlierList) = groupedSections

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .testTag("notification_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========================================================
            // HEADER BAR (Parity with Web NotificationsPage top bar)
            // ========================================================
            Surface(
                color = BgSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderWhite10, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Back button + Title + Unread Count Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("notification_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Notifications",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (viewModel.unreadNotificationsCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.2f))
                                        .border(1.dp, PrimaryBlue.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${viewModel.unreadNotificationsCount} new",
                                        color = LightBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Right Actions: Search toggle & Mark all read
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Search toggle button
                            IconButton(
                                onClick = {
                                    isSearchOpen = !isSearchOpen
                                    if (!isSearchOpen) searchQuery = ""
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSearchOpen) PrimaryBlue.copy(alpha = 0.2f) else Color.Transparent)
                                    .testTag("notification_search_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search notifications",
                                    tint = if (isSearchOpen) LightBlue else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Mark all read button (CheckCheck icon)
                            if (viewModel.unreadNotificationsCount > 0) {
                                IconButton(
                                    onClick = { viewModel.markAllNotificationsRead() },
                                    enabled = !viewModel.isMarkingAllNotifications,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("notification_mark_all_read")
                                ) {
                                    if (viewModel.isMarkingAllNotifications) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = LightBlue,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Mark all as read",
                                            tint = LightBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Expandable Search Bar
                    AnimatedVisibility(
                        visible = isSearchOpen,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BgCard)
                                .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search notifications by title or actor...",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        textStyle = TextStyle(
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        ),
                                        cursorBrush = SolidColor(LightBlue),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("notification_search_input"),
                                        singleLine = true
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ========================================================
            // MAIN BODY: AUTH GATE OR NOTIFICATIONS FEED
            // ========================================================
            if (viewModel.userToken == null) {
                // Not authenticated prompt (Parity with web auth guard)
                UnauthenticatedPrompt(onSignInClick = { viewModel.openAuth() })
            } else if (viewModel.isNotificationsLoading && viewModel.notificationsList.isEmpty()) {
                // Loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Loading notifications...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (filteredNotifications.isEmpty()) {
                // Empty state
                EmptyNotificationsView(
                    isSearchActive = searchQuery.isNotBlank(),
                    onClearSearch = { searchQuery = "" }
                )
            } else {
                // Notifications List with Grouped Sections
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. NEW SECTION
                    if (newList.isNotEmpty()) {
                        item(key = "section_header_new") {
                            SectionHeader(title = "NEW", count = newList.size)
                        }
                        items(newList, key = { "new_${it.id}" }) { item ->
                            NotificationCard(
                                item = item,
                                onClick = { viewModel.handleNotificationClick(item) },
                                onToggleRead = { viewModel.markNotificationRead(item.id, !item.isRead) },
                                onDelete = { viewModel.deleteNotification(item.id) }
                            )
                        }
                    }

                    // 2. TODAY SECTION
                    if (todayList.isNotEmpty()) {
                        item(key = "section_header_today") {
                            SectionHeader(title = "TODAY", count = todayList.size)
                        }
                        items(todayList, key = { "today_${it.id}" }) { item ->
                            NotificationCard(
                                item = item,
                                onClick = { viewModel.handleNotificationClick(item) },
                                onToggleRead = { viewModel.markNotificationRead(item.id, !item.isRead) },
                                onDelete = { viewModel.deleteNotification(item.id) }
                            )
                        }
                    }

                    // 3. EARLIER SECTION
                    if (earlierList.isNotEmpty()) {
                        item(key = "section_header_earlier") {
                            SectionHeader(title = "EARLIER", count = earlierList.size)
                        }
                        items(earlierList, key = { "earlier_${it.id}" }) { item ->
                            NotificationCard(
                                item = item,
                                onClick = { viewModel.handleNotificationClick(item) },
                                onToggleRead = { viewModel.markNotificationRead(item.id, !item.isRead) },
                                onDelete = { viewModel.deleteNotification(item.id) }
                            )
                        }
                    }

                    // 4. LOAD MORE / PAGINATION
                    if (viewModel.hasMoreNotifications && searchQuery.isBlank()) {
                        item(key = "load_more_button") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.loadNotifications(offset = viewModel.notificationsList.size)
                                    },
                                    enabled = !viewModel.isNotificationsLoadingMore,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BgCard,
                                        contentColor = LightBlue
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                                        .testTag("notification_load_more")
                                ) {
                                    if (viewModel.isNotificationsLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = LightBlue,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading more...", fontSize = 12.sp)
                                    } else {
                                        Text("See previous notifications", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ========================================================
        // FLOATING TOAST NOTIFICATION
        // ========================================================
        AnimatedVisibility(
            visible = viewModel.notificationToastMessage != null,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            val isError = viewModel.notificationToastIsError
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isError) Color(0xFFEF4444) else Color(0xFF0F172A))
                    .border(
                        1.dp,
                        if (isError) Color(0xFFF87171) else PrimaryBlue.copy(alpha = 0.6f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Clear else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isError) Color.White else EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = viewModel.notificationToastMessage ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ============================================================
// SECTION HEADER
// ============================================================
@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = "$count",
            color = TextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============================================================
// NOTIFICATION CARD ROW
// ============================================================
@Composable
private fun NotificationCard(
    item: AppNotification,
    onClick: () -> Unit,
    onToggleRead: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    // Icon info
    val (iconVector, badgeBg) = remember(item.type) {
        when (item.type) {
            "job_created" -> Pair(Icons.Default.Work, Color(0xFF1877F2))
            "story_created" -> Pair(Icons.Default.AutoAwesome, Color(0xFFA033FF))
            "job_daily" -> Pair(Icons.Default.Work, Color(0xFF10B981))
            else -> Pair(Icons.Default.Notifications, Color(0xFF1877F2))
        }
    }

    val actorName = item.actor?.name?.trim() ?: ""
    val avatarUrl = item.actor?.profileImageUrl?.takeIf { it.isNotBlank() }
        ?: getDefaultNotificationAvatar(actorName.ifEmpty { item.title }, item.actorId ?: item.id)

    val relativeTime = remember(item.createdAt) {
        formatNotificationRelativeTime(item.createdAt)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (!item.isRead) PrimaryBlue.copy(alpha = 0.08f) else BgCard)
            .border(
                1.dp,
                if (!item.isRead) PrimaryBlue.copy(alpha = 0.35f) else BorderWhite5,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("notification_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Actor Avatar with Type Badge Overlay
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Top)
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = actorName.ifEmpty { "Notification actor" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BgSurface)
                )
                // Small badge icon in bottom-right corner
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(badgeBg)
                        .border(1.5.dp, BgDeep, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle Content: Title, Body, Timestamp
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                // Title (With bold actor name if present)
                val annotatedTitle = remember(item.title, actorName) {
                    buildAnnotatedString {
                        if (actorName.isNotBlank() && item.title.startsWith(actorName, ignoreCase = true)) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                                append(actorName)
                            }
                            append(item.title.substring(actorName.length))
                        } else {
                            append(item.title)
                        }
                    }
                }

                Text(
                    text = annotatedTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (!item.isRead) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.body.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.body,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = relativeTime,
                        color = if (!item.isRead) LightBlue else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace
                    )

                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LightBlue)
                        )
                    }
                }
            }

            // Right Actions: Unread dot & 3-dots Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(LightBlue, PrimaryBlue)
                                )
                            )
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Notification options",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier
                            .background(BgSurface)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (item.isRead) "Mark as unread" else "Mark as read",
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (item.isRead) Icons.Default.Check else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LightBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onToggleRead()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Delete notification",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// EMPTY STATE
// ============================================================
@Composable
private fun EmptyNotificationsView(
    isSearchActive: Boolean,
    onClearSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Search else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (isSearchActive) LightBlue else TextMuted,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = if (isSearchActive) "No matching notifications" else "All caught up!",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isSearchActive) {
                    "No notifications match your search query. Try searching with a different term."
                } else {
                    "You have no notifications right now. Check back later for updates on jobs, stories, and platform activity."
                },
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isSearchActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onClearSearch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear search filter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ============================================================
// UNAUTHENTICATED PROMPT
// ============================================================
@Composable
private fun UnauthenticatedPrompt(onSignInClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
                .border(1.dp, BorderWhite10, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.15f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LightBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "Sign In Required",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sign in to view your real-time notification alerts, new job opportunities, application updates, and community story mentions.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onSignInClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("notification_signin_button")
                ) {
                    Text(
                        text = "Sign In / Join JobsReport",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
