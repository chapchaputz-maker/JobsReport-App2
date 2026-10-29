package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ads.AdConfig
import com.example.ads.NativeAdCard
import com.example.ads.StoryGridAdCard
import com.example.data.JobOpportunity
import com.example.data.StoryType
import com.example.data.StoryUser
import com.example.data.isUserVerified
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.VerifiedBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface ProfileStoryFeedItem {
    data class Story(val story: StoryType) : ProfileStoryFeedItem
    data class Ad(val adIndex: Int) : ProfileStoryFeedItem
}

// ============================================================
// CONSTANTS
// ============================================================
private const val PROFILE_JOBS_PER_PAGE = 6
private const val STORIES_PER_PAGE = 6
private const val TOP_STORIES_COUNT = 5

private val PageDarkBg = Color(0xFF050B18)
private val DialogDarkBg = Color(0xFF0B1120)
private val BrandBlue = Color(0xFF1877F2)
private val BrandPurple = Color(0xFF7B4DFF)
private val BrandPurpleEnd = Color(0xFFA033FF)
private val BrandCyan = Color(0xFF4A9EFF)
private val EmeraldText = Color(0xFF34D399)
private val CardBorderColor = Color(0x1AFFFFFF)
private val CardBg = Color(0x05FFFFFF)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray300 = Color(0xFFD1D5DB)
private val TextGray400 = Color(0xFF9CA3AF)
private val TextGray500 = Color(0xFF6B7280)
private val TextGray600 = Color(0xFF4B5563)

// ═══ FIX B — sanitizer, mirror of the one in AppState.kt
private fun String?.cleanText(): String? {
    if (this == null) return null
    val t = this.trim()
    if (t.isEmpty()) return null
    if (t.equals("null", ignoreCase = true)) return null
    return t
}

// ============================================================
// HELPERS
// ============================================================
private fun getDefaultAvatar(name: String, seed: String): String {
    val colors = listOf("1877F2", "45BD62", "F3425F", "F7B928", "9360F7")
    val hash = seed.fold(0) { acc, c -> acc + c.code }
    val c = colors[kotlin.math.abs(hash) % colors.size]
    val init = name.ifBlank { "U" }.take(1).uppercase()
    return "https://ui-avatars.com/api/?name=${Uri.encode(init)}&background=$c&color=fff&size=200&bold=true&rounded=true"
}

private fun fmtCount(n: Int): String {
    if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0)
    if (n >= 1_000) return String.format("%.1fK", n / 1_000.0)
    return n.toString()
}

private fun parseServerTime(value: String?): Long {
    val s = value?.trim().orEmpty()
    if (s.isEmpty()) return System.currentTimeMillis()
    return try {
        if (s.contains("T")) {
            val iso = if (!s.endsWith("Z") && !s.contains("+") && !s.substring(10).contains("-")) s + "Z" else s
            java.time.Instant.parse(iso).toEpochMilli()
        } else if (s.contains(" ")) {
            val iso = s.replace(" ", "T") + "Z"
            java.time.Instant.parse(iso).toEpochMilli()
        } else {
            s.toLongOrNull() ?: System.currentTimeMillis()
        }
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatRelativeTime(iso: String?): String {
    val t = parseServerTime(iso)
    val diff = System.currentTimeMillis() - t
    if (diff < 60_000) return "just now"
    if (diff < 3_600_000) return "${diff / 60_000}m"
    if (diff < 86_400_000) return "${diff / 3_600_000}h"
    if (diff < 604_800_000) return "${diff / 86_400_000}d"
    val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
    return sdf.format(java.util.Date(t))
}

private fun isStoryExpired(story: StoryType): Boolean {
    val activeVal = story.isActive
    if (activeVal == 0 || activeVal == false || activeVal == "0") return true
    val expiresAt = story.expiresAt
    if (expiresAt.isBlank()) return false
    return parseServerTime(expiresAt) < System.currentTimeMillis()
}

// ============================================================
// MAIN COMPONENT
// ============================================================
@Composable
fun UserProfilePage(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    BackHandler { onClose() }

    val baseUser = viewModel.viewingProfileUser ?: viewModel.currentUser
    var user by remember(baseUser?.id) { mutableStateOf(baseUser) }
    var jobs by remember { mutableStateOf<List<JobOpportunity>>(emptyList()) }
    var stories by remember { mutableStateOf<List<StoryType>>(emptyList()) }

    var activeTab by remember { mutableStateOf("jobs") }
    var isEditing by remember { mutableStateOf(false) }
    var following by remember(user?.id) {
        mutableStateOf(user?.isFollowing == true || (user != null && viewModel.isUserFollowed(user!!.id)))
    }
    var followLoading by remember { mutableStateOf(false) }

    var jobsVisible by remember { mutableIntStateOf(PROFILE_JOBS_PER_PAGE) }
    var storiesVisible by remember { mutableIntStateOf(STORIES_PER_PAGE) }

    var viewingStory by remember { mutableStateOf<StoryType?>(null) }
    var jobToDelete by remember { mutableStateOf<JobOpportunity?>(null) }
    var shareToast by remember { mutableStateOf<String?>(null) }

    // Upload state
    var uploadingCover by remember { mutableStateOf(false) }
    var uploadingAvatar by remember { mutableStateOf(false) }
    var uploadingDialogCover by remember { mutableStateOf(false) }
    var uploadingDialogAvatar by remember { mutableStateOf(false) }
    var pickTarget by remember { mutableStateOf<String?>(null) }
    var pendingCoverUrl by remember { mutableStateOf<String?>(null) }
    var pendingAvatarUrl by remember { mutableStateOf<String?>(null) }

    val authUser = viewModel.currentUser
    val isOwnProfile = authUser != null && user != null && authUser.id == user?.id
    val isAdmin = viewModel.isAdmin

    LaunchedEffect(shareToast) {
        if (shareToast != null) {
            delay(2500)
            shareToast = null
        }
    }

    LaunchedEffect(activeTab) {
        jobsVisible = PROFILE_JOBS_PER_PAGE
        storiesVisible = STORIES_PER_PAGE
    }

    LaunchedEffect(baseUser?.id) {
        if (baseUser != null) {
            val fetchedUser = viewModel.fetchUserProfile(baseUser.id)
            if (fetchedUser != null) {
                user = fetchedUser
                following = fetchedUser.isFollowing == true || viewModel.isUserFollowed(fetchedUser.id)
            }
            jobs = viewModel.fetchUserJobs(baseUser.id)
            stories = viewModel.stories.filter { it.userId == baseUser.id && !isStoryExpired(it) }
        }
    }

    val sortedJobs = remember(jobs) {
        jobs.sortedByDescending { parseServerTime(it.postedAt) }
    }
    val visibleJobs = remember(sortedJobs, jobsVisible) { sortedJobs.take(jobsVisible) }
    val hasMoreJobs = jobsVisible < sortedJobs.size

    val sortedStories = remember(stories, viewModel.stories, user?.id) {
        val list = if (user != null) viewModel.stories.filter { it.userId == user!!.id && !isStoryExpired(it) } else emptyList()
        list.sortedByDescending { parseServerTime(it.createdAt) }
    }
    val topStories = remember(sortedStories) { sortedStories.take(TOP_STORIES_COUNT) }
    val visibleStories = remember(sortedStories, storiesVisible) { sortedStories.take(storiesVisible) }
    val hasMoreStories = storiesVisible < sortedStories.size

    fun handleFollow() {
        val targetUser = user ?: return
        if (viewModel.userToken == null) { viewModel.openAuth(); return }
        if (followLoading) return
        followLoading = true
        val next = !following
        following = next
        scope.launch {
            viewModel.toggleFollowUser(targetUser.id)
            viewModel.followUserApi(targetUser.id, next)
            user = user?.copy(
                followersCount = ((user?.followersCount ?: 0) + if (next) 1 else -1).coerceAtLeast(0),
                isFollowing = next
            )
            followLoading = false
        }
    }

    fun handleShareProfile() {
        val targetUser = user ?: return
        val handleStr = targetUser.username ?: targetUser.id
        val profileUrl = "https://jobsreport.online/u/$handleStr"
        clipboardManager.setText(AnnotatedString(profileUrl))
        shareToast = "Link copied to clipboard!"
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out ${targetUser.name}'s profile on JobsReport: $profileUrl")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
        } catch (_: Exception) {}
    }

    fun handleDeleteJob(job: JobOpportunity) {
        scope.launch {
            viewModel.deleteUserJob(job.id)
            jobs = jobs.filter { it.id != job.id }
            user = user?.copy(jobsCount = ((user?.jobsCount ?: 1) - 1).coerceAtLeast(0))
            jobToDelete = null
            shareToast = "Job deleted"
        }
    }

    fun handleDeleteStory(storyId: String) {
        scope.launch {
            viewModel.deleteStory(storyId)
            stories = stories.filter { it.id != storyId }
            user = user?.copy(storiesCount = ((user?.storiesCount ?: 1) - 1).coerceAtLeast(0))
            shareToast = "Story deleted"
        }
    }

    val displayUser = user ?: baseUser
    if (displayUser == null) {
        Box(modifier = Modifier.fillMaxSize().background(PageDarkBg), contentAlignment = Alignment.Center) {
            Text("User not found", color = TextWhite, fontWeight = FontWeight.Bold)
        }
        return
    }

    // ═══ FIX A + C — picker callback:
    //  - preserves spinner reset in all code paths
    //  - prefers freshly-uploaded URL over stale ViewModel currentUser
    //  - updates BOTH user state and viewModel.currentUser / viewingProfileUser
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val target = pickTarget
        pickTarget = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult

        fun commitUpload(url: String, isCover: Boolean, dialogMode: Boolean) {
            if (dialogMode) {
                if (isCover) pendingCoverUrl = url else pendingAvatarUrl = url
                return
            }
            // Persist and update both local state and the ViewModel
            scope.launch {
                try {
                    val saved = viewModel.saveProfileApi(
                        avatarUrl = if (isCover) null else url,
                        coverUrl = if (isCover) url else null
                    )
                    if (!saved) {
                        shareToast = "Uploaded but failed to save to profile"
                        return@launch
                    }
                    // Prefer local copy so we don't read stale ViewModel data
                    val updated = user?.copy(
                        profileImageUrl = if (isCover) user?.profileImageUrl else url,
                        coverImageUrl = if (isCover) url else user?.coverImageUrl
                    ) ?: viewModel.currentUser
                    if (updated != null) {
                        user = updated
                        viewModel.viewingProfileUser = updated
                        if (viewModel.currentUser?.id == updated.id) {
                            viewModel.currentUser = updated
                        }
                    }
                    shareToast = if (isCover) "Cover updated!" else "Avatar updated!"
                } finally {
                    // always reset spinner
                    if (isCover) uploadingCover = false else uploadingAvatar = false
                }
            }
        }

        when (target) {
            "cover" -> {
                uploadingCover = true
                scope.launch {
                    val url = viewModel.uploadImage(context, uri)
                    if (url == null) {
                        shareToast = viewModel.uploadError ?: "Cover upload failed"
                        uploadingCover = false
                        return@launch
                    }
                    commitUpload(url, isCover = true, dialogMode = false)
                }
            }
            "avatar" -> {
                uploadingAvatar = true
                scope.launch {
                    val url = viewModel.uploadImage(context, uri)
                    if (url == null) {
                        shareToast = viewModel.uploadError ?: "Avatar upload failed"
                        uploadingAvatar = false
                        return@launch
                    }
                    commitUpload(url, isCover = false, dialogMode = false)
                }
            }
            "dialogCover" -> {
                uploadingDialogCover = true
                scope.launch {
                    try {
                        val url = viewModel.uploadImage(context, uri)
                        if (url != null) {
                            pendingCoverUrl = url
                        } else {
                            shareToast = viewModel.uploadError ?: "Cover upload failed"
                        }
                    } finally {
                        uploadingDialogCover = false
                    }
                }
            }
            "dialogAvatar" -> {
                uploadingDialogAvatar = true
                scope.launch {
                    try {
                        val url = viewModel.uploadImage(context, uri)
                        if (url != null) {
                            pendingAvatarUrl = url
                        } else {
                            shareToast = viewModel.uploadError ?: "Avatar upload failed"
                        }
                    } finally {
                        uploadingDialogAvatar = false
                    }
                }
            }
        }
    }

    // ═══ FIX B — sanitize display values so "null" never renders as text
    val safeHeadline = displayUser.headline.cleanText()
    val safeBio = displayUser.bio.cleanText()
    val safeWork = displayUser.work.cleanText()
    val safeEducation = displayUser.education.cleanText()
    val safeLocation = displayUser.location.cleanText()
    val safeWebsite = displayUser.website.cleanText()

    val handle = if (!displayUser.username.isNullOrBlank()) "@${displayUser.username}" else (displayUser.email?.substringBefore("@")?.let { "@$it" } ?: "@user")
    val avatar = if (!displayUser.profileImageUrl.isNullOrBlank()) displayUser.profileImageUrl!! else getDefaultAvatar(displayUser.name, displayUser.id)
    val isVerified = displayUser.isVerified == 1 || displayUser.isVerified == true || displayUser.isVerified == "1" || displayUser.isUserVerified || viewModel.isUserVerified(displayUser.id, displayUser)

    Box(modifier = Modifier.fillMaxSize().background(PageDarkBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // COVER
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        BrandBlue.copy(alpha = 0.4f),
                                        BrandPurple.copy(alpha = 0.3f),
                                        BrandPurpleEnd.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    ) {
                        if (!displayUser.coverImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = displayUser.coverImageUrl,
                                contentDescription = "Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "NO COVER YET",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, PageDarkBg.copy(alpha = 0.6f), PageDarkBg)
                                    )
                                )
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(12.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isOwnProfile) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .clickable(enabled = !uploadingCover) {
                                        if (viewModel.userToken.isNullOrBlank()) {
                                            viewModel.openAuth()
                                            shareToast = "Please sign in first"
                                        } else {
                                            pickTarget = "cover"
                                            imagePicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                if (uploadingCover) {
                                    CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(13.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CameraAlt, null, tint = TextWhite, modifier = Modifier.size(13.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uploadingCover) "Uploading…" else "Edit cover",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // AVATAR + INFO
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth().offset(y = (-50).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple, BrandPurpleEnd)))
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DialogDarkBg)
                                        .border(3.dp, PageDarkBg, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = avatar,
                                        contentDescription = displayUser.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (isOwnProfile) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(DialogDarkBg)
                                            .border(2.dp, PageDarkBg, CircleShape)
                                            .clickable(enabled = !uploadingAvatar) {
                                                if (viewModel.userToken.isNullOrBlank()) {
                                                    viewModel.openAuth()
                                                    shareToast = "Please sign in first"
                                                } else {
                                                    pickTarget = "avatar"
                                                    imagePicker.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uploadingAvatar) {
                                            CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(11.dp), strokeWidth = 1.5.dp)
                                        } else {
                                            Icon(Icons.Default.CameraAlt, "Edit avatar", tint = TextWhite, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f).padding(bottom = 6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = displayUser.name,
                                        color = TextWhite,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isVerified) VerifiedBadge(size = 18.dp)
                                }
                                Text(text = handle, color = TextGray500, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Column(modifier = Modifier.offset(y = (-36).dp)) {
                            if (!safeHeadline.isNullOrBlank()) {
                                Text(
                                    text = safeHeadline,
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            if (!safeBio.isNullOrBlank()) {
                                Text(
                                    text = safeBio,
                                    color = TextGray300,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!safeWork.isNullOrBlank()) InfoTag(icon = Icons.Default.Work, text = safeWork)
                                if (!safeEducation.isNullOrBlank()) InfoTag(icon = Icons.Default.School, text = safeEducation)
                                if (!safeLocation.isNullOrBlank()) InfoTag(icon = Icons.Default.LocationOn, text = safeLocation)
                                if (!safeWebsite.isNullOrBlank()) {
                                    val cleanUrl = safeWebsite.removePrefix("https://").removePrefix("http://")
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            try {
                                                val uri = if (safeWebsite.startsWith("http")) Uri.parse(safeWebsite) else Uri.parse("https://$safeWebsite")
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            } catch (_: Exception) {}
                                        }
                                    ) {
                                        Icon(Icons.Default.Language, null, tint = BrandCyan, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(cleanUrl, color = BrandCyan, fontSize = 12.5.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CountItem(count = fmtCount(displayUser.followersCount ?: 0), label = "followers")
                                CountItem(count = fmtCount(sortedJobs.size), label = "job adverts")
                                CountItem(count = fmtCount(sortedStories.size), label = "stories")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isOwnProfile) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (viewModel.userToken.isNullOrBlank()) {
                                                    viewModel.openAuth()
                                                    shareToast = "Please sign in first"
                                                } else {
                                                    isEditing = true
                                                }
                                            }
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = TextWhite, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Profile", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                                            .background(Brush.horizontalGradient(listOf(BrandBlue, BrandPurple)))
                                            .clickable { viewModel.openPostJob(NavRoute.USER_PROFILE) }
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = TextWhite, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Post a Job", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp).clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.isCreateStoryOpen = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CameraAlt, "New Story", tint = TextWhite, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp))
                                            .background(if (following) Color.White.copy(alpha = 0.04f) else BrandBlue)
                                            .border(1.dp, if (following) CardBorderColor else Color.Transparent, RoundedCornerShape(12.dp))
                                            .clickable(enabled = !followLoading) { handleFollow() }
                                    ) {
                                        if (followLoading) {
                                            CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(
                                                imageVector = if (following) Icons.Default.Check else Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                tint = TextWhite,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (following) "Following" else "Follow", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp).clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                            .clickable { handleShareProfile() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Share, "Share", tint = TextWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            if (topStories.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        topStories.forEach { s ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.clickable { viewingStory = s }.width(64.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp).clip(CircleShape)
                                                        .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple, BrandPurpleEnd)))
                                                        .padding(2.5.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize().clip(CircleShape)
                                                            .background(DialogDarkBg)
                                                            .border(3.dp, PageDarkBg, CircleShape)
                                                    ) {
                                                        if (s.type == "text" && s.mediaThumb.isNullOrBlank()) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = s.textContent?.take(12).orEmpty(),
                                                                    color = Color.White, fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Black,
                                                                    textAlign = TextAlign.Center, maxLines = 2
                                                                )
                                                            }
                                                        } else {
                                                            AsyncImage(
                                                                model = s.mediaThumb ?: s.mediaUrl,
                                                                contentDescription = null,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = formatRelativeTime(s.createdAt),
                                                    color = TextGray400, fontSize = 10.sp,
                                                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth().padding(top = 18.dp)
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileTabButton("Job Adverts", Icons.Default.Work, sortedJobs.size, activeTab == "jobs", { activeTab = "jobs" }, Modifier.weight(1f))
                                ProfileTabButton("Stories", Icons.Default.CameraAlt, sortedStories.size, activeTab == "stories", { activeTab = "stories" }, Modifier.weight(1f))
                                ProfileTabButton("About", Icons.Default.Forum, null, activeTab == "about", { activeTab = "about" }, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // TAB CONTENT
                when (activeTab) {
                    "jobs" -> {
                        if (isOwnProfile) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "YOUR JOB POSTINGS (${sortedJobs.size})",
                                        color = TextGray400, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                                    )
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.horizontalGradient(listOf(BrandBlue, BrandPurple)))
                                            .clickable { viewModel.openPostJob(NavRoute.USER_PROFILE) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("POST A JOB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        if (sortedJobs.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.Work,
                                    title = "No job adverts yet",
                                    subtitle = if (isOwnProfile) "Post a job to attract candidates" else "This user hasn't posted any jobs yet",
                                    actionLabel = if (isOwnProfile) "Post a Job" else null,
                                    onAction = { viewModel.openPostJob(NavRoute.USER_PROFILE) }
                                )
                            }
                        } else {
                            visibleJobs.forEachIndexed { index, job ->
                                item(key = "user_job_${job.id}") {
                                    val canManage = authUser != null && (isAdmin || isOwnProfile)
                                    NativeJobCard(
                                        job = job,
                                        canManage = canManage,
                                        onSelect = {
                                            viewModel.selectJob(job)
                                            viewModel.navigateTo(NavRoute.JOB_DETAIL)
                                        },
                                        onEdit = { viewModel.openEditJob(job, NavRoute.USER_PROFILE) },
                                        onDelete = { jobToDelete = job }
                                    )
                                }
                                if ((index + 1) % 5 == 0) {
                                    item(key = "user_job_ad_$index") {
                                        NativeAdCard(
                                            adUnitId = AdConfig.getNativeAdUnitId((index + 1) / 5),
                                            slotKey = "profile_job_ad_$index"
                                        )
                                    }
                                }
                            }

                            item {
                                if (hasMoreJobs) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.04f))
                                                .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                                .clickable { jobsVisible += PROFILE_JOBS_PER_PAGE }
                                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "SEE MORE (${sortedJobs.size - jobsVisible} REMAINING)",
                                                color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                } else if (jobsVisible > PROFILE_JOBS_PER_PAGE) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "SHOW LESS",
                                            color = TextGray500, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                                            modifier = Modifier.clickable { jobsVisible = PROFILE_JOBS_PER_PAGE }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "stories" -> {
                        if (sortedStories.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.CameraAlt,
                                    title = "No stories yet",
                                    subtitle = if (isOwnProfile) "Share your first story — job wins, tips, updates" else "This user hasn't shared any stories yet",
                                    actionLabel = if (isOwnProfile) "Create Story" else null,
                                    onAction = {
                                        viewModel.isCreateStoryOpen = true
                                    }
                                )
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val storyDisplayItems = remember(visibleStories) {
                                        val items = mutableListOf<ProfileStoryFeedItem>()
                                        var adCount = 0
                                        visibleStories.forEachIndexed { index, story ->
                                            items.add(ProfileStoryFeedItem.Story(story))
                                            if ((index + 1) % 4 == 0) {
                                                items.add(ProfileStoryFeedItem.Ad(adCount++))
                                            }
                                        }
                                        items
                                    }

                                    val rows = storyDisplayItems.chunked(2)
                                    rows.forEach { pair ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            pair.forEach { item ->
                                                Box(modifier = Modifier.weight(1f)) {
                                                    when (item) {
                                                        is ProfileStoryFeedItem.Story -> {
                                                            NativeStoryCard(story = item.story, onClick = { viewingStory = item.story })
                                                        }
                                                        is ProfileStoryFeedItem.Ad -> {
                                                            StoryGridAdCard(
                                                                adUnitId = AdConfig.getNativeAdUnitId(item.adIndex),
                                                                slotKey = "profile_story_grid_${item.adIndex}"
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }

                                    if (hasMoreStories) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White.copy(alpha = 0.04f))
                                                    .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                                                    .clickable { storiesVisible += STORIES_PER_PAGE }
                                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "SEE MORE (${sortedStories.size - storiesVisible} REMAINING)",
                                                    color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    } else if (storiesVisible > STORIES_PER_PAGE) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "SHOW LESS",
                                                color = TextGray500, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                                                modifier = Modifier.clickable { storiesVisible = STORIES_PER_PAGE }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "about" -> {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AboutCard(Icons.Default.Forum, "Bio", safeBio ?: if (isOwnProfile) "Add a short bio" else "No bio available", safeBio.isNullOrBlank(), onClick = if (isOwnProfile && safeBio.isNullOrBlank()) { { if (viewModel.userToken.isNullOrBlank()) { viewModel.openAuth(); shareToast = "Please sign in first" } else isEditing = true } } else null)
                                AboutCard(Icons.Default.Work, "Work", safeWork ?: if (isOwnProfile) "Add current company" else "Not specified", safeWork.isNullOrBlank(), onClick = if (isOwnProfile && safeWork.isNullOrBlank()) { { if (viewModel.userToken.isNullOrBlank()) { viewModel.openAuth(); shareToast = "Please sign in first" } else isEditing = true } } else null)
                                AboutCard(Icons.Default.School, "Education", safeEducation ?: if (isOwnProfile) "Add education" else "Not specified", safeEducation.isNullOrBlank(), onClick = if (isOwnProfile && safeEducation.isNullOrBlank()) { { if (viewModel.userToken.isNullOrBlank()) { viewModel.openAuth(); shareToast = "Please sign in first" } else isEditing = true } } else null)
                                AboutCard(Icons.Default.LocationOn, "Location", safeLocation ?: if (isOwnProfile) "Add location" else "Not specified", safeLocation.isNullOrBlank(), onClick = if (isOwnProfile && safeLocation.isNullOrBlank()) { { if (viewModel.userToken.isNullOrBlank()) { viewModel.openAuth(); shareToast = "Please sign in first" } else isEditing = true } } else null)
                                if (!safeWebsite.isNullOrBlank()) {
                                    AboutCard(Icons.Default.Language, "Website", safeWebsite, false, link = safeWebsite)
                                }
                                AboutCard(
                                    Icons.Default.CalendarToday, "Joined",
                                    displayUser.createdAt?.let {
                                        try {
                                            val t = parseServerTime(it)
                                            java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US).format(java.util.Date(t))
                                        } catch (_: Exception) { "October 2026" }
                                    } ?: "October 2026",
                                    false
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }

        // TOAST
        AnimatedVisibility(
            visible = shareToast != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(DialogDarkBg)
                    .border(1.dp, EmeraldText.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = EmeraldText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(shareToast.orEmpty(), color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ═══ FIX B + C — Edit Profile dialog now seeds from sanitized values
        // and passes "" (not null) for cleared fields.
        if (isEditing) {
            EditProfileDialog(
                user = displayUser,
                initialCoverUrl = pendingCoverUrl ?: displayUser.coverImageUrl,
                initialAvatarUrl = pendingAvatarUrl ?: displayUser.profileImageUrl,
                uploadingCover = uploadingDialogCover,
                uploadingAvatar = uploadingDialogAvatar,
                onPickCover = {
                    if (viewModel.userToken.isNullOrBlank()) {
                        viewModel.openAuth()
                        shareToast = "Please sign in first"
                    } else {
                        pickTarget = "dialogCover"
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                onPickAvatar = {
                    if (viewModel.userToken.isNullOrBlank()) {
                        viewModel.openAuth()
                        shareToast = "Please sign in first"
                    } else {
                        pickTarget = "dialogAvatar"
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                onClose = {
                    isEditing = false
                    pendingCoverUrl = null
                    pendingAvatarUrl = null
                },
                onSave = { name, headline, bio, work, education, location, website, avatarUrl, coverUrl ->
                    if (viewModel.userToken.isNullOrBlank()) {
                        viewModel.openAuth()
                        shareToast = "Please sign in to save profile"
                        return@EditProfileDialog
                    }
                    scope.launch {
                        val ok = viewModel.saveProfileApi(
                            name = name,
                            headline = headline,
                            bio = bio,
                            work = work,
                            education = education,
                            location = location,
                            website = website,
                            avatarUrl = avatarUrl,
                            coverUrl = coverUrl
                        )
                        if (ok) {
                            // Mirror in local state so the page reflects the change immediately,
                            // even before ViewModel's next refresh.
                            val updated = user?.copy(
                                name = name,
                                headline = headline.cleanText(),
                                bio = bio.cleanText(),
                                work = work.cleanText(),
                                education = education.cleanText(),
                                location = location.cleanText(),
                                website = website.cleanText(),
                                profileImageUrl = avatarUrl ?: user?.profileImageUrl,
                                coverImageUrl = coverUrl ?: user?.coverImageUrl
                            ) ?: viewModel.currentUser
                            if (updated != null) {
                                user = updated
                                viewModel.viewingProfileUser = updated
                                if (viewModel.currentUser?.id == updated.id) {
                                    viewModel.currentUser = updated
                                }
                            }
                            isEditing = false
                            pendingCoverUrl = null
                            pendingAvatarUrl = null
                            shareToast = "Profile saved!"
                        } else {
                            shareToast = "Failed to save profile"
                        }
                    }
                }
            )
        }

        if (jobToDelete != null) {
            DeleteJobDialog(
                job = jobToDelete!!,
                onCancel = { jobToDelete = null },
                onConfirm = { handleDeleteJob(jobToDelete!!) }
            )
        }

        if (viewingStory != null) {
            val storyItem = viewingStory!!
            StoryViewer(
                story = storyItem,
                user = displayUser,
                currentUser = authUser,
                isVerified = isVerified,
                allStories = sortedStories,
                onClose = { viewingStory = null },
                onNext = {
                    val idx = sortedStories.indexOfFirst { it.id == storyItem.id }
                    if (idx >= 0 && idx + 1 < sortedStories.size) viewingStory = sortedStories[idx + 1] else viewingStory = null
                },
                onPrev = {
                    val idx = sortedStories.indexOfFirst { it.id == storyItem.id }
                    if (idx > 0) viewingStory = sortedStories[idx - 1] else viewingStory = null
                },
                onDeleteStory = { sId -> handleDeleteStory(sId); viewingStory = null },
                onViewRecorded = { sId -> viewModel.recordStoryView(sId) },
                onProfileClick = { pUser -> viewingStory = null; viewModel.openUserProfile(pUser) }
            )
        }
    }
}

// ============================================================
// PROFILE TAB BUTTON
// ============================================================
@Composable
private fun ProfileTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) TextWhite else TextGray500, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title,
                color = if (isSelected) TextWhite else TextGray500,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) BrandBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text("$count", color = if (isSelected) BrandCyan else TextGray500, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(Modifier.width(40.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(Brush.horizontalGradient(listOf(BrandBlue, BrandPurpleEnd))))
        } else {
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun InfoTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextGray500, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = TextGray400, fontSize = 12.5.sp)
    }
}

@Composable
private fun CountItem(count: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(count, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextGray500, fontSize = 13.sp)
    }
}

// ============================================================
// NATIVE JOB CARD
// ============================================================
@Composable
private fun NativeJobCard(
    job: JobOpportunity,
    canManage: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!job.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = job.logoUrl,
                        contentDescription = job.company,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(job.company.take(1).uppercase().ifBlank { "?" }, color = TextGray400, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (!job.role.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(job.role!!.uppercase(), color = BrandCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(job.title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Work, null, tint = TextGray500, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(job.company, color = TextGray500, fontSize = 11.sp, maxLines = 1)
                    }
                    if (!job.location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = TextGray500, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(job.location!!, color = TextGray500, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                if (!job.salary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(job.salary!!, color = EmeraldText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            if (canManage) {
                Box {
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, "Options", tint = TextGray400, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier.background(DialogDarkBg).border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, null, tint = BrandCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Job", color = TextWhite, fontSize = 12.sp)
                                }
                            },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete Job", color = Color(0xFFF87171), fontSize = 12.sp)
                                }
                            },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// NATIVE STORY CARD
// ============================================================
@Composable
private fun NativeStoryCard(story: StoryType, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        if (story.type == "text" && story.mediaThumb.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent.orEmpty(),
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, maxLines = 6, overflow = TextOverflow.Ellipsis
                )
            }
        } else if (!story.mediaThumb.isNullOrBlank() || !story.mediaUrl.isNullOrBlank()) {
            AsyncImage(story.mediaThumb ?: story.mediaUrl, "Story", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandBlue, BrandPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, null, tint = TextWhite, modifier = Modifier.size(28.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Visibility, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(10.dp))
                Text(
                    "${fmtCount(story.viewsCount ?: 0)} · ${formatRelativeTime(story.createdAt)}",
                    color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================
// ABOUT CARD
// ============================================================
@Composable
private fun AboutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isMuted: Boolean = false,
    link: String? = null,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null || link != null) {
                if (onClick != null) onClick()
                else if (link != null) {
                    try {
                        val uri = if (link.startsWith("http")) Uri.parse(link) else Uri.parse("https://$link")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (_: Exception) {}
                }
            }
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextGray400, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title.uppercase(), color = TextGray500, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(2.dp))
            if (link != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, color = BrandCyan, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, null, tint = BrandCyan, modifier = Modifier.size(11.dp))
                }
            } else {
                Text(
                    text = value,
                    color = if (isMuted) TextGray500 else TextWhite,
                    fontSize = 13.sp,
                    fontStyle = if (isMuted) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ============================================================
// EMPTY STATE
// ============================================================
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.01f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextGray500, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle, color = TextGray500, fontSize = 12.sp,
            textAlign = TextAlign.Center, lineHeight = 16.sp,
            modifier = Modifier.widthIn(max = 280.dp)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandBlue)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = TextWhite, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(actionLabel, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================
// EDIT PROFILE DIALOG
// ============================================================
@Composable
private fun EditProfileDialog(
    user: StoryUser,
    initialCoverUrl: String?,
    initialAvatarUrl: String?,
    uploadingCover: Boolean,
    uploadingAvatar: Boolean,
    onPickCover: () -> Unit,
    onPickAvatar: () -> Unit,
    onClose: () -> Unit,
    onSave: (
        name: String,
        headline: String,
        bio: String,
        work: String,
        education: String,
        location: String,
        website: String,
        avatarUrl: String?,
        coverUrl: String?
    ) -> Unit
) {
    // ═══ FIX B — seed from sanitized values so the field starts empty, never "null"
    var name by remember(user.id) { mutableStateOf(user.name.cleanText().orEmpty()) }
    var headline by remember(user.id) { mutableStateOf(user.headline.cleanText().orEmpty()) }
    var bio by remember(user.id) { mutableStateOf(user.bio.cleanText().orEmpty()) }
    var work by remember(user.id) { mutableStateOf(user.work.cleanText().orEmpty()) }
    var education by remember(user.id) { mutableStateOf(user.education.cleanText().orEmpty()) }
    var location by remember(user.id) { mutableStateOf(user.location.cleanText().orEmpty()) }
    var website by remember(user.id) { mutableStateOf(user.website.cleanText().orEmpty()) }
    var avatarUrl by remember(initialAvatarUrl) { mutableStateOf(initialAvatarUrl.cleanText().orEmpty()) }
    var coverUrl by remember(initialCoverUrl) { mutableStateOf(initialCoverUrl.cleanText().orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DialogDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Profile", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = TextGray400, modifier = Modifier.size(16.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(100.dp)
                        .background(Brush.linearGradient(
                            listOf(
                                BrandBlue.copy(alpha = 0.3f),
                                BrandPurple.copy(alpha = 0.2f),
                                BrandPurpleEnd.copy(alpha = 0.3f)
                            )
                        ))
                ) {
                    if (coverUrl.isNotBlank()) {
                        AsyncImage(coverUrl, "Cover", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 20.dp).offset(y = (-30).dp).size(68.dp)
                        .clip(CircleShape).background(DialogDarkBg)
                        .border(3.dp, DialogDarkBg, CircleShape)
                ) {
                    AsyncImage(
                        avatarUrl.ifBlank { getDefaultAvatar(name, user.id) },
                        "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().offset(y = (-16).dp).padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .weight(1f).height(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable(enabled = !uploadingCover, onClick = onPickCover),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (uploadingCover) CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            else Icon(Icons.Default.CameraAlt, null, tint = TextWhite, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uploadingCover) "Uploading…" else "Change cover", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier
                                .weight(1f).height(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable(enabled = !uploadingAvatar, onClick = onPickAvatar),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (uploadingAvatar) CircularProgressIndicator(color = TextWhite, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                            else Icon(Icons.Default.CameraAlt, null, tint = TextWhite, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uploadingAvatar) "Uploading…" else "Change avatar", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    EditField("NAME", name, { name = it }, "Your name", isRequired = true)
                    EditField("HEADLINE", headline, { headline = it }, "e.g. Software Engineer • Nairobi")
                    EditField("BIO", bio, { bio = it }, "Short bio", isTextArea = true)
                    EditField("WORK", work, { work = it }, "Current company", icon = Icons.Default.Work)
                    EditField("EDUCATION", education, { education = it }, "School or university", icon = Icons.Default.School)
                    EditField("LOCATION", location, { location = it }, "City, Country", icon = Icons.Default.LocationOn)
                    EditField("WEBSITE", website, { website = it }, "https://...", icon = Icons.Default.Language)
                    EditField("AVATAR IMAGE URL", avatarUrl, { avatarUrl = it }, "https://...")
                    EditField("COVER IMAGE URL", coverUrl, { coverUrl = it }, "https://...")

                    if (error != null) Text(error!!, color = Color(0xFFF87171), fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f)).clickable(onClick = onClose),
                            contentAlignment = Alignment.Center
                        ) { Text("Cancel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp) }

                        Box(
                            modifier = Modifier
                                .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(BrandBlue).clickable {
                                    if (name.isBlank()) { error = "Name is required"; return@clickable }
                                    // ═══ FIX B + C — send "" for text fields so clearing works.
                                    // URL fields still send null when empty so the DB doesn't get "".
                                    onSave(
                                        name.trim(),
                                        headline.trim(),
                                        bio.trim(),
                                        work.trim(),
                                        education.trim(),
                                        location.trim(),
                                        website.trim(),
                                        avatarUrl.trim().ifBlank { null },
                                        coverUrl.trim().ifBlank { null }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) { Text("Save", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isRequired: Boolean = false,
    isTextArea: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = TextGray400, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(label, color = TextGray400, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            if (isRequired) Text(" *", color = Color(0xFFEF4444), fontSize = 10.sp)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray600, fontSize = 13.sp) },
            singleLine = !isTextArea,
            maxLines = if (isTextArea) 3 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================
// DELETE JOB DIALOG
// ============================================================
@Composable
private fun DeleteJobDialog(job: JobOpportunity, onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DialogDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp).clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Delete this job?", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("This action cannot be undone.", color = TextGray500, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.02f))
                        .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("JOB", color = TextGray500, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(job.title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(job.company, color = TextGray500, fontSize = 11.sp)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f)).clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) { Text("Cancel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    Box(
                        modifier = Modifier
                            .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDC2626)).clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, null, tint = TextWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
