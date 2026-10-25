package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CreateStoryPayload
import com.example.data.STORY_BACKGROUNDS
import com.example.data.StoryType
import com.example.data.StoryUser
import com.example.data.isUserVerified
import com.example.data.formatStoryCount
import com.example.data.formatStoryTime
import com.example.data.getDefaultStoryAvatar
import com.example.data.isStoryExpired
import com.example.data.parseStoryServerTime
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CompressedMediaResult
import com.example.util.ImageCompressor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

// ============================================================
// STORY COLOR PALETTE (matching Story.tsx)
// ============================================================
private val StoryBgDeep = Color(0xFF050B18)
private val StoryBgSurface = Color(0xFF0B1120)
private val StoryBgCard = Color(0xFF0F172A)
private val StoryBorderSlate = Color(0xFF1E293B)
private val StoryInputDark = Color(0xFF070D1D)
private val StoryPrimaryBlue = Color(0xFF1877F2)
private val StoryLightBlueHover = Color(0xFF166FE5)
private val StoryDangerRed = Color(0xFFF3425F)
private val StoryDangerRedDark = Color(0xFFE41E3F)
private val StoryTextWhite = Color(0xFFF8FAFC)

fun getStoryBrush(style: String?): Brush {
    val s = style?.trim().orEmpty()
    return when {
        s.contains("#1877F2") || s.contains("#0055FF") -> Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF0055FF)))
        s.contains("#F3425F") || s.contains("#E41E3F") -> Brush.linearGradient(listOf(Color(0xFFF3425F), Color(0xFFE41E3F)))
        s.contains("#45BD62") || s.contains("#31A24C") -> Brush.linearGradient(listOf(Color(0xFF45BD62), Color(0xFF31A24C)))
        s.contains("#F7B928") || s.contains("#E3A300") -> Brush.linearGradient(listOf(Color(0xFFF7B928), Color(0xFFE3A300)))
        s.contains("#A033FF") || s.contains("#7B1FA2") -> Brush.linearGradient(listOf(Color(0xFFA033FF), Color(0xFF7B1FA2)))
        s.contains("#FF7E5F") || s.contains("#FEB47B") -> Brush.linearGradient(listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)))
        s.contains("#00C6FF") || s.contains("#0072FF") -> Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
        s.contains("#2193b0") || s.contains("#6dd5ed") -> Brush.linearGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED)))
        s.contains("#42275a") || s.contains("#734b6d") -> Brush.linearGradient(listOf(Color(0xFF42275A), Color(0xFF734B6D)))
        s.contains("#000000") || s.contains("#434343") -> Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFF434343)))
        else -> Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF0055FF)))
    }
}

// ============================================================
// MAIN SCREEN — StoryPage (/stories)
// ============================================================
@Composable
fun StoryScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED)
    }

    val activeStories = remember(viewModel.stories) {
        viewModel.stories.filter { !isStoryExpired(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StoryBgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Screen Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Job Stories",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real stories from job seekers and professionals. Share yours.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    // Profile / Account shortcut
                    IconButton(
                        onClick = {
                            if (viewModel.isLoggedIn) {
                                viewModel.openUserProfile(null)
                            } else {
                                viewModel.openAuth()
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StoryBgCard)
                            .border(1.dp, StoryBorderSlate, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = LightBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // STORY REEL (Horizontal Carousel) — keeps its card chrome here
            StoryReel(
                stories = activeStories,
                loading = viewModel.isStoriesLoading,
                currentUser = viewModel.currentUser,
                isUserVerified = { uid, u -> viewModel.isUserVerified(uid, u) },
                authorProvider = { uid, u -> viewModel.getResolvedUser(uid, u) },
                onCreateStory = {
                    viewModel.isCreateStoryOpen = true
                },
                onViewStory = { story -> viewModel.viewingStory = story },
                onProfileClick = { authorId ->
                    val author = viewModel.getResolvedUser(authorId, activeStories.firstOrNull { it.userId == authorId }?.user)
                    viewModel.openUserProfile(author)
                },
                onRequestLogin = { viewModel.openAuth() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle & Refresh Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMUNITY FEED",
                    color = LightBlue,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                IconButton(
                    onClick = { viewModel.loadStories() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Stories",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Empty state
            if (activeStories.isEmpty() && !viewModel.isStoriesLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active stories yet. Be the first to share one.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Grid of Stories
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                val chunkedStories = activeStories.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunkedStories.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { story ->
                                Box(modifier = Modifier.weight(1f)) {
                                    StoryGridCard(
                                        story = story,
                                        author = viewModel.getResolvedUser(story.userId, story.user),
                                        isVerified = viewModel.isUserVerified(story.userId, story.user),
                                        onClick = { viewModel.viewingStory = story },
                                        onProfileClick = {
                                            val authorUser = viewModel.getResolvedUser(story.userId, story.user)
                                            viewModel.openUserProfile(authorUser)
                                        }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// GLOBAL STORY MODALS & OVERLAYS
// ============================================================
@Composable
fun GlobalStoryModals(viewModel: AppViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activeStories = remember(viewModel.stories) {
        viewModel.stories.filter { !isStoryExpired(it) }
    }

    // STORY VIEWER MODAL
    if (viewModel.viewingStory != null) {
        val currentStory = viewModel.viewingStory!!
        val author = viewModel.getResolvedUser(currentStory.userId, currentStory.user)
        val isVerified = viewModel.isUserVerified(currentStory.userId, author)
        StoryViewer(
            story = currentStory,
            user = author,
            currentUser = viewModel.currentUser,
            isVerified = isVerified,
            allStories = activeStories,
            onClose = { viewModel.viewingStory = null },
            onNext = {
                val idx = activeStories.indexOfFirst { it.id == currentStory.id }
                if (idx >= 0 && idx + 1 < activeStories.size) {
                    viewModel.viewingStory = activeStories[idx + 1]
                } else {
                    viewModel.viewingStory = null
                }
            },
            onPrev = {
                val idx = activeStories.indexOfFirst { it.id == currentStory.id }
                if (idx > 0) {
                    viewModel.viewingStory = activeStories[idx - 1]
                } else {
                    viewModel.viewingStory = null
                }
            },
            onDeleteStory = { storyId ->
                coroutineScope.launch {
                    viewModel.deleteStory(storyId)
                    Toast.makeText(context, "Story deleted", Toast.LENGTH_SHORT).show()
                }
            },
            onViewRecorded = { storyId ->
                viewModel.recordStoryView(storyId)
            },
            onProfileClick = { authorUser ->
                viewModel.viewingStory = null
                viewModel.openUserProfile(authorUser)
            }
        )
    }

    // CREATE STORY MODAL
    if (viewModel.isCreateStoryOpen) {
        CreateStoryModal(
            viewModel = viewModel,
            currentUser = viewModel.currentUser ?: StoryUser(
                id = "anon",
                name = "User"
            ),
            onClose = {
                viewModel.clearStoryDraft()
                viewModel.isCreateStoryOpen = false
            },
            onCreate = { payload ->
                viewModel.createStory(payload)
            },
            onUpload = { mainBytes, mainName, mainMime, thumbBytes, thumbName, thumbMime ->
                viewModel.uploadMedia(mainBytes, mainName, mainMime, thumbBytes, thumbName, thumbMime)
            },
            onRequestLogin = {
                viewModel.openAuth()
            }
        )
    }
}

// ============================================================
// STORY REEL (Horizontal Carousel) — now supports flat mode
// ============================================================
@Composable
fun StoryReel(
    stories: List<StoryType>,
    loading: Boolean = false,
    currentUser: StoryUser?,
    isUserVerified: ((String, StoryUser?) -> Boolean)? = null,
    authorProvider: ((String, StoryUser?) -> StoryUser)? = null,
    onCreateStory: () -> Unit,
    onViewStory: (StoryType) -> Unit,
    onProfileClick: (String) -> Unit,
    onRequestLogin: () -> Unit,
    flat: Boolean = false
) {
    val active = remember(stories) { stories.filter { !isStoryExpired(it) } }

    val uniqueUsers = remember(active, currentUser?.id) {
        val seen = mutableSetOf<String>()
        val out = mutableListOf<StoryType>()
        val sorted = active.sortedByDescending { parseStoryServerTime(it.createdAt) }
        val meId = currentUser?.id.orEmpty()
        val mine = sorted.filter { it.userId == meId }
        val others = sorted.filter { it.userId != meId }
        if (mine.isNotEmpty()) out.add(mine[0])
        for (s in others) {
            if (seen.add(s.userId)) {
                out.add(s)
            }
        }
        out
    }

    val baseModifier = if (flat) {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .background(StoryBgCard)
            .border(width = 1.dp, color = StoryBorderSlate)
            .padding(vertical = 12.dp)
    }

    Box(modifier = baseModifier) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = if (flat) 16.dp else 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                CreateStoryCard(
                    currentUser = currentUser,
                    loading = loading,
                    onClick = onCreateStory
                )
            }

            items(uniqueUsers, key = { it.id }) { story ->
                val resolvedAuthor = authorProvider?.invoke(story.userId, story.user) ?: story.user
                val isVerified = isUserVerified?.invoke(story.userId, resolvedAuthor)
                    ?: (resolvedAuthor?.isUserVerified == true)
                StoryReelCard(
                    story = story,
                    author = resolvedAuthor,
                    currentUser = currentUser,
                    isVerified = isVerified,
                    onClick = { onViewStory(story) },
                    onProfileClick = { onProfileClick(story.userId) },
                    onAddMore = onCreateStory
                )
            }
        }
    }
}

@Composable
fun CreateStoryCard(
    currentUser: StoryUser?,
    loading: Boolean,
    onClick: () -> Unit
) {
    val avatarUrl = currentUser?.profileImageUrl
        ?: getDefaultStoryAvatar(currentUser?.name ?: "User", currentUser?.id ?: "anon")

    Box(
        modifier = Modifier
            .width(115.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StoryBgCard)
            .border(1.dp, StoryBorderSlate, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f)
                    .background(StoryBgDeep)
            ) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Create Story Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    alpha = 0.8f
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(StoryBgCard),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Story",
                    color = StoryTextWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
                .size(38.dp)
                .clip(CircleShape)
                .border(3.dp, StoryBgCard, CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(StoryLightBlueHover, StoryPrimaryBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Story",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StoryReelCard(
    story: StoryType,
    author: StoryUser? = null,
    currentUser: StoryUser?,
    isVerified: Boolean = false,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddMore: () -> Unit
) {
    val resolvedAuthor = author ?: story.user ?: StoryUser(
        id = story.userId,
        name = "User",
        profileImageUrl = getDefaultStoryAvatar("User", story.userId)
    )
    val isMine = currentUser != null && currentUser.id == story.userId
    val isText = story.type == "text"
    val thumbUrl = story.mediaThumb ?: story.mediaUrl

    Box(
        modifier = Modifier
            .width(115.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StoryBgSurface)
            .border(1.dp, StoryBorderSlate, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        if (isText) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getStoryBrush(story.backgroundStyle))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent.orEmpty(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        } else if (!thumbUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbUrl,
                contentDescription = "Story",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF0284C7)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
                .size(34.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.5.dp, StoryPrimaryBlue, CircleShape)
                    .clickable(onClick = onProfileClick)
            ) {
                AsyncImage(
                    model = resolvedAuthor.profileImageUrl ?: getDefaultStoryAvatar(resolvedAuthor.name, resolvedAuthor.id),
                    contentDescription = resolvedAuthor.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (isVerified) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                ) {
                    VerifiedBadge(size = 13.dp)
                }
            }
        }

        if (isMine) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1877F2), Color(0xFF7B4DFF), Color(0xFFA033FF))
                        )
                    )
                    .border(1.5.dp, StoryBgSurface, CircleShape)
                    .clickable(onClick = onAddMore),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add another story",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .clickable(onClick = onProfileClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isMine) "Your Story" else resolvedAuthor.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                VerifiedBadge(size = 11.dp)
            }
        }
    }
}

// ============================================================
// STORY GRID CARD (9/16 Card for Main Feed Grid)
// ============================================================
@Composable
fun StoryGridCard(
    story: StoryType,
    author: StoryUser? = null,
    isVerified: Boolean = false,
    onClick: () -> Unit,
    onProfileClick: (() -> Unit)? = null
) {
    val resolvedAuthor = author ?: story.user ?: StoryUser(
        id = story.userId,
        name = "User",
        profileImageUrl = getDefaultStoryAvatar("User", story.userId)
    )
    val isText = story.type == "text"
    val thumbUrl = story.mediaThumb ?: story.mediaUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        if (isText) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getStoryBrush(story.backgroundStyle))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent.orEmpty(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        } else if (!thumbUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbUrl,
                contentDescription = "Story Media",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StoryBgCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = if (onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier
                ) {
                    Text(
                        text = resolvedAuthor.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(3.dp))
                        VerifiedBadge(size = 11.dp)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = formatStoryCount(story.viewsCount ?: 0),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ============================================================
// STORY VIEWER MODAL
// ============================================================
@Composable
fun StoryViewer(
    story: StoryType,
    user: StoryUser,
    currentUser: StoryUser?,
    isVerified: Boolean = user.isUserVerified,
    allStories: List<StoryType>,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onDeleteStory: (String) -> Unit,
    onViewRecorded: (String) -> Unit,
    onProfileClick: ((StoryUser) -> Unit)? = null
) {
    val context = LocalContext.current
    var progress by remember(story.id) { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var originalLoaded by remember(story.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val isAuthor = currentUser != null && currentUser.id == user.id

    val userStories = remember(allStories, story.userId) {
        val src = if (allStories.isNotEmpty()) allStories else listOf(story)
        src.filter { it.userId == story.userId }
            .sortedByDescending { parseStoryServerTime(it.createdAt) }
    }

    val currentIndex = userStories.indexOfFirst { it.id == story.id }.coerceAtLeast(0)

    val isText = story.type == "text"
    val originalUrl = story.mediaUrl.orEmpty()
    val thumbUrl = story.mediaThumb ?: originalUrl
    val displayUrl = if (originalLoaded && originalUrl.isNotBlank()) originalUrl else thumbUrl

    LaunchedEffect(story.id) { onViewRecorded(story.id) }

    LaunchedEffect(story.id, isPaused) {
        if (!isPaused) {
            val stepMs = 50L
            val totalSteps = 5000L / stepMs
            while (progress < 1f && !isPaused) {
                delay(stepMs)
                progress += (1f / totalSteps)
            }
            if (progress >= 1f) onNext()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StoryBgDeep)
                .pointerInput(story.id) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.35f) onPrev()
                            else if (offset.x > width * 0.65f) onNext()
                            else isPaused = !isPaused
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .background(StoryBgSurface)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isText) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getStoryBrush(story.backgroundStyle))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LinkifyText(
                                text = story.textContent.orEmpty(),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 32.sp
                                )
                            )
                        }
                    } else if (displayUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(displayUrl)
                                .crossfade(true)
                                .listener(
                                    onSuccess = { _, _ -> originalLoaded = true },
                                    onError = { _, _ -> originalLoaded = true }
                                )
                                .build(),
                            contentDescription = "Story Media",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Media unavailable", color = TextMuted, fontSize = 14.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    userStories.forEachIndexed { idx, _ ->
                        val segmentProgress = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> progress
                            else -> 0f
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(segmentProgress)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 12.dp, end = 12.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(StoryBgSurface.copy(alpha = 0.85f), Color.Transparent)
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = if (onProfileClick != null) Modifier.clickable { onProfileClick(user) } else Modifier
                    ) {
                        AsyncImage(
                            model = user.profileImageUrl ?: getDefaultStoryAvatar(user.name, user.id),
                            contentDescription = user.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, StoryPrimaryBlue, CircleShape)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = user.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isVerified) VerifiedBadge(size = 16.dp)
                            }
                            Text(
                                text = formatStoryTime(story.createdAt),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this story on JobsReport: https://jobsreport.online/story/${story.slug}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share story"))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Share, "Share Story", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        if (isAuthor) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StoryDangerRed.copy(alpha = 0.85f))
                            ) {
                                Icon(Icons.Default.Delete, "Delete Story", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StoryBgCard.copy(alpha = 0.8f))
                                .border(1.dp, StoryBorderSlate, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                IconButton(
                    onClick = onPrev,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.ChevronLeft, "Previous", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.ChevronRight, "Next", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, StoryBgSurface.copy(alpha = 0.95f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Visibility, "Views", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                        Text(
                            text = "${formatStoryCount(story.viewsCount ?: 0)} views",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out this story on JobsReport: https://jobsreport.online/story/${story.slug}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share"))
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Share", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showDeleteConfirm) {
                Dialog(onDismissRequest = { showDeleteConfirm = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(StoryBgCard)
                            .border(1.dp, StoryBorderSlate, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, null, tint = StoryDangerRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete Story", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                IconButton(
                                    onClick = { showDeleteConfirm = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Delete this story?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "This story will be permanently deleted. This action cannot be undone.",
                                color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable { showDeleteConfirm = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(StoryDangerRed)
                                        .clickable {
                                            isDeleting = true
                                            onDeleteStory(story.id)
                                            showDeleteConfirm = false
                                            onClose()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDeleting) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// CREATE STORY MODAL — with deadline validation + minDate = tomorrow
// ============================================================
@Composable
fun CreateStoryModal(
    viewModel: AppViewModel,
    currentUser: StoryUser,
    onClose: () -> Unit,
    onCreate: suspend (CreateStoryPayload) -> Result<Unit>,
    onUpload: suspend (ByteArray, String, String, ByteArray, String, String) -> Result<com.example.data.UploadResponse>,
    onRequestLogin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(viewModel.storyDraftMode.ifBlank { "text" }) }
    var textContent by remember { mutableStateOf(viewModel.storyDraftText) }
    var backgroundStyle by remember { mutableStateOf(viewModel.storyDraftBg.ifBlank { STORY_BACKGROUNDS[0] }) }

    var selectedImageUri by remember {
        mutableStateOf(viewModel.storyDraftImageUri)
    }
    var compressedResult by remember { mutableStateOf<CompressedMediaResult?>(null) }
    var uploadedUrl by remember { mutableStateOf(viewModel.storyDraftUploadedUrl) }
    var uploadedThumb by remember { mutableStateOf(viewModel.storyDraftUploadedThumb) }

    var isUploading by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val defaultDeadline = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 7)
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
    var deadlineDate by remember {
        mutableStateOf(viewModel.storyDraftDeadline.takeIf { it.isNotBlank() } ?: defaultDeadline)
    }

    // Helper to keep draft persistent in AppViewModel so logins or app backgrounding never wipe user's work
    fun syncDraftToViewModel(
        currText: String = textContent,
        currBg: String = backgroundStyle,
        currUri: Uri? = selectedImageUri,
        currUrl: String? = uploadedUrl,
        currThumb: String? = uploadedThumb,
        currMode: String = mode,
        currDeadline: String = deadlineDate
    ) {
        viewModel.saveStoryDraft(
            text = currText,
            bg = currBg,
            imageUri = currUri,
            uploadedUrl = currUrl,
            uploadedThumb = currThumb,
            mode = currMode,
            deadline = currDeadline
        )
    }

    // Auto-clear auth-related error banners once the user finishes signing in
    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn && (
            errorMessage?.contains("log in", ignoreCase = true) == true ||
            errorMessage?.contains("sign in", ignoreCase = true) == true ||
            errorMessage?.contains("session", ignoreCase = true) == true ||
            errorMessage?.contains("expired", ignoreCase = true) == true
        )) {
            errorMessage = null
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            mode = "image"
            errorMessage = null
            isUploading = true
            syncDraftToViewModel(currUri = uri, currMode = "image")

            coroutineScope.launch {
                val compression = ImageCompressor.processImageUri(context, uri)
                if (compression.isSuccess) {
                    val result = compression.getOrThrow()
                    compressedResult = result

                    val uploadResult = onUpload(
                        result.mainBytes,
                        result.mainFileName,
                        result.mainMime,
                        result.thumbBytes,
                        result.thumbFileName,
                        result.thumbMime
                    )

                    if (uploadResult.isSuccess) {
                        val resp = uploadResult.getOrThrow()
                        uploadedUrl = resp.url ?: resp.dataUrl
                        uploadedThumb = resp.thumbnailUrl ?: resp.url ?: resp.dataUrl
                        syncDraftToViewModel(currUrl = uploadedUrl, currThumb = uploadedThumb)
                    } else {
                        errorMessage = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                    }
                } else {
                    errorMessage = compression.exceptionOrNull()?.message ?: "Image processing failed"
                }
                isUploading = false
            }
        }
    }

    val canShare = when (mode) {
        "text" -> textContent.trim().isNotEmpty()
        "image" -> !uploadedUrl.isNullOrBlank() && !isUploading
        else -> false
    }

    fun handleShare() {
        if (!canShare || isSharing) return

        // Verify active authentication before hitting the API
        if (!viewModel.isLoggedIn) {
            errorMessage = "Please log in to share your story. Your draft is safely saved."
            syncDraftToViewModel()
            onRequestLogin?.invoke()
            return
        }

        isSharing = true
        errorMessage = null
        syncDraftToViewModel()

        // ── Client-side deadline check (mirrors server rule) ──
        val isoExpires = "${deadlineDate}T23:59:59.000Z"
        val expiresMs = try {
            java.time.Instant.parse(isoExpires).toEpochMilli()
        } catch (e: Exception) {
            errorMessage = "Invalid deadline"
            isSharing = false
            return
        }
        if (expiresMs <= System.currentTimeMillis()) {
            errorMessage = "Deadline must be in the future"
            isSharing = false
            return
        }

        coroutineScope.launch {
            try {
                val payload = CreateStoryPayload(
                    type = mode,
                    textContent = if (mode == "text") textContent.trim() else null,
                    backgroundStyle = if (mode == "text") backgroundStyle else null,
                    mediaUrl = if (mode == "image") uploadedUrl else null,
                    mediaThumb = if (mode == "image") uploadedThumb else null,
                    expiresAt = isoExpires
                )
                val res = onCreate(payload)
                if (res.isSuccess) {
                    Toast.makeText(context, "Story shared successfully!", Toast.LENGTH_SHORT).show()
                    viewModel.clearStoryDraft()
                    onClose()
                } else {
                    val err = res.exceptionOrNull()?.message ?: "Failed to share story"
                    errorMessage = err
                    if (err.contains("log in", ignoreCase = true) || err.contains("session", ignoreCase = true) || err.contains("expired", ignoreCase = true)) {
                        onRequestLogin?.invoke()
                    }
                }
            } catch (e: Exception) {
                val err = e.message ?: "Failed to share story"
                errorMessage = err
                if (err.contains("log in", ignoreCase = true) || err.contains("session", ignoreCase = true) || err.contains("expired", ignoreCase = true)) {
                    onRequestLogin?.invoke()
                }
            } finally {
                isSharing = false
            }
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StoryBgDeep)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StoryBgSurface)
                    .border(width = 1.dp, color = StoryBorderSlate)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(StoryBorderSlate)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Discard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Text("Create Story", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (canShare && !isSharing) StoryPrimaryBlue else Color.Gray.copy(alpha = 0.5f))
                        .clickable(enabled = canShare && !isSharing, onClick = { handleShare() })
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    if (isSharing) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Share", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }

            // Proactive Non-Intrusive Auth Banner if not currently signed in
            if (!viewModel.isLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .border(width = 1.dp, color = LightBlue.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LightBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sign in to share your story. Your draft is safe.",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StoryPrimaryBlue)
                                .clickable {
                                    syncDraftToViewModel()
                                    onRequestLogin?.invoke()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (mode == "text") getStoryBrush(backgroundStyle) else Brush.linearGradient(listOf(StoryBgDeep, StoryBgDeep))),
                contentAlignment = Alignment.Center
            ) {
                if (mode == "text") {
                    TextField(
                        value = textContent,
                        onValueChange = {
                            textContent = it
                            syncDraftToViewModel(currText = it)
                        },
                        placeholder = {
                            Text(
                                text = "Share your job story... (links welcome)",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                    )
                } else {
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Story Preview",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    compressedResult = null
                                    uploadedUrl = null
                                    uploadedThumb = null
                                    syncDraftToViewModel(currUri = null, currUrl = null, currThumb = null)
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopStart)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Delete, "Remove Image", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            if (isUploading) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CircularProgressIndicator(color = StoryPrimaryBlue, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                        Text(
                                            "Compressing & uploading…",
                                            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(StoryBgCard)
                                    .border(1.dp, StoryBorderSlate, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, "Select Image", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Select an Image", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("JPG, PNG, WEBP — auto-generates thumbnail", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Controls Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StoryBgSurface)
                    .border(width = 1.dp, color = StoryBorderSlate)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mode == "text") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(STORY_BACKGROUNDS) { bg ->
                            val isSelected = backgroundStyle == bg
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getStoryBrush(bg))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        backgroundStyle = bg
                                        syncDraftToViewModel(currBg = bg)
                                    }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StoryInputDark)
                        .border(1.dp, StoryBorderSlate, RoundedCornerShape(14.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            val parts = deadlineDate.split("-")
                            if (parts.size == 3) {
                                cal.set(Calendar.YEAR, parts[0].toIntOrNull() ?: cal.get(Calendar.YEAR))
                                cal.set(Calendar.MONTH, (parts[1].toIntOrNull() ?: 1) - 1)
                                cal.set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH))
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    deadlineDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    syncDraftToViewModel(currDeadline = deadlineDate)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
                            }.show()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, "Deadline", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Text("Deadline", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = deadlineDate,
                        color = LightBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(StoryInputDark)
                            .border(1.dp, StoryBorderSlate, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (mode == "text") StoryPrimaryBlue else Color.Transparent)
                                .clickable {
                                    mode = "text"
                                    syncDraftToViewModel(currMode = "text")
                                }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    Icons.Default.TextFields, "Text",
                                    tint = if (mode == "text") Color.White else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Text",
                                    color = if (mode == "text") Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp, fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (mode == "image") StoryPrimaryBlue else Color.Transparent)
                                .clickable {
                                    mode = "image"
                                    syncDraftToViewModel(currMode = "image")
                                }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    Icons.Default.Image, "Image",
                                    tint = if (mode == "image") Color.White else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Image",
                                    color = if (mode == "image") Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp, fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, "Add image file", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StoryDangerRed.copy(alpha = 0.15f))
                            .border(1.dp, StoryDangerRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ErrorOutline, "Error", tint = StoryDangerRed, modifier = Modifier.size(16.dp))
                            Text(
                                errorMessage.orEmpty(),
                                color = StoryDangerRed, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (onRequestLogin != null && (
                                errorMessage?.contains("log in", ignoreCase = true) == true ||
                                errorMessage?.contains("sign in", ignoreCase = true) == true ||
                                errorMessage?.contains("session", ignoreCase = true) == true ||
                                errorMessage?.contains("expired", ignoreCase = true) == true ||
                                !viewModel.isLoggedIn
                            )) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Log In",
                                    color = LightBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        syncDraftToViewModel()
                                        onRequestLogin()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// USER PROFILE / TOKEN MODAL
// ============================================================
@Composable
fun UserProfileModal(
    currentUser: StoryUser?,
    userToken: String?,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var token by remember { mutableStateOf(userToken ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(StoryBgCard)
                .border(1.dp, StoryBorderSlate, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Story Author Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Manage your JobsReport story identity", color = TextSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, StoryPrimaryBlue, CircleShape)
                ) {
                    AsyncImage(
                        model = currentUser?.profileImageUrl ?: getDefaultStoryAvatar(name.ifBlank { "User" }, currentUser?.id ?: "seed"),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Display Name") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StoryPrimaryBlue,
                        unfocusedBorderColor = StoryBorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Username (@handle)") }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StoryPrimaryBlue,
                        unfocusedBorderColor = StoryBorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = token, onValueChange = { token = it },
                    label = { Text("Website Token (jobsreport_token)") },
                    placeholder = { Text("Optional Bearer JWT token from web", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StoryPrimaryBlue,
                        unfocusedBorderColor = StoryBorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(StoryBorderSlate).clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(StoryPrimaryBlue)
                            .clickable {
                                val cleanToken = token.trim().removeSurrounding("\"").removePrefix("Bearer ").removePrefix("bearer ").trim().ifBlank { null }
                                onSave(name.trim(), username.trim().ifBlank { null }, email.trim().ifBlank { null }, cleanToken)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ============================================================
// LINKIFY TEXT
// ============================================================
@Composable
fun LinkifyText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlPattern = Pattern.compile("((?:https?://|www\\.)[^\\s<>\"']+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})")
    val matcher = urlPattern.matcher(text)

    val annotatedString = buildAnnotatedString {
        var lastIdx = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val match = matcher.group()

            if (start > lastIdx) append(text.substring(lastIdx, start))

            val isEmail = match.contains("@") && !match.startsWith("http")
            val targetUrl = when {
                isEmail -> "mailto:$match"
                match.startsWith("http") -> match
                else -> "https://$match"
            }

            pushStringAnnotation(tag = "URL", annotation = targetUrl)
            pushStyle(
                SpanStyle(
                    color = Color(0xFF93C5FD),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            )
            append(match)
            pop()
            pop()

            lastIdx = end
        }
        if (lastIdx < text.length) append(text.substring(lastIdx))
    }

    ClickableText(
        text = annotatedString,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)))
                    } catch (_: Exception) {}
                }
        }
    )
}
