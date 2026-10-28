package com.example.state

import android.app.Application
import android.content.Context
import android.net.Uri
import com.example.util.ImageCompressor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.JobsReportApi
import com.example.data.Company
import com.example.data.Country
import com.example.data.JobCategory
import com.example.data.JobOpportunity
import com.example.data.LocationInfo
import com.example.data.MarketReport
import com.example.data.NotificationItem
import com.example.data.defaultCountriesList
import com.example.data.sampleCompanies
import com.example.data.sampleJobCategories
import com.example.data.sampleJobOpportunities
import com.example.data.sampleLocations
import com.example.data.sampleMarketReports
import com.example.data.sampleNotifications
import com.example.data.sampleSpotlightCompanies
import com.example.data.CreateStoryPayload
import com.example.data.StoriesResponse
import com.example.data.StoryType
import com.example.data.StoryUser
import com.example.data.UploadResponse
import com.example.data.getDefaultStoryAvatar
import com.example.data.isStoryExpired
import com.example.data.isUserVerified
import com.example.data.AppNotification
import com.example.data.NotificationActor
import com.example.data.getDefaultNotificationAvatar
import com.example.data.formatNotificationRelativeTime
import com.example.data.formatNotificationDisplay
import com.example.data.parseNotificationTimeMs
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

enum class NavRoute(val title: String, val routePath: String) {
    INTELLIGENCE_FEED("Intelligence Feed", "/"),
    ALL_JOBS("All Jobs List", "/jobs"),
    SAVED_JOBS("Saved Jobs", "/saved-jobs"),
    COMPANIES("Companies", "/companies"),
    REGIONS("Regions", "/regions"),
    JOB_REPORTS("All Job Reports", "/reports"),
    ADMIN_STUDIO("Admin Studio", "/admin"),
    JOB_DETAIL("Job Details", "/job-detail"),
    CATEGORY_JOBS("Category Jobs", "/category"),
    REPORT_DETAIL("Report Detail", "/report-detail"),
    REGION_DETAIL("Region Detail", "/region-detail"),
    ABOUT_US("About Us", "/about-us"),
    DISCLAIMER("Disclaimer", "/disclaimer"),
    PRIVACY_POLICY("Privacy Policy", "/privacy-policy"),
    STORIES("Job Stories", "/stories"),
    USER_AUTH("Account / Sign In", "/auth"),
    USER_PROFILE("User Profile", "/profile"),
    USER_JOB_INPUT("Post a Job", "/post-job"),
    NOTIFICATIONS("Notifications", "/notifications")
}

// ═══ FIX B — shared sanitizer for text fields coming from JSON or prefs.
// Treats null, blank, and the literal string "null" as "no value".
internal fun String?.cleanDbText(): String? {
    if (this == null) return null
    val t = this.trim()
    if (t.isEmpty()) return null
    if (t.equals("null", ignoreCase = true)) return null
    return t
}

// ═══ FIX B — safe read from a JSONObject that could contain JSON null.
// org.json's optString() returns the string "null" when the value is JSON null,
// which is the source of the "null" bug on the profile screen.
internal fun JSONObject.safeStr(key: String): String? {
    if (!has(key) || isNull(key)) return null
    val raw = optString(key, null) ?: return null
    return raw.cleanDbText()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val api = JobsReportApi.create { cleanUserToken }
    private val prefs = application.getSharedPreferences("jobsreport_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val savedJobsType = Types.newParameterizedType(List::class.java, JobOpportunity::class.java)
    private val savedJobsAdapter = moshi.adapter<List<JobOpportunity>>(savedJobsType)

    private val storyListType = Types.newParameterizedType(List::class.java, StoryType::class.java)
    private val storyListAdapter = moshi.adapter<List<StoryType>>(storyListType)
    private val storiesResponseAdapter = moshi.adapter(StoriesResponse::class.java)
    private val uploadResponseAdapter = moshi.adapter(UploadResponse::class.java)

    var currentRoute by mutableStateOf(NavRoute.INTELLIGENCE_FEED)
    var selectedJob by mutableStateOf<JobOpportunity?>(null)

    // Story & Community Intel State (exact parity with Story.tsx)
    var stories by mutableStateOf<List<StoryType>>(emptyList())
        private set
    var isStoriesLoading by mutableStateOf(false)
        private set
    var viewingStory by mutableStateOf<StoryType?>(null)
    var isCreateStoryOpen by mutableStateOf(false)
    var isUserProfileModalOpen by mutableStateOf(false)
    var isUserAuthModalOpen by mutableStateOf(false)

    // Story Draft preservation across Auth flows
    var storyDraftText by mutableStateOf("")
    var storyDraftBg by mutableStateOf(com.example.data.STORY_BACKGROUNDS[0])
    var storyDraftImageUri by mutableStateOf<android.net.Uri?>(null)
    var storyDraftUploadedUrl by mutableStateOf<String?>(null)
    var storyDraftUploadedThumb by mutableStateOf<String?>(null)
    var storyDraftMode by mutableStateOf("text")
    var storyDraftDeadline by mutableStateOf("")

    fun saveStoryDraft(
        text: String,
        bg: String,
        imageUri: android.net.Uri?,
        uploadedUrl: String?,
        uploadedThumb: String?,
        mode: String,
        deadline: String
    ) {
        storyDraftText = text
        storyDraftBg = bg
        storyDraftImageUri = imageUri
        storyDraftUploadedUrl = uploadedUrl
        storyDraftUploadedThumb = uploadedThumb
        storyDraftMode = mode
        storyDraftDeadline = deadline
    }

    fun clearStoryDraft() {
        storyDraftText = ""
        storyDraftBg = com.example.data.STORY_BACKGROUNDS[0]
        storyDraftImageUri = null
        storyDraftUploadedUrl = null
        storyDraftUploadedThumb = null
        storyDraftMode = "text"
        storyDraftDeadline = ""
    }

    // User Session State (exact sync with jobsreport_token)
    var userToken by mutableStateOf<String?>(null)
        private set
    var currentUser by mutableStateOf<StoryUser?>(null)
    var viewingProfileUser by mutableStateOf<StoryUser?>(null)
    val followedUserIds = mutableStateListOf<String>()

    // Real server-verified user status & profile cache
    val verifiedUsersMap = mutableStateMapOf<String, Boolean>()
    val userProfilesCache = mutableStateMapOf<String, StoryUser>()

    private val pendingProfileFetches = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    fun isUserVerified(userId: String, fallbackUser: StoryUser? = null): Boolean {
        if (userId.isBlank()) return false
        verifiedUsersMap[userId]?.let { return it }
        val cached = userProfilesCache[userId]
        if (cached != null) {
            val v = cached.isUserVerified
            verifiedUsersMap[userId] = v
            return v
        }
        if (fallbackUser != null && fallbackUser.isVerified != null) {
            val v = fallbackUser.isUserVerified
            verifiedUsersMap[userId] = v
            return v
        }
        ensureUserProfileLoaded(userId)
        return false
    }

    fun getResolvedUser(userId: String, fallbackUser: StoryUser? = null): StoryUser {
        if (userId.isBlank()) {
            return fallbackUser ?: StoryUser(
                id = "",
                name = "User",
                profileImageUrl = getDefaultStoryAvatar("User", "anon")
            )
        }
        val cached = userProfilesCache[userId]
        if (cached != null) return cached
        ensureUserProfileLoaded(userId)
        return fallbackUser ?: StoryUser(
            id = userId,
            name = "User",
            profileImageUrl = getDefaultStoryAvatar("User", userId)
        )
    }

    fun ensureUserProfileLoaded(userId: String) {
        if (userId.isBlank() || userProfilesCache.containsKey(userId) || pendingProfileFetches.contains(userId)) return
        pendingProfileFetches.add(userId)
        viewModelScope.launch {
            try {
                val user = fetchUserProfile(userId)
                if (user != null) {
                    withContext(Dispatchers.Main) {
                        userProfilesCache[userId] = user
                        verifiedUsersMap[userId] = user.isUserVerified
                    }
                }
            } finally {
                pendingProfileFetches.remove(userId)
            }
        }
    }

    fun getUserNameById(userId: String?): String? {
        if (userId.isNullOrBlank()) return null
        if (currentUser?.id == userId) return currentUser?.name
        val cached = userProfilesCache[userId]?.name
        if (cached != null) return cached
        ensureUserProfileLoaded(userId)
        return null
    }

    fun isUserFollowed(userId: String): Boolean = followedUserIds.contains(userId)

    fun toggleFollowUser(userId: String): Boolean {
        return if (followedUserIds.contains(userId)) {
            followedUserIds.remove(userId)
            false
        } else {
            followedUserIds.add(userId)
            true
        }
    }

    fun openUserProfile(user: StoryUser? = null) {
        viewingProfileUser = user
        isMobileMenuOpen = false
        currentRoute = NavRoute.USER_PROFILE
    }

    fun openUserProfileById(userId: String) {
        val foundUser = stories.firstOrNull { it.userId == userId || it.user?.id == userId }?.user
            ?: if (currentUser?.id == userId) currentUser
            else StoryUser(
                id = userId,
                name = "User",
                username = "user_${userId.takeLast(4)}",
                profileImageUrl = getDefaultStoryAvatar("User", userId)
            )
        openUserProfile(foundUser)
    }

    fun openUserProfileByName(userId: String, name: String?, avatarUrl: String? = null) {
        val resolvedName = name?.trim()?.takeIf { it.isNotBlank() } ?: "User"
        val resolvedId = userId.trim().takeIf { it.isNotBlank() } ?: "user_${resolvedName.lowercase().replace(" ", "_")}"
        val foundUser = stories.firstOrNull {
            it.userId == resolvedId || it.user?.id == resolvedId || it.user?.name?.equals(resolvedName, ignoreCase = true) == true
        }?.user
            ?: if (currentUser?.id == resolvedId || (currentUser?.name?.equals(resolvedName, ignoreCase = true) == true)) currentUser
            else StoryUser(
                id = resolvedId,
                name = resolvedName,
                username = resolvedName.lowercase().replace(" ", "_"),
                profileImageUrl = avatarUrl?.ifBlank { null } ?: getDefaultStoryAvatar(resolvedName, resolvedId)
            )
        openUserProfile(foundUser)
    }

    var storyRotationSeed by mutableStateOf(System.currentTimeMillis())
        private set

    fun rotateStories() {
        storyRotationSeed = System.currentTimeMillis()
    }

    var editingJob by mutableStateOf<JobOpportunity?>(null)
    var jobInputReturnRoute by mutableStateOf<NavRoute?>(null)

    fun openPostJob(returnRoute: NavRoute? = currentRoute) {
        if (userToken == null) {
            openAuth()
            return
        }
        editingJob = null
        jobInputReturnRoute = returnRoute
        isMobileMenuOpen = false
        currentRoute = NavRoute.USER_JOB_INPUT
    }

    fun openEditJob(job: JobOpportunity, returnRoute: NavRoute? = currentRoute) {
        if (userToken == null) {
            openAuth()
            return
        }
        editingJob = job
        jobInputReturnRoute = returnRoute
        isMobileMenuOpen = false
        currentRoute = NavRoute.USER_JOB_INPUT
    }

    // Saved / Bookmarked Jobs State (Offline memory / lightweight local storage)
    val savedJobs = mutableStateListOf<JobOpportunity>()

    val savedJobsCount: Int
        get() = savedJobs.size

    fun isJobSaved(jobId: String): Boolean {
        return savedJobs.any { it.id == jobId }
    }

    fun toggleSaveJob(job: JobOpportunity): Boolean {
        val existingIndex = savedJobs.indexOfFirst { it.id == job.id }
        val isNowSaved = if (existingIndex >= 0) {
            savedJobs.removeAt(existingIndex)
            false
        } else {
            savedJobs.add(0, job)
            true
        }
        persistSavedJobs()
        return isNowSaved
    }

    fun removeSavedJob(jobId: String) {
        savedJobs.removeAll { it.id == jobId }
        persistSavedJobs()
    }

    fun clearAllSavedJobs() {
        savedJobs.clear()
        persistSavedJobs()
    }

    private fun loadSavedJobs() {
        val json = prefs.getString("saved_jobs_json", null)
        if (!json.isNullOrBlank()) {
            try {
                val list = savedJobsAdapter.fromJson(json)
                if (!list.isNullOrEmpty()) {
                    savedJobs.clear()
                    savedJobs.addAll(list)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun persistSavedJobs() {
        try {
            val json = savedJobsAdapter.toJson(savedJobs.toList())
            prefs.edit().putString("saved_jobs_json", json).apply()
        } catch (_: Exception) {
        }
    }

    fun selectJob(job: JobOpportunity) {
        selectedJob = job
        currentRoute = NavRoute.JOB_DETAIL
    }

    // Country state
    var selectedCountry by mutableStateOf(prefs.getString("selected_country", "Worldwide") ?: "Worldwide")
    var countrySearchQuery by mutableStateOf("")
    val countries = defaultCountriesList

    val currentFlag: String
        get() {
            if (selectedCountry.lowercase() == "worldwide") return "🌍"
            return countries.find { it.name.equals(selectedCountry, ignoreCase = true) }?.flag ?: "🌍"
        }

    // Home / Market Feed Data
    var isDataLoading by mutableStateOf(false)
        private set

    var categories by mutableStateOf<List<JobCategory>>(sampleJobCategories)
        private set

    var activeJobs by mutableStateOf<List<JobOpportunity>>(sampleJobOpportunities)
        private set

    var reports by mutableStateOf<List<MarketReport>>(sampleMarketReports)
        private set

    var spotlightCompanies by mutableStateOf<List<String>>(sampleSpotlightCompanies)
        private set

    var companies by mutableStateOf<List<Company>>(sampleCompanies)
        private set

    var selectedCompany by mutableStateOf<Company?>(null)
    var selectedCompanyJobs by mutableStateOf<List<JobOpportunity>>(emptyList())
    var isCompanyJobsLoading by mutableStateOf(false)

    // Category Jobs State
    var selectedCategoryName by mutableStateOf<String?>(null)
    var categoryJobs by mutableStateOf<List<JobOpportunity>>(emptyList())
    var isCategoryJobsLoading by mutableStateOf(false)

    // Report Detail State
    var selectedReportDetail by mutableStateOf<MarketReport?>(null)
    var isReportDetailLoading by mutableStateOf(false)

    // Region / Location State
    var locations by mutableStateOf<List<LocationInfo>>(sampleLocations)
    var isLocationsLoading by mutableStateOf(false)
    var selectedRegionName by mutableStateOf<String?>(null)
    var selectedRegionLocationInfo by mutableStateOf<LocationInfo?>(null)

    fun selectRegion(regionName: String, locationInfo: LocationInfo? = null) {
        selectedRegionName = regionName
        selectedRegionLocationInfo = locationInfo ?: locations.find {
            it.name.equals(regionName, ignoreCase = true) ||
            it.region?.equals(regionName, ignoreCase = true) == true
        }
        currentRoute = NavRoute.REGION_DETAIL
    }

    fun fetchLocations() {
        viewModelScope.launch {
            isLocationsLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val res = runCatching { api.getLocations() }.getOrNull()
                    if (!res.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            locations = res
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                isLocationsLoading = false
            }
        }
    }

    fun selectReport(report: MarketReport) {
        selectedReportDetail = report
        currentRoute = NavRoute.REPORT_DETAIL
        fetchReportDetail(report)
    }

    private fun fetchReportDetail(report: MarketReport) {
        val targetSlug = report.slug ?: report.id
        viewModelScope.launch {
            isReportDetailLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val detail = runCatching { api.getReportDetail(targetSlug) }.getOrNull()
                    if (detail != null) {
                        withContext(Dispatchers.Main) {
                            selectedReportDetail = detail
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                isReportDetailLoading = false
            }
        }
    }

    fun selectCategory(categoryName: String) {
        selectedCategoryName = categoryName
        currentRoute = NavRoute.CATEGORY_JOBS
        fetchCategoryJobs(categoryName)
    }

    private fun fetchCategoryJobs(categoryName: String) {
        viewModelScope.launch {
            isCategoryJobsLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val res = runCatching { api.getCategoryJobs(categoryName) }.getOrNull()
                    val fetched = res?.jobs
                    withContext(Dispatchers.Main) {
                        if (fetched != null && fetched.isNotEmpty()) {
                            categoryJobs = fetched
                        } else {
                            categoryJobs = activeJobs.filter { job ->
                                job.jobCategory?.contains(categoryName, ignoreCase = true) == true ||
                                job.role?.contains(categoryName, ignoreCase = true) == true ||
                                job.title.contains(categoryName, ignoreCase = true)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                categoryJobs = activeJobs.filter { job ->
                    job.jobCategory?.contains(categoryName, ignoreCase = true) == true ||
                    job.role?.contains(categoryName, ignoreCase = true) == true ||
                    job.title.contains(categoryName, ignoreCase = true)
                }
            } finally {
                isCategoryJobsLoading = false
            }
        }
    }

    fun selectCompany(company: Company?) {
        selectedCompany = company
        if (company != null) {
            fetchCompanyJobs(company)
        } else {
            selectedCompanyJobs = emptyList()
        }
    }

    private fun fetchCompanyJobs(company: Company) {
        viewModelScope.launch {
            isCompanyJobsLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val companyJobsResult = runCatching { api.getCompanyJobs(company.id) }.getOrNull()
                    withContext(Dispatchers.Main) {
                        if (companyJobsResult != null && companyJobsResult.isNotEmpty()) {
                            selectedCompanyJobs = companyJobsResult
                        } else {
                            selectedCompanyJobs = activeJobs.filter {
                                it.company.contains(company.name, ignoreCase = true) || company.name.contains(it.company, ignoreCase = true)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                selectedCompanyJobs = activeJobs.filter {
                    it.company.contains(company.name, ignoreCase = true) || company.name.contains(it.company, ignoreCase = true)
                }
            } finally {
                isCompanyJobsLoading = false
            }
        }
    }

    // Admin Auth State
    var isAdmin by mutableStateOf(false)
        private set

    var passcode by mutableStateOf("")
    var loginError by mutableStateOf("")

    // Modals / Overlays
    var isMobileMenuOpen by mutableStateOf(false)
    var isLoginModalOpen by mutableStateOf(false)
    var isCountryDropdownOpen by mutableStateOf(false)
    var isNotificationBellOpen by mutableStateOf(false)
    var isSearchModalOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    // Notifications State (Live Backend Sync)
    var notificationsList by mutableStateOf<List<AppNotification>>(emptyList())
    var unreadNotificationsCount by mutableStateOf(0)
    var totalNotificationsCount by mutableStateOf(0)
    var hasMoreNotifications by mutableStateOf(false)
    var isNotificationsLoading by mutableStateOf(false)
    var isNotificationsLoadingMore by mutableStateOf(false)
    var isMarkingAllNotifications by mutableStateOf(false)
    var notificationToastMessage by mutableStateOf<String?>(null)
    var notificationToastIsError by mutableStateOf(false)

    val unreadCount: Int
        get() = unreadNotificationsCount

    val notifications = mutableStateListOf<NotificationItem>().apply {
        addAll(sampleNotifications)
    }

    var uploadError by mutableStateOf<String?>(null)

    private var notificationPollingJob: kotlinx.coroutines.Job? = null

    init {
        loadSavedJobs()
        loadCurrentUser()
        fetchDashboardData()
        loadStories()
    }

    fun startNotificationPolling() {
        notificationPollingJob?.cancel()
        val auth = authHeader
        if (auth.isNullOrBlank()) return
        notificationPollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(45_000L) // Poll every 45s, matching web NotificationBell interval
                if (!isActive) break
                try {
                    val currentAuth = authHeader
                    if (!currentAuth.isNullOrBlank()) {
                        val response = api.getNotifications(auth = currentAuth, limit = 1, offset = 0)
                        val rawJson = response.string()
                        if (rawJson.isNotBlank()) {
                            val json = JSONObject(rawJson)
                            val unread = json.optInt("unread", -1)
                            if (unread >= 0) {
                                withContext(Dispatchers.Main) {
                                    unreadNotificationsCount = unread
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // silent like web
                }
            }
        }
    }

    fun stopNotificationPolling() {
        notificationPollingJob?.cancel()
        notificationPollingJob = null
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            isDataLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val catResult = runCatching { api.getCategories() }.getOrNull()
                    val homeResult = runCatching { api.getHomeData(selectedCountry) }.getOrNull()
                    val marketResult = runCatching { api.getMarketData(100) }.getOrNull()
                    val compResult = runCatching { api.getCompanies() }.getOrNull()
                    val reportsResult: List<MarketReport>? = runCatching { api.getReports() }.getOrNull()

                    withContext(Dispatchers.Main) {
                        if (!catResult.isNullOrEmpty()) {
                            categories = catResult
                        }
                        if (!reportsResult.isNullOrEmpty()) {
                            reports = reportsResult
                        } else if (homeResult != null && !homeResult.reports.isNullOrEmpty()) {
                            reports = homeResult.reports
                        }
                        if (homeResult != null && !homeResult.spotlightCompanies.isNullOrEmpty()) {
                            spotlightCompanies = homeResult.spotlightCompanies
                        }
                        if (marketResult != null) {
                            val jobs = marketResult.activeJobs ?: marketResult.jobs
                            if (!jobs.isNullOrEmpty()) {
                                activeJobs = jobs
                            }
                        }
                        if (!compResult.isNullOrEmpty()) {
                            companies = compResult
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                isDataLoading = false
            }
        }
    }

    fun selectCountry(countryName: String) {
        selectedCountry = countryName
        isCountryDropdownOpen = false
        prefs.edit().putString("selected_country", countryName).apply()
        fetchDashboardData()
    }

    fun loadNotifications(offset: Int = 0) {
        val auth = authHeader
        if (auth.isNullOrBlank()) {
            isNotificationsLoading = false
            isNotificationsLoadingMore = false
            return
        }

        if (offset == 0) {
            isNotificationsLoading = true
        } else {
            isNotificationsLoadingMore = true
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.getNotifications(
                    auth = auth,
                    limit = 30,
                    offset = offset
                )
                val rawJson = response.string()
                val json = if (rawJson.isNotBlank()) JSONObject(rawJson) else JSONObject()
                val list = mutableListOf<AppNotification>()
                val arr = json.optJSONArray("notifications") ?: JSONArray()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue

                    val actorObj = obj.optJSONObject("actor")
                        ?: obj.optJSONObject("user")
                        ?: obj.optJSONObject("sender")
                        ?: obj.optJSONObject("author")

                    val actorId = actorObj?.optString("id")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("_id")?.takeIf { it.isNotBlank() }
                        ?: obj.optString("actorId").takeIf { it.isNotBlank() }
                        ?: obj.optString("actor_id").takeIf { it.isNotBlank() }
                        ?: obj.optString("userId").takeIf { it.isNotBlank() }
                        ?: obj.optString("user_id").takeIf { it.isNotBlank() }

                    val actorName = actorObj?.optString("name")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("fullName")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("full_name")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("displayName")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("display_name")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("username")?.takeIf { it.isNotBlank() }
                        ?: obj.optString("actorName").takeIf { it.isNotBlank() }
                        ?: obj.optString("actor_name").takeIf { it.isNotBlank() }
                        ?: obj.optString("senderName").takeIf { it.isNotBlank() }
                        ?: obj.optString("sender_name").takeIf { it.isNotBlank() }
                        ?: obj.optString("userName").takeIf { it.isNotBlank() }
                        ?: obj.optString("user_name").takeIf { it.isNotBlank() }
                        ?: obj.optString("authorName").takeIf { it.isNotBlank() }
                        ?: obj.optString("author_name").takeIf { it.isNotBlank() }

                    val actorUsername = actorObj?.optString("username")?.takeIf { it.isNotBlank() }
                        ?: obj.optString("actorUsername").takeIf { it.isNotBlank() }
                        ?: obj.optString("username").takeIf { it.isNotBlank() }

                    val actorAvatar = actorObj?.optString("profileImageUrl")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("profile_image_url")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("avatar")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("avatar_url")?.takeIf { it.isNotBlank() }
                        ?: actorObj?.optString("imageUrl")?.takeIf { it.isNotBlank() }
                        ?: obj.optString("actorImage").takeIf { it.isNotBlank() }
                        ?: obj.optString("actor_image").takeIf { it.isNotBlank() }
                        ?: obj.optString("avatar").takeIf { it.isNotBlank() }

                    val actor = if (actorName != null || actorId != null || actorAvatar != null) {
                        NotificationActor(
                            id = actorId,
                            name = actorName,
                            username = actorUsername,
                            profileImageUrl = actorAvatar
                        )
                    } else null

                    if (actor != null && !actor.id.isNullOrBlank() && !actor.name.isNullOrBlank()) {
                        userProfilesCache[actor.id] = StoryUser(
                            id = actor.id,
                            name = actor.name,
                            username = actor.username,
                            profileImageUrl = actor.profileImageUrl
                        )
                    }

                    val rawTitle = obj.optString("title").takeIf { it.isNotBlank() }
                        ?: obj.optString("message").takeIf { it.isNotBlank() }
                        ?: obj.optString("text").takeIf { it.isNotBlank() }
                        ?: obj.optString("subject").takeIf { it.isNotBlank() }
                        ?: ""

                    val rawBody = obj.optString("body").takeIf { it.isNotBlank() }
                        ?: obj.optString("message").takeIf { it.isNotBlank() && it != rawTitle }
                        ?: obj.optString("description").takeIf { it.isNotBlank() }
                        ?: obj.optString("content").takeIf { it.isNotBlank() }

                    val notifLink = obj.optString("link").takeIf { it.isNotBlank() }
                        ?: obj.optString("url").takeIf { it.isNotBlank() }
                        ?: obj.optString("action_url").takeIf { it.isNotBlank() }

                    val notifType = obj.optString("type").takeIf { it.isNotBlank() }
                        ?: obj.optString("notification_type").takeIf { it.isNotBlank() }
                        ?: "job_created"

                    val entityType = obj.optString("entityType").takeIf { it.isNotBlank() }
                        ?: obj.optString("entity_type").takeIf { it.isNotBlank() }
                        ?: obj.optString("targetType").takeIf { it.isNotBlank() }

                    val entityId = obj.optString("entityId").takeIf { it.isNotBlank() }
                        ?: obj.optString("entity_id").takeIf { it.isNotBlank() }
                        ?: obj.optString("targetId").takeIf { it.isNotBlank() }
                        ?: obj.optString("job_id").takeIf { it.isNotBlank() }
                        ?: obj.optString("story_id").takeIf { it.isNotBlank() }

                    val notifImg = obj.optString("imageUrl").takeIf { it.isNotBlank() }
                        ?: obj.optString("image").takeIf { it.isNotBlank() }
                        ?: actorAvatar

                    list.add(
                        AppNotification(
                            id = obj.optString("id", "${System.currentTimeMillis()}_$i"),
                            type = notifType,
                            title = rawTitle,
                            body = rawBody,
                            link = notifLink,
                            imageUrl = notifImg,
                            actorId = actorId,
                            actor = actor,
                            entityType = entityType,
                            entityId = entityId,
                            isRead = obj.optBoolean("isRead", false) || obj.optInt("isRead", 0) == 1,
                            createdAt = obj.optString("createdAt").takeIf { it.isNotBlank() }
                                ?: obj.optString("created_at", "")
                        )
                    )
                }
                val unread = json.optInt("unread", 0)
                val total = json.optInt("total", list.size)
                val hasMore = json.optBoolean("hasMore", false)

                withContext(Dispatchers.Main) {
                    if (offset == 0) {
                        notificationsList = list
                    } else {
                        val existingIds = notificationsList.map { it.id }.toSet()
                        val newItems = list.filter { it.id !in existingIds }
                        notificationsList = notificationsList + newItems
                    }
                    unreadNotificationsCount = unread
                    totalNotificationsCount = total
                    hasMoreNotifications = hasMore
                }
            } catch (e: Exception) {
            } finally {
                withContext(Dispatchers.Main) {
                    isNotificationsLoading = false
                    isNotificationsLoadingMore = false
                }
            }
        }
    }

    fun markAllNotificationsRead() {
        if (isMarkingAllNotifications || unreadNotificationsCount == 0) return
        val auth = authHeader ?: return

        isMarkingAllNotifications = true
        val snapshot = notificationsList
        notificationsList = notificationsList.map { it.copy(isRead = true) }
        unreadNotificationsCount = 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("all", true)
                }.toString().toRequestBody("application/json".toMediaTypeOrNull())

                api.updateNotifications(auth, payload)
                withContext(Dispatchers.Main) {
                    notificationToastMessage = "All marked as read"
                    notificationToastIsError = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    notificationsList = snapshot
                    unreadNotificationsCount = snapshot.count { !it.isRead }
                    notificationToastMessage = "Failed to mark all as read"
                    notificationToastIsError = true
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isMarkingAllNotifications = false
                }
            }
        }
    }

    fun markNotificationRead(id: String, read: Boolean) {
        val prevItem = notificationsList.find { it.id == id }
        notificationsList = notificationsList.map { if (it.id == id) it.copy(isRead = read) else it }
        if (prevItem != null && prevItem.isRead != read) {
            unreadNotificationsCount = if (read) maxOf(0, unreadNotificationsCount - 1) else unreadNotificationsCount + 1
        }

        val auth = authHeader ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("ids", JSONArray().put(id))
                }.toString().toRequestBody("application/json".toMediaTypeOrNull())

                api.updateNotifications(auth, payload)
            } catch (_: Exception) {}
        }
    }

    fun deleteNotification(id: String) {
        val snapshot = notificationsList
        val item = notificationsList.find { it.id == id }
        notificationsList = notificationsList.filter { it.id != id }
        if (item != null && !item.isRead) {
            unreadNotificationsCount = maxOf(0, unreadNotificationsCount - 1)
        }

        val auth = authHeader ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                api.deleteNotification(auth, id)
                withContext(Dispatchers.Main) {
                    notificationToastMessage = "Removed"
                    notificationToastIsError = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    notificationsList = snapshot
                    unreadNotificationsCount = snapshot.count { !it.isRead }
                    notificationToastMessage = "Failed to remove notification"
                    notificationToastIsError = true
                }
            }
        }
    }

    fun handleNotificationClick(notification: AppNotification) {
        if (!notification.isRead) {
            markNotificationRead(notification.id, true)
        }

        val type = notification.type.lowercase().trim()
        val entityType = notification.entityType?.lowercase()?.trim() ?: ""
        val rawLink = notification.link?.trim() ?: ""
        val rawTitle = notification.title.lowercase().trim()
        val rawBody = (notification.body ?: "").lowercase().trim()

        val isStory = type.contains("story") ||
                entityType == "story" ||
                rawLink.contains("/stories") ||
                rawLink.contains("/story") ||
                rawTitle.contains("story") ||
                rawTitle.contains("stoty") ||
                rawBody.contains("story") ||
                rawBody.contains("stoty")

        if (isStory) {
            openStoryFromNotification(notification)
            return
        }

        val isJob = type.contains("job") ||
                entityType == "job" ||
                rawLink.contains("/job") ||
                rawLink.contains("/careerredirect") ||
                rawTitle.contains("job") ||
                rawBody.contains("job")

        if (isJob) {
            openJobFromNotification(notification)
            return
        }

        when {
            rawLink.contains("/profile") || rawLink.contains("/u/") || entityType == "user" -> {
                val targetUid = notification.entityId ?: notification.actorId ?: notification.actor?.id
                if (!targetUid.isNullOrBlank()) {
                    openUserProfileById(targetUid)
                } else {
                    openUserProfile(null)
                }
            }
            rawLink.contains("/reports") || entityType == "report" -> {
                val slug = notification.entityId ?: extractParamFromLink(rawLink, "slug")
                if (!slug.isNullOrBlank()) {
                    val existing = reports.find { it.slug == slug || it.id == slug }
                    if (existing != null) selectReport(existing) else navigateTo(NavRoute.JOB_REPORTS)
                } else {
                    navigateTo(NavRoute.JOB_REPORTS)
                }
            }
            else -> {
                navigateTo(NavRoute.INTELLIGENCE_FEED)
            }
        }
    }

    fun openStoryFromNotification(notification: AppNotification) {
        val storyId = notification.entityId
            ?: extractParamFromLink(notification.link, "id")
            ?: extractParamFromLink(notification.link, "storyId")
            ?: extractLastPathSegment(notification.link, listOf("stories", "story"))

        val actorId = notification.actorId ?: notification.actor?.id

        // 1. Try to find the story in the existing stories list
        val existingStory = if (!storyId.isNullOrBlank()) {
            stories.find { it.id == storyId }
        } else null ?: if (!actorId.isNullOrBlank()) {
            stories.find { it.userId == actorId }
        } else null

        if (existingStory != null) {
            viewingStory = existingStory
            navigateTo(NavRoute.STORIES)
        } else {
            // Build a story representation from notification details
            val (actorName, _) = formatNotificationDisplay(notification, getUserNameById(actorId))
            val finalActorName = actorName.ifBlank { "Community Member" }
            val authorUser = StoryUser(
                id = actorId ?: "user_${notification.id}",
                name = finalActorName,
                username = notification.actor?.username,
                profileImageUrl = notification.actor?.profileImageUrl ?: notification.imageUrl ?: getDefaultStoryAvatar(finalActorName, actorId ?: notification.id)
            )

            val createdStory = StoryType(
                id = storyId ?: "story_${notification.id}",
                userId = authorUser.id,
                user = authorUser,
                textContent = notification.body?.takeIf { it.isNotBlank() } ?: notification.title,
                mediaUrl = notification.imageUrl,
                createdAt = notification.createdAt,
                expiresAt = ""
            )

            if (stories.none { it.id == createdStory.id }) {
                stories = listOf(createdStory) + stories
            }
            viewingStory = createdStory
            navigateTo(NavRoute.STORIES)

            // Trigger sync in case new stories were posted
            loadStories()
        }
    }

    fun openJobFromNotification(notification: AppNotification) {
        val jobId = notification.entityId
            ?: extractParamFromLink(notification.link, "id")
            ?: extractParamFromLink(notification.link, "jobId")
            ?: extractLastPathSegment(notification.link, listOf("jobs", "job", "job-detail", "careerredirect"))

        // 1. Check if job is already cached
        val existingJob = if (!jobId.isNullOrBlank()) {
            activeJobs.find { it.id == jobId }
                ?: savedJobs.find { it.id == jobId }
                ?: categoryJobs.find { it.id == jobId }
                ?: selectedCompanyJobs.find { it.id == jobId }
        } else null

        if (existingJob != null) {
            selectJob(existingJob)
        } else {
            // 2. Build JobOpportunity from notification details and select it
            val (actorName, displayTitle) = formatNotificationDisplay(notification, getUserNameById(notification.actorId))
            val companyName = actorName.ifBlank { "JobsReport" }

            var cleanTitle = notification.title.trim()
            if (cleanTitle.startsWith(companyName, ignoreCase = true)) {
                cleanTitle = cleanTitle.substring(companyName.length).trim()
            }
            cleanTitle = cleanTitle
                .removePrefix("posted a new job:")
                .removePrefix("posted new job:")
                .removePrefix("shared new job:")
                .removePrefix("posted a new job")
                .removePrefix("posted new job")
                .removePrefix("shared new job")
                .trim()
            if (cleanTitle.startsWith(":")) cleanTitle = cleanTitle.substring(1).trim()
            if (cleanTitle.isBlank()) {
                cleanTitle = displayTitle.removePrefix("$companyName posted a new job: ").removePrefix("$companyName posted: ").ifBlank { "Job Opportunity" }
            }

            val fallbackJob = JobOpportunity(
                id = jobId ?: "job_${notification.id}",
                title = cleanTitle,
                company = companyName,
                description = notification.body ?: notification.title,
                logoUrl = notification.imageUrl ?: notification.actor?.profileImageUrl,
                postedAt = formatNotificationRelativeTime(notification.createdAt),
                location = "East Africa & Remote"
            )
            selectJob(fallbackJob)
        }
    }

    private fun extractParamFromLink(link: String?, paramName: String): String? {
        if (link.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(link)
            uri.getQueryParameter(paramName)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractLastPathSegment(link: String?, expectedPrefixes: List<String>): String? {
        if (link.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(link)
            val segments = uri.pathSegments ?: emptyList()
            if (segments.size >= 2 && expectedPrefixes.any { it.equals(segments[0], ignoreCase = true) }) {
                segments[1]
            } else if (segments.size == 1 && expectedPrefixes.any { it.equals(segments[0], ignoreCase = true) }) {
                null
            } else {
                segments.lastOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun openNotifications() {
        navigateTo(NavRoute.NOTIFICATIONS)
        if (userToken != null) {
            loadNotifications(0)
        }
    }

    fun submitPasscode(): Boolean {
        loginError = ""
        val trimmed = passcode.trim()
        if (trimmed == "admin" || trimmed == "admin123") {
            isAdmin = true
            isLoginModalOpen = false
            passcode = ""
            return true
        } else {
            loginError = "INVALID INTEL ACCESS CODE."
            passcode = ""
            return false
        }
    }

    fun navigateTo(route: NavRoute) {
        currentRoute = route
        isMobileMenuOpen = false
        if (route == NavRoute.INTELLIGENCE_FEED || route == NavRoute.STORIES) {
            rotateStories()
        }
    }

    // ============================================================
    // USER AUTH & SESSION (jobsreport_token sync)
    // ============================================================
    fun sanitizeJwtToken(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var t = raw.trim()
        var changed = true
        while (changed) {
            val prev = t
            t = t.removeSurrounding("\"")
                .removeSurrounding("'")
                .trim()
            if (t.startsWith("Bearer ", ignoreCase = true)) {
                t = t.substring(7).trim()
            }
            changed = (t != prev)
        }
        return t.takeIf { it.isNotBlank() }
    }

    fun isTokenExpired(token: String?): Boolean {
        val clean = sanitizeJwtToken(token) ?: return true
        val claims = extractTokenClaims(clean) ?: return true
        val expSec = claims.optLong("exp", 0L)
        if (expSec <= 0L) return false
        val nowSec = System.currentTimeMillis() / 1000L
        return nowSec >= (expSec - 60L)
    }

    val authHeader: String?
        get() {
            val raw = userToken?.takeIf { it.isNotBlank() } ?: prefs.getString("jobsreport_token", null)?.takeIf { it.isNotBlank() }
            val clean = sanitizeJwtToken(raw)
            return if (!clean.isNullOrBlank() && !isTokenExpired(clean)) "Bearer $clean" else null
        }

    val cleanUserToken: String?
        get() {
            val raw = userToken?.takeIf { it.isNotBlank() } ?: prefs.getString("jobsreport_token", null)?.takeIf { it.isNotBlank() }
            val clean = sanitizeJwtToken(raw)
            return if (!clean.isNullOrBlank() && !isTokenExpired(clean)) clean else null
        }

    val isLoggedIn: Boolean
        get() {
            val token = cleanUserToken
            return !token.isNullOrBlank() && !isTokenExpired(token)
        }

    fun handleSessionExpired() {
        android.util.Log.w("AppState", "User session expired or invalidated; resetting token")
        userToken = null
        stopNotificationPolling()
        unreadNotificationsCount = 0
        prefs.edit().remove("jobsreport_token").apply()
    }

    private fun extractTokenClaims(token: String?): JSONObject? {
        val clean = sanitizeJwtToken(token) ?: return null
        return try {
            val parts = clean.split(".")
            if (parts.size >= 2) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING), Charsets.UTF_8)
                JSONObject(payload)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractUserIdFromToken(token: String?): String? {
        val json = extractTokenClaims(token) ?: return null
        return json.optString("sub").takeIf { it.isNotBlank() } ?: json.optString("id").takeIf { it.isNotBlank() }
    }

    private fun extractEmailFromToken(token: String?): String? {
        val json = extractTokenClaims(token) ?: return null
        return json.optString("email").takeIf { it.isNotBlank() }
    }

    // ═══ FIX B — loadCurrentUser: sanitize all prefs reads so legacy "null" strings
    // are dropped on next launch. No structural changes to the flow.
    private fun loadCurrentUser() {
        try {
            val rawToken = prefs.getString("jobsreport_token", null)
            val clean = sanitizeJwtToken(rawToken)

            if (clean != null && isTokenExpired(clean)) {
                android.util.Log.w("AppState", "Stored token is expired; resetting session")
                handleSessionExpired()
                currentUser = null
                return
            }

            userToken = clean
            if (clean != rawToken && clean != null) {
                prefs.edit().putString("jobsreport_token", clean).apply()
            }

            var savedId = prefs.getString("user_id", null)
            val extractedId = extractUserIdFromToken(clean)
            if (!extractedId.isNullOrBlank() && (savedId.isNullOrBlank() || savedId.startsWith("usr_"))) {
                savedId = extractedId
                prefs.edit().putString("user_id", extractedId).apply()
            }

            val extractedEmail = extractEmailFromToken(clean)
            var savedName = prefs.getString("user_name", null).cleanDbText()
            if (savedName.isNullOrBlank() && !extractedEmail.isNullOrBlank()) {
                savedName = extractedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
            }
            val savedUsername = prefs.getString("user_username", null).cleanDbText() ?: extractedEmail?.substringBefore("@")
            val savedAvatar = prefs.getString("user_avatar", null).cleanDbText()
            val savedEmail = prefs.getString("user_email", null).cleanDbText() ?: extractedEmail
            val savedHeadline = prefs.getString("user_headline", null).cleanDbText()
            val savedLocation = prefs.getString("user_location", null).cleanDbText()
            val savedBio = prefs.getString("user_bio", null).cleanDbText()
            val savedWebsite = prefs.getString("user_website", null).cleanDbText()
            val savedWork = prefs.getString("user_work", null).cleanDbText()
            val savedEducation = prefs.getString("user_education", null).cleanDbText()
            val savedCover = prefs.getString("user_cover", null).cleanDbText()
            val isVerifiedInt = runCatching { prefs.getInt("user_is_verified", 0) }.getOrDefault(0)

            val effectiveId = savedId?.takeIf { it.isNotBlank() }
                ?: extractedId?.takeIf { it.isNotBlank() }
                ?: if (!clean.isNullOrBlank()) "user_me" else ""
            val effectiveName = savedName?.takeIf { it.isNotBlank() } ?: "User"

            if (!clean.isNullOrBlank() && effectiveId.isNotBlank()) {
                currentUser = StoryUser(
                    id = effectiveId,
                    name = effectiveName,
                    username = savedUsername,
                    email = savedEmail,
                    profileImageUrl = savedAvatar,
                    coverImageUrl = savedCover,
                    headline = savedHeadline,
                    location = savedLocation,
                    bio = savedBio,
                    work = savedWork,
                    education = savedEducation,
                    website = savedWebsite,
                    isVerified = isVerifiedInt,
                    followersCount = runCatching { prefs.getInt("user_followers", 0) }.getOrDefault(0),
                    followingCount = runCatching { prefs.getInt("user_following", 0) }.getOrDefault(0),
                    jobsCount = runCatching { prefs.getInt("user_jobs_count", 0) }.getOrDefault(0),
                    storiesCount = runCatching { prefs.getInt("user_stories_count", 0) }.getOrDefault(0),
                    jobAdvertsCount = runCatching { prefs.getInt("user_job_adverts", 0) }.getOrDefault(0),
                    joinedDate = prefs.getString("user_joined_date", null),
                    skills = emptyList()
                )
                viewModelScope.launch {
                    try {
                        val meResp = api.getAuthUserMe("Bearer $clean")
                        val meJsonStr = meResp.string()
                        val meJson = JSONObject(meJsonStr)
                        val userObj = meJson.optJSONObject("user")
                        if (userObj != null) {
                            // ═══ FIX B — use safeStr() so JSON null doesn't become "null"
                            val liveUser = StoryUser(
                                id = userObj.safeStr("id") ?: effectiveId,
                                name = userObj.safeStr("name") ?: effectiveName,
                                username = userObj.safeStr("username") ?: savedUsername,
                                email = userObj.safeStr("email") ?: savedEmail,
                                profileImageUrl = userObj.safeStr("profile_image_url") ?: userObj.safeStr("avatar") ?: savedAvatar,
                                coverImageUrl = userObj.safeStr("cover_image_url") ?: savedCover,
                                headline = userObj.safeStr("headline") ?: savedHeadline,
                                bio = userObj.safeStr("bio") ?: savedBio,
                                work = userObj.safeStr("work") ?: savedWork,
                                education = userObj.safeStr("education") ?: savedEducation,
                                location = userObj.safeStr("location") ?: savedLocation,
                                website = userObj.safeStr("website") ?: savedWebsite,
                                country = userObj.safeStr("country"),
                                role = userObj.safeStr("role"),
                                isVerified = userObj.opt("is_verified") ?: userObj.opt("isVerified"),
                                followersCount = userObj.optInt("followers_count", runCatching { prefs.getInt("user_followers", 0) }.getOrDefault(0)),
                                followingCount = userObj.optInt("following_count", runCatching { prefs.getInt("user_following", 0) }.getOrDefault(0)),
                                jobsCount = userObj.optInt("jobs_count", runCatching { prefs.getInt("user_jobs_count", 0) }.getOrDefault(0)),
                                storiesCount = userObj.optInt("stories_count", runCatching { prefs.getInt("user_stories_count", 0) }.getOrDefault(0)),
                                isFollowing = false
                            )
                            withContext(Dispatchers.Main) {
                                currentUser = liveUser
                                prefs.edit()
                                    .putString("user_id", liveUser.id)
                                    .putString("user_name", liveUser.name)
                                    .putString("user_username", liveUser.username)
                                    .putString("user_avatar", liveUser.profileImageUrl)
                                    .putString("user_cover", liveUser.coverImageUrl)
                                    .putString("user_email", liveUser.email)
                                    .putString("user_headline", liveUser.headline)
                                    .putString("user_location", liveUser.location)
                                    .putString("user_bio", liveUser.bio)
                                    .putString("user_work", liveUser.work)
                                    .putString("user_education", liveUser.education)
                                    .putString("user_website", liveUser.website)
                                    .putInt("user_is_verified", if (liveUser.isUserVerified) 1 else 0)
                                    .apply()
                            }
                        }
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 401 || e.code() == 403) {
                            withContext(Dispatchers.Main) {
                                handleSessionExpired()
                                currentUser = null
                            }
                        } else {
                            val liveUser = fetchUserProfile(effectiveId)
                            if (liveUser != null) {
                                withContext(Dispatchers.Main) {
                                    currentUser = liveUser
                                }
                            }
                        }
                    } catch (_: Exception) {
                        val liveUser = fetchUserProfile(effectiveId)
                        if (liveUser != null) {
                            withContext(Dispatchers.Main) {
                                currentUser = liveUser
                            }
                        }
                    }
                }
                loadNotifications(0)
                startNotificationPolling()
            } else {
                currentUser = null
            }
        } catch (e: Exception) {
            android.util.Log.e("AppState", "loadCurrentUser encountered error", e)
        }
    }

    // ═══ FIX B + C — updateCurrentUser now distinguishes:
    //   null  → keep current value
    //   ""    → clear the field
    //   value → set it
    // Previously "blank or null = keep old", which made it impossible to clear fields.
    fun updateCurrentUser(
        name: String,
        username: String? = null,
        email: String? = null,
        token: String? = null,
        headline: String? = null,
        location: String? = null,
        bio: String? = null,
        website: String? = null,
        avatarUrl: String? = null,
        coverUrl: String? = null,
        work: String? = null,
        education: String? = null,
        isVerified: Any? = null,
        followersCount: Int? = null,
        followingCount: Int? = null,
        jobsCount: Int? = null,
        storiesCount: Int? = null,
        jobAdvertsCount: Int? = null,
        joinedDate: String? = null,
        id: String? = null
    ) {
        val current = currentUser
        val tokenResolved = token?.trim()?.takeIf { it.isNotBlank() } ?: userToken
        val idFromToken = extractUserIdFromToken(tokenResolved)
        val resolvedId = id?.takeIf { it.isNotBlank() && !it.startsWith("usr_") }
            ?: idFromToken
            ?: current?.id?.takeIf { !it.startsWith("usr_") }
            ?: id?.takeIf { it.isNotBlank() }
            ?: current?.id
            ?: ("usr_" + java.util.UUID.randomUUID().toString().take(8))

        // Merge incoming text field with the current value:
        //   null           → keep current
        //   ""/"null"/sp   → clear (returns null)
        //   "value"        → set value
        fun merge(incoming: String?, existing: String?): String? {
            if (incoming == null) return existing.cleanDbText()
            return incoming.cleanDbText()
        }

        val resolvedAvatar = avatarUrl?.cleanDbText() ?: current?.profileImageUrl
            ?: getDefaultStoryAvatar(name, resolvedId)
        val resolvedCover = merge(coverUrl, current?.coverImageUrl)
        val resolvedHeadline = merge(headline, current?.headline)
        val resolvedLocation = merge(location, current?.location)
        val resolvedBio = merge(bio, current?.bio)
        val resolvedWebsite = merge(website, current?.website)
        val resolvedWork = merge(work, current?.work)
        val resolvedEducation = merge(education, current?.education)
        val resolvedVerified = isVerified ?: current?.isVerified

        val updated = StoryUser(
            id = resolvedId,
            name = name.cleanDbText() ?: "User",
            username = username?.cleanDbText() ?: current?.username,
            email = email?.cleanDbText() ?: current?.email,
            profileImageUrl = resolvedAvatar,
            coverImageUrl = resolvedCover,
            headline = resolvedHeadline,
            location = resolvedLocation,
            bio = resolvedBio,
            work = resolvedWork,
            education = resolvedEducation,
            website = resolvedWebsite,
            isVerified = resolvedVerified,
            followersCount = followersCount ?: current?.followersCount ?: 0,
            followingCount = followingCount ?: current?.followingCount ?: 0,
            jobsCount = jobsCount ?: current?.jobsCount ?: 0,
            storiesCount = storiesCount ?: current?.storiesCount ?: 0,
            jobAdvertsCount = jobAdvertsCount ?: current?.jobAdvertsCount ?: 0,
            joinedDate = joinedDate ?: current?.joinedDate,
            skills = current?.skills ?: emptyList()
        )
        currentUser = updated
        if (!token.isNullOrBlank()) {
            val cleanTok = sanitizeJwtToken(token)
            userToken = cleanTok
            if (cleanTok != null) {
                prefs.edit().putString("jobsreport_token", cleanTok).apply()
            }
            loadNotifications(0)
            startNotificationPolling()
        }
        val isVerifiedInt = if (updated.isUserVerified) 1 else 0
        prefs.edit()
            .putString("user_id", resolvedId)
            .putString("user_name", updated.name)
            .putString("user_username", updated.username)
            .putString("user_avatar", resolvedAvatar)
            .putString("user_cover", resolvedCover)
            .putString("user_email", updated.email)
            .putString("user_headline", resolvedHeadline)
            .putString("user_location", resolvedLocation)
            .putString("user_bio", resolvedBio)
            .putString("user_work", resolvedWork)
            .putString("user_education", resolvedEducation)
            .putString("user_website", resolvedWebsite)
            .putInt("user_is_verified", isVerifiedInt)
            .putInt("user_followers", updated.followersCount ?: 0)
            .putInt("user_following", updated.followingCount ?: 0)
            .putInt("user_jobs_count", updated.jobsCount ?: 0)
            .putInt("user_stories_count", updated.storiesCount ?: 0)
            .putInt("user_job_adverts", updated.jobAdvertsCount ?: 0)
            .apply()
    }

    // ═══ FIX B — fetchUserProfile: use safeStr() on every text field.
    suspend fun fetchUserProfile(userId: String): StoryUser? = withContext(Dispatchers.IO) {
        try {
            val res = runCatching { api.getUserProfile(userId) }.getOrNull()
            if (res != null) {
                val jsonStr = res.string().trim()
                val json = JSONObject(jsonStr)
                val userObj = json.optJSONObject("user")
                    ?: if (json.has("id") || json.has("name") || json.has("is_verified") || json.has("username")) json else null
                if (userObj != null) {
                    val fetchedUser = StoryUser(
                        id = userObj.safeStr("id") ?: userId,
                        name = userObj.safeStr("name") ?: "User",
                        username = userObj.safeStr("username"),
                        email = userObj.safeStr("email"),
                        profileImageUrl = userObj.safeStr("profile_image_url") ?: userObj.safeStr("avatar"),
                        coverImageUrl = userObj.safeStr("cover_image_url"),
                        headline = userObj.safeStr("headline"),
                        bio = userObj.safeStr("bio"),
                        work = userObj.safeStr("work"),
                        education = userObj.safeStr("education"),
                        location = userObj.safeStr("location"),
                        website = userObj.safeStr("website"),
                        country = userObj.safeStr("country"),
                        role = userObj.safeStr("role"),
                        isVerified = userObj.opt("is_verified") ?: userObj.opt("isVerified"),
                        followersCount = userObj.optInt("followers_count", 0),
                        followingCount = userObj.optInt("following_count", 0),
                        jobsCount = userObj.optInt("jobs_count", 0),
                        storiesCount = userObj.optInt("stories_count", 0),
                        isFollowing = userObj.optBoolean("is_following", false),
                        createdAt = userObj.safeStr("created_at")
                    )
                    withContext(Dispatchers.Main) {
                        userProfilesCache[userId] = fetchedUser
                        verifiedUsersMap[userId] = fetchedUser.isUserVerified
                    }
                    return@withContext fetchedUser
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchUserJobs(userId: String): List<JobOpportunity> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext emptyList()
        try {
            val res = runCatching { api.getUserJobs(userId, limit = 100) }.getOrNull()
            if (res != null) {
                val rawStr = res.string().trim()
                val list = mutableListOf<JobOpportunity>()
                val arr = if (rawStr.startsWith("[")) {
                    JSONArray(rawStr)
                } else if (rawStr.startsWith("{")) {
                    val json = JSONObject(rawStr)
                    json.optJSONArray("jobs")
                        ?: json.optJSONArray("data")
                        ?: json.optJSONArray("userJobs")
                } else null

                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            JobOpportunity(
                                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                                title = obj.optString("title", "Job Title"),
                                company = obj.optString("company", obj.optString("company_name", "Company")),
                                location = obj.optString("location", "Remote"),
                                salary = obj.optString("salary", null),
                                role = obj.optString("role", obj.optString("job_category", "Technology & Engineering")),
                                logoUrl = obj.optString("logoUrl", obj.optString("company_logo", null)),
                                active = if (obj.has("active")) obj.optBoolean("active", true) else (obj.optInt("is_active", 1) == 1),
                                postedAt = obj.optString("postedAt", obj.optString("posted_at", "Recent")),
                                expiresAt = obj.optString("expiresAt", obj.optString("expires_at", null)),
                                description = obj.optString("description", null),
                                companyWebsite = obj.optString("companyWebsite", obj.optString("company_website", null)),
                                whatsappNumber = obj.optString("whatsapp_number", obj.optString("whatsappNumber", null)),
                                applicationInstructions = obj.optString("application_instructions", obj.optString("applicationInstructions", null)),
                                url = obj.optString("url", null),
                                employmentType = obj.optString("employment_type", obj.optString("employmentType", "FULL_TIME")),
                                workplaceType = obj.optString("workplace_type", obj.optString("workplaceType", "Onsite")),
                                educationLevel = obj.optString("education_level", obj.optString("educationLevel", "Degree / Diploma")),
                                experienceMonths = obj.optInt("experience_months", obj.optInt("experienceMonths", 24)),
                                city = obj.optString("city", "Dar es Salaam"),
                                region = obj.optString("region", "Dar es Salaam"),
                                country = obj.optString("country", "Tanzania"),
                                jobCategory = obj.optString("job_category", obj.optString("jobCategory", "Technology & Engineering"))
                            )
                        )
                    }
                }
                return@withContext list
            }
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun followUserApi(userId: String, shouldFollow: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader
            if (shouldFollow) {
                runCatching { api.followUser(auth, userId) }
            } else {
                runCatching { api.unfollowUser(auth, userId) }
            }
            true
        } catch (_: Exception) {
            true
        }
    }

    // ═══ FIX B — saveProfileApi sends "" (empty string) for cleared text fields
    // and only includes a field if the caller passed a non-null argument. That
    // means the backend stores an empty string (or NULL) — never the literal "null".
    suspend fun saveProfileApi(
        name: String? = null,
        headline: String? = null,
        bio: String? = null,
        work: String? = null,
        education: String? = null,
        location: String? = null,
        website: String? = null,
        avatarUrl: String? = null,
        coverUrl: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val auth = authHeader
        if (auth.isNullOrBlank()) {
            android.util.Log.e("AppState", "saveProfileApi: authHeader is null")
            return@withContext false
        }

        // "" means clear; null means keep current
        fun cleanOutgoing(v: String?): String {
            if (v == null) return ""
            val t = v.trim()
            return if (t.equals("null", ignoreCase = true)) "" else t
        }

        try {
            val json = JSONObject()
            val effectiveName = cleanOutgoing(name).ifBlank {
                cleanOutgoing(currentUser?.name).ifBlank { "User" }
            }
            json.put("name", effectiveName)

            // Only include fields the caller explicitly targeted. Empty string
            // tells the server "clear this field" — never send JSONObject.NULL
            // because that ends up round-tripping as the literal string "null".
            if (headline != null)  json.put("headline",  cleanOutgoing(headline))
            if (bio != null)       json.put("bio",       cleanOutgoing(bio))
            if (work != null)      json.put("work",      cleanOutgoing(work))
            if (education != null) json.put("education", cleanOutgoing(education))
            if (location != null)  json.put("location",  cleanOutgoing(location))
            if (website != null)   json.put("website",   cleanOutgoing(website))
            if (avatarUrl != null) json.put("profile_image_url", avatarUrl.trim())
            if (coverUrl != null)  json.put("cover_image_url",   coverUrl.trim())

            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val responseBody = api.updateProfile(auth, body)
            val jsonStr = responseBody.string()
            val respJson = JSONObject(jsonStr)
            val userObj = respJson.optJSONObject("user")

            val serverId = userObj?.safeStr("id") ?: currentUser?.id ?: extractUserIdFromToken(cleanUserToken)

            withContext(Dispatchers.Main) {
                updateCurrentUser(
                    id = serverId,
                    name = userObj?.safeStr("name") ?: effectiveName,
                    username = userObj?.safeStr("username") ?: currentUser?.username,
                    // Pass through exactly what we sent, falling back to the server echo.
                    // This ensures the ViewModel matches what the user sees in the modal.
                    headline = userObj?.safeStr("headline") ?: headline,
                    location = userObj?.safeStr("location") ?: location,
                    bio = userObj?.safeStr("bio") ?: bio,
                    website = userObj?.safeStr("website") ?: website,
                    avatarUrl = userObj?.safeStr("profile_image_url") ?: avatarUrl,
                    coverUrl = userObj?.safeStr("cover_image_url") ?: coverUrl,
                    work = userObj?.safeStr("work") ?: work,
                    education = userObj?.safeStr("education") ?: education
                )
            }
            true
        } catch (e: retrofit2.HttpException) {
            val errBody = e.response()?.errorBody()?.string() ?: ""
            android.util.Log.e("AppState", "saveProfileApi HTTP ${e.code()}: $errBody", e)
            false
        } catch (e: Exception) {
            android.util.Log.e("AppState", "saveProfileApi failed", e)
            false
        }
    }

    suspend fun deleteUserJob(jobId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader
            runCatching { api.deleteJob(auth, jobId) }
            activeJobs = activeJobs.filter { it.id != jobId }
            true
        } catch (_: Exception) {
            activeJobs = activeJobs.filter { it.id != jobId }
            true
        }
    }

    fun openAuth() {
        isUserAuthModalOpen = true
    }

    fun closeAuth() {
        isUserAuthModalOpen = false
    }

    fun logout() {
        isAdmin = false
        userToken = null
        currentUser = null
        stopNotificationPolling()
        notificationsList = emptyList()
        unreadNotificationsCount = 0
        prefs.edit()
            .remove("jobsreport_token")
            .remove("user_id")
            .remove("user_name")
            .remove("user_username")
            .remove("user_avatar")
            .remove("user_cover")
            .remove("user_email")
            .remove("user_headline")
            .remove("user_location")
            .remove("user_bio")
            .remove("user_website")
            .remove("user_work")
            .remove("user_education")
            .remove("user_is_verified")
            .apply()
        if (currentRoute == NavRoute.USER_PROFILE) {
            currentRoute = NavRoute.INTELLIGENCE_FEED
        }
    }

    suspend fun login(email: String, password: String, remember: Boolean = true): com.example.data.AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("remember", remember)
            }
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val responseBody = try {
                api.loginRaw(body)
            } catch (e: retrofit2.HttpException) {
                val errBody = e.response()?.errorBody()?.string() ?: ""
                val errMsg = try {
                    val errJson = JSONObject(errBody)
                    errJson.optString("message", errJson.optString("error", "Invalid credentials"))
                } catch (_: Exception) {
                    "Invalid email or password"
                }
                return@withContext com.example.data.AuthResult(success = false, message = errMsg)
            } catch (e: Exception) {
                return@withContext com.example.data.AuthResult(success = false, message = e.message ?: "Authentication failed. Please check network.")
            }

            val jsonStr = responseBody.string()
            val respJson = org.json.JSONObject(jsonStr)
            val rawToken = respJson.optString("token", respJson.optString("accessToken", ""))
            val token = rawToken.trim().removeSurrounding("\"").removePrefix("Bearer ").removePrefix("bearer ").trim()

            if (token.isNotBlank()) {
                val userObj = respJson.optJSONObject("user")
                val name = userObj?.safeStr("name") ?: email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                val username = userObj?.safeStr("username") ?: email.substringBefore("@")
                val avatar = userObj?.safeStr("profile_image_url") ?: userObj?.safeStr("avatar")
                    ?: getDefaultStoryAvatar(name, username)
                val userId = userObj?.safeStr("id")
                    ?: extractUserIdFromToken(token)
                    ?: ""
                val isVerifiedVal = userObj?.opt("is_verified") ?: userObj?.opt("isVerified")

                withContext(Dispatchers.Main) {
                    updateCurrentUser(
                        id = userId,
                        name = name,
                        username = username,
                        email = email.trim(),
                        token = token,
                        avatarUrl = avatar,
                        isVerified = isVerifiedVal
                    )
                    if (storyDraftText.isNotBlank() || storyDraftUploadedUrl != null) {
                        isCreateStoryOpen = true
                    }
                    if (userId.isNotBlank()) {
                        viewModelScope.launch {
                            val live = fetchUserProfile(userId)
                            if (live != null) {
                                withContext(Dispatchers.Main) {
                                    currentUser = live.copy(email = email.trim())
                                }
                            }
                        }
                    }
                }
                return@withContext com.example.data.AuthResult(success = true, token = token, message = "Welcome back, $name!")
            } else {
                val errMsg = respJson.optString("message", respJson.optString("error", "Invalid credentials. Please check your email and password."))
                return@withContext com.example.data.AuthResult(success = false, message = errMsg)
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Authentication failed. Please check your network connection and credentials."
            return@withContext com.example.data.AuthResult(success = false, message = msg)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        username: String? = null,
        headline: String? = null,
        location: String? = null
    ): com.example.data.AuthResult = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("name", name.trim())
                put("email", email.trim())
                put("password", password)
                if (!username.isNullOrBlank()) put("username", username.trim())
                if (!headline.isNullOrBlank()) put("headline", headline.trim())
                if (!location.isNullOrBlank()) put("location", location.trim())
            }
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val responseBody = try {
                api.registerRaw(body)
            } catch (e: retrofit2.HttpException) {
                val errBody = e.response()?.errorBody()?.string() ?: ""
                val errMsg = try {
                    val errJson = JSONObject(errBody)
                    errJson.optString("message", errJson.optString("error", "Registration failed"))
                } catch (_: Exception) {
                    "Registration failed"
                }
                return@withContext com.example.data.AuthResult(success = false, message = errMsg)
            } catch (e: Exception) {
                return@withContext com.example.data.AuthResult(success = false, message = e.message ?: "Registration failed")
            }

            val jsonStr = responseBody.string()
            val respJson = org.json.JSONObject(jsonStr)
            val rawToken = respJson.optString("token", respJson.optString("accessToken", ""))
            val token = rawToken.trim().removeSurrounding("\"").removePrefix("Bearer ").removePrefix("bearer ").trim()
            val userObj = respJson.optJSONObject("user")
            val resolvedName = userObj?.safeStr("name") ?: name.trim()
            val resolvedUsername = userObj?.safeStr("username") ?: (username?.trim() ?: email.substringBefore("@"))
            val avatar = userObj?.safeStr("profile_image_url") ?: userObj?.safeStr("avatar")
                ?: getDefaultStoryAvatar(resolvedName, resolvedUsername)
            val userId = userObj?.safeStr("id")
                ?: extractUserIdFromToken(token)
                ?: ""
            val isVerifiedVal = userObj?.opt("is_verified") ?: userObj?.opt("isVerified")

            if (token.isNotBlank()) {
                withContext(Dispatchers.Main) {
                    updateCurrentUser(
                        id = userId,
                        name = resolvedName,
                        username = resolvedUsername,
                        email = email.trim(),
                        token = token,
                        avatarUrl = avatar,
                        headline = headline?.trim(),
                        location = location?.trim(),
                        isVerified = isVerifiedVal
                    )
                    if (storyDraftText.isNotBlank() || storyDraftUploadedUrl != null) {
                        isCreateStoryOpen = true
                    }
                    if (userId.isNotBlank()) {
                        viewModelScope.launch {
                            val live = fetchUserProfile(userId)
                            if (live != null) {
                                withContext(Dispatchers.Main) {
                                    currentUser = live.copy(email = email.trim())
                                }
                            }
                        }
                    }
                }
                return@withContext com.example.data.AuthResult(success = true, token = token, message = "Account created! Welcome, $resolvedName.")
            } else {
                val errMsg = respJson.optString("message", respJson.optString("error", "Registration failed."))
                return@withContext com.example.data.AuthResult(success = false, message = errMsg)
            }
        } catch (e: Exception) {
            return@withContext com.example.data.AuthResult(success = false, message = e.message ?: "Registration failed")
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = com.example.data.ForgotPasswordPayload(email = email.trim())
            runCatching { api.forgotPassword(payload) }
            Unit
        }
    }

    // ============================================================
    // STORY SERVICE METHODS
    // ============================================================
    fun loadStories() {
        viewModelScope.launch {
            isStoriesLoading = true
            try {
                withContext(Dispatchers.IO) {
                    val response = runCatching { api.getStoriesRaw() }.getOrNull()
                    if (response != null) {
                        val rawStr = response.string().trim()
                        val parsed = parseStories(rawStr)
                        if (!parsed.isNullOrEmpty()) {
                            val active = parsed.filter { !isStoryExpired(it) }
                            withContext(Dispatchers.Main) {
                                val serverStoryIds = active.map { it.id }.toSet()
                                val currentUid = currentUser?.id
                                val recentOwnStories = stories.filter {
                                    (currentUid != null && it.userId == currentUid) &&
                                    it.id !in serverStoryIds &&
                                    !isStoryExpired(it)
                                }
                                stories = recentOwnStories + active
                                active.forEach { s ->
                                    if (s.user != null) {
                                        userProfilesCache[s.userId] = s.user
                                        if (s.user.isVerified != null) {
                                            verifiedUsersMap[s.userId] = s.user.isUserVerified
                                        }
                                    }
                                }
                            }

                            val uniqueAuthorIds = active.map { it.userId }.distinct()
                            uniqueAuthorIds.forEach { uid ->
                                if (!verifiedUsersMap.containsKey(uid) || !userProfilesCache.containsKey(uid)) {
                                    val fetchedUser = fetchUserProfile(uid)
                                    if (fetchedUser != null) {
                                        withContext(Dispatchers.Main) {
                                            userProfilesCache[uid] = fetchedUser
                                            verifiedUsersMap[uid] = fetchedUser.isUserVerified
                                        }
                                    }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                val currentUid = currentUser?.id
                                val recentOwnStories = stories.filter {
                                    (currentUid != null && it.userId == currentUid) && !isStoryExpired(it)
                                }
                                stories = recentOwnStories
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            val currentUid = currentUser?.id
                            val recentOwnStories = stories.filter {
                                (currentUid != null && it.userId == currentUid) && !isStoryExpired(it)
                            }
                            stories = recentOwnStories
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    val currentUid = currentUser?.id
                    val recentOwnStories = stories.filter {
                        (currentUid != null && it.userId == currentUid) && !isStoryExpired(it)
                    }
                    stories = recentOwnStories
                }
            } finally {
                isStoriesLoading = false
            }
        }
    }

    private fun parseStories(rawJson: String): List<StoryType>? {
        return try {
            if (rawJson.startsWith("[")) {
                storyListAdapter.fromJson(rawJson)
            } else if (rawJson.startsWith("{")) {
                val obj = storiesResponseAdapter.fromJson(rawJson)
                if (obj?.stories != null) {
                    obj.stories
                } else {
                    null
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun recordStoryView(storyId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    runCatching { api.recordStoryView(storyId) }
                }
                withContext(Dispatchers.Main) {
                    stories = stories.map {
                        if (it.id == storyId) it.copy(viewsCount = (it.viewsCount ?: 0) + 1) else it
                    }
                    if (viewingStory?.id == storyId) {
                        viewingStory = viewingStory?.let { it.copy(viewsCount = (it.viewsCount ?: 0) + 1) }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun createStory(payload: CreateStoryPayload): Result<Unit> = withContext(Dispatchers.IO) {
        val token = cleanUserToken
        if (token.isNullOrBlank() || isTokenExpired(token)) {
            android.util.Log.e("AppState", "createStory: auth token is null or expired")
            withContext(Dispatchers.Main) {
                handleSessionExpired()
            }
            return@withContext Result.failure(IllegalStateException("Your session has expired. Please log in to post your story."))
        }
        val auth = "Bearer $token"

        try {
            val json = JSONObject().apply {
                put("type", payload.type)
                put("expires_at", payload.expiresAt)
                if (!payload.textContent.isNullOrBlank()) {
                    put("text_content", payload.textContent.trim())
                }
                if (!payload.backgroundStyle.isNullOrBlank()) {
                    put("background_style", payload.backgroundStyle.trim())
                }
                if (!payload.mediaUrl.isNullOrBlank()) {
                    put("media_url", payload.mediaUrl.trim())
                }
                if (!payload.mediaThumb.isNullOrBlank()) {
                    put("media_thumb", payload.mediaThumb.trim())
                }
            }
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val responseBody = api.createStoryRaw(auth, body)
            val jsonStr = responseBody.string()
            val respObj = JSONObject(jsonStr)

            val storyObj = respObj.optJSONObject("story")
            if (storyObj != null) {
                val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date())

                val userObj = currentUser ?: StoryUser(
                    id = storyObj.optString("user_id", "user_me"),
                    name = "You",
                    profileImageUrl = getDefaultStoryAvatar("You", "you")
                )

                val serverStory = StoryType(
                    id = storyObj.safeStr("id") ?: "",
                    userId = storyObj.safeStr("user_id") ?: userObj.id,
                    slug = storyObj.safeStr("slug") ?: "",
                    type = storyObj.safeStr("type") ?: payload.type,
                    textContent = storyObj.safeStr("text_content") ?: payload.textContent,
                    backgroundStyle = storyObj.safeStr("background_style") ?: payload.backgroundStyle,
                    mediaUrl = storyObj.safeStr("media_url") ?: payload.mediaUrl,
                    mediaThumb = storyObj.safeStr("media_thumb") ?: payload.mediaThumb,
                    createdAt = storyObj.safeStr("created_at") ?: nowIso,
                    expiresAt = storyObj.safeStr("expires_at") ?: payload.expiresAt,
                    isActive = storyObj.opt("is_active") ?: 1,
                    viewsCount = storyObj.optInt("views_count", 0),
                    user = userObj
                )
                withContext(Dispatchers.Main) {
                    stories = listOf(serverStory) + stories.filter { it.id != serverStory.id }
                    currentUser = currentUser?.copy(
                        storiesCount = (currentUser?.storiesCount ?: 0) + 1
                    )
                    clearStoryDraft()
                }
            }
            Result.success(Unit)
        } catch (e: retrofit2.HttpException) {
            val errBody = e.response()?.errorBody()?.string() ?: ""
            if (e.code() == 401 || e.code() == 403) {
                withContext(Dispatchers.Main) {
                    handleSessionExpired()
                }
                Result.failure(IllegalStateException("Your session has expired. Please log in to post your story."))
            } else {
                val errMsg = try {
                    JSONObject(errBody).optString("error", "Failed to share story (${e.code()})")
                } catch (_: Exception) {
                    "Failed to share story (${e.code()})"
                }
                android.util.Log.e("AppState", "createStory HTTP ${e.code()}: $errBody", e)
                Result.failure(IllegalStateException(errMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("AppState", "createStory error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteStory(storyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = authHeader
            api.deleteStory(auth, storyId)
            withContext(Dispatchers.Main) {
                stories = stories.filter { it.id != storyId }
                if (viewingStory?.id == storyId) {
                    viewingStory = null
                }
            }
            Unit
        }
    }

    suspend fun uploadMedia(
        mainBytes: ByteArray,
        mainName: String,
        mainMime: String,
        thumbBytes: ByteArray,
        thumbName: String,
        thumbMime: String
    ): Result<UploadResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = authHeader

            val fileReq = mainBytes.toRequestBody(mainMime.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", mainName, fileReq)

            val thumbReq = thumbBytes.toRequestBody(thumbMime.toMediaTypeOrNull())
            val thumbPart = MultipartBody.Part.createFormData("thumbnail", thumbName, thumbReq)

            val nameReq = mainName.toRequestBody("text/plain".toMediaTypeOrNull())
            val namePart = MultipartBody.Part.createFormData("name", null, nameReq)

            val alt = "Story by ${currentUser?.name ?: "User"}"
            val altReq = alt.toRequestBody("text/plain".toMediaTypeOrNull())
            val altPart = MultipartBody.Part.createFormData("altText", null, altReq)

            val responseBody = api.uploadMedia(auth, filePart, thumbPart, namePart, altPart)
            val jsonStr = responseBody.string()
            val parsed = uploadResponseAdapter.fromJson(jsonStr)
            parsed ?: run {
                val json = JSONObject(jsonStr)
                UploadResponse(
                    url = json.safeStr("url") ?: json.safeStr("dataUrl"),
                    thumbnailUrl = json.safeStr("thumbnail_url") ?: json.safeStr("url"),
                    dataUrl = json.safeStr("dataUrl")
                )
            }
        }
    }


    // ═══ FIX A — uploadImage: check auth FIRST, wrap compressor with a specific
    // error, surface HttpException code/body, log the raw response, parse url/dataUrl.
    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        uploadError = null
        try {
            // 0. Auth check before compressing — gives a clear error if token is gone
            val auth = authHeader
            if (auth.isNullOrBlank()) {
                uploadError = "Please sign in to upload"
                return@withContext null
            }

            // 1. Compress
            val compressed = try {
                ImageCompressor.processImageUri(context, uri).getOrThrow()
            } catch (e: Exception) {
                uploadError = "Could not process image: ${e.message ?: "unsupported format"}"
                return@withContext null
            }

            // 2. Build multipart parts
            val filePart = MultipartBody.Part.createFormData(
                "file",
                compressed.mainFileName,
                compressed.mainBytes.toRequestBody(compressed.mainMime.toMediaTypeOrNull())
            )
            val namePart = MultipartBody.Part.createFormData("name", compressed.mainFileName)
            val altPart = MultipartBody.Part.createFormData("altText", "")

            // 3. Upload
            val resp = try {
                api.uploadMedia(auth, filePart, null, namePart, altPart)
            } catch (e: retrofit2.HttpException) {
                val errBody = e.response()?.errorBody()?.string().orEmpty()
                uploadError = when (e.code()) {
                    401, 403 -> "Session expired — please sign in again"
                    413 -> "Image too large"
                    else -> "Upload failed (${e.code()}): $errBody"
                }
                if (e.code() == 401 || e.code() == 403) handleSessionExpired()
                return@withContext null
            }

            // 4. Parse { url } or { dataUrl }
            val rawStr = resp.string()
            android.util.Log.d("AppState", "upload response: $rawStr")
            val json = JSONObject(rawStr)
            val url = (json.safeStr("url") ?: json.safeStr("dataUrl")).orEmpty()
            if (url.isBlank()) {
                uploadError = "Server did not return an image URL"
                return@withContext null
            }
            url
        } catch (e: Exception) {
            android.util.Log.e("AppState", "uploadImage failed", e)
            uploadError = e.message ?: "Upload failed"
            null
        }
    }

    suspend fun updateProfileImage(context: Context, uri: Uri, field: String): String? {
        val url = uploadImage(context, uri) ?: return null
        val u = currentUser ?: return null
        saveProfileApi(
            name = u.name,
            headline = u.headline,
            bio = u.bio,
            work = u.work,
            education = u.education,
            location = u.location,
            website = u.website,
            avatarUrl = if (field == "avatar") url else u.profileImageUrl,
            coverUrl = if (field == "cover") url else u.coverImageUrl
        )
        return url
    }

    suspend fun fetchAllCompaniesCatalog(): List<Company> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAllCompaniesRaw(all = true).string()
            val list = mutableListOf<Company>()
            if (response.trim().startsWith("[")) {
                val array = org.json.JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Company(
                            id = obj.optString("id", "comp-$i"),
                            name = obj.optString("name", ""),
                            industry = obj.optString("industry", null),
                            logoUrl = obj.optString("logoUrl", null),
                            url = obj.optString("url", null),
                            description = obj.optString("description", null),
                            country = obj.optString("country", null)
                        )
                    )
                }
            } else {
                val root = org.json.JSONObject(response)
                val array = root.optJSONArray("companies") ?: org.json.JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Company(
                            id = obj.optString("id", "comp-$i"),
                            name = obj.optString("name", ""),
                            industry = obj.optString("industry", null),
                            logoUrl = obj.optString("logoUrl", null),
                            url = obj.optString("url", null),
                            description = obj.optString("description", null),
                            country = obj.optString("country", null)
                        )
                    )
                }
            }
            if (list.isNotEmpty()) list.filter { it.name.isNotBlank() } else sampleCompanies
        } catch (_: Exception) {
            sampleCompanies
        }
    }

    suspend fun createCompany(jsonPayload: String): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader
            val reqBody = jsonPayload.toRequestBody("application/json".toMediaTypeOrNull())
            api.createCompanyRaw(auth, reqBody).string()
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Failed to create company")
        }
    }

    suspend fun saveUserWork(companyName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader ?: return@withContext false
            val json = JSONObject().apply { put("work", companyName.trim()) }
            val reqBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            api.updateProfile(auth, reqBody)
            withContext(Dispatchers.Main) {
                currentUser = currentUser?.copy(work = companyName.trim())
                prefs.edit().putString("user_work", companyName.trim()).apply()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitUserJob(
        jsonPayload: String,
        isEdit: Boolean,
        jobId: String?
    ): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader ?: return@withContext Pair(false, "User not authenticated")
            val reqBody = jsonPayload.toRequestBody("application/json".toMediaTypeOrNull())
            if (isEdit && !jobId.isNullOrBlank()) {
                val userId = currentUser?.id ?: ""
                api.updateUserJob(auth, userId, reqBody)
            } else {
                api.createUserJob(auth, reqBody)
            }
            withContext(Dispatchers.Main) {
                fetchDashboardData()
            }
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Failed to submit job")
        }
    }
}
