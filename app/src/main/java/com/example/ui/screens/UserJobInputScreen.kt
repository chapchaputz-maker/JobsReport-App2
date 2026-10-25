package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Company
import com.example.data.JobAttachment
import com.example.data.JobOpportunity
import com.example.state.AppViewModel
import com.example.state.NavRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// -------------------------------------------------------------
// PALETTE & STYLING CONSTANTS (Matched to Web UserJobInput.tsx)
// -------------------------------------------------------------
private val DarkBackground = Color(0xFF050B18)
private val DialogCardBg = Color(0xFF0B1120)
private val PrimaryBlue = Color(0xFF1877F2)
private val BrandPurple = Color(0xFF7B4DFF)
private val AccentCyan = Color(0xFF4A9EFF)
private val BorderWhite10 = Color(0x1AFFFFFF)
private val BorderWhite05 = Color(0x0DFFFFFF)
private val SurfaceWhite02 = Color(0x05FFFFFF)
private val SurfaceWhite04 = Color(0x0AFFFFFF)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray300 = Color(0xFFD1D5DB)
private val TextGray400 = Color(0xFF9CA3AF)
private val TextGray500 = Color(0xFF6B7280)
private val TextGray600 = Color(0xFF4B5563)
private val EmeraldColor = Color(0xFF10B981)
private val AmberColor = Color(0xFFF59E0B)
private val RedColor = Color(0xFFEF4444)

private enum class JobInputStep {
    CHOOSE_COMPANY,
    CREATE_COMPANY,
    JOB_FORM
}

data class JobAttachmentItem(
    val url: String,
    val thumbnail: String,
    val name: String,
    val type: String
)

@Composable
fun UserJobInputScreen(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authUser = viewModel.currentUser
    val editingJob = viewModel.editingJob
    val isEditing = editingJob != null

    // Back button behavior
    BackHandler {
        onClose()
    }

    // Auth guard: If user not logged in, prompt sign in and close
    LaunchedEffect(viewModel.userToken) {
        if (viewModel.userToken == null) {
            viewModel.openAuth()
            onClose()
        }
    }

    val profileWork = authUser?.work?.trim().orEmpty()

    // Flow State
    var step by remember {
        mutableStateOf(
            if (isEditing || profileWork.isNotBlank()) JobInputStep.JOB_FORM
            else JobInputStep.CHOOSE_COMPANY
        )
    }
    var selectedCompany by remember {
        mutableStateOf(editingJob?.company ?: profileWork)
    }

    // Company & Role Catalogs
    var companies by remember { mutableStateOf<List<Company>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingCatalog by remember { mutableStateOf(true) }

    // Alert Messages
    var messageText by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    fun showMessage(msg: String, isError: Boolean = false) {
        messageText = msg
        messageIsError = isError
        scope.launch {
            delay(5000)
            if (messageText == msg) messageText = null
        }
    }

    // Load companies and categories from backend
    LaunchedEffect(Unit) {
        loadingCatalog = true
        val compList = viewModel.fetchAllCompaniesCatalog()
        companies = compList
        val catList = viewModel.categories.map { it.name }.filter { it.isNotBlank() }
        categories = if (catList.isNotEmpty()) catList else listOf(
            "Technology & Engineering",
            "Finance & Accounting",
            "Sales & Marketing",
            "Operations & Logistics",
            "Human Resources",
            "Healthcare & Medical",
            "Education & Training",
            "Customer Service",
            "Hospitality & Tourism",
            "General Management"
        )
        loadingCatalog = false
    }

    // ---------------------------------------------------------
    // JOB FORM FIELDS
    // ---------------------------------------------------------
    var title by remember { mutableStateOf(editingJob?.title.orEmpty()) }
    var selectedRole by remember {
        mutableStateOf(editingJob?.jobCategory ?: editingJob?.role ?: "Technology & Engineering")
    }
    var location by remember { mutableStateOf(editingJob?.location ?: "Dar es Salaam") }
    var salary by remember { mutableStateOf(editingJob?.salary ?: "Not Disclosed") }

    // Expiry date (default 30 days from now)
    val defaultExpiry = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
    var expiresAt by remember {
        mutableStateOf(editingJob?.expiresAt?.take(10) ?: defaultExpiry)
    }

    // Application Type: 'url' | 'email' | 'whatsapp' | 'instructions'
    var applicationType by remember {
        mutableStateOf(
            when {
                editingJob?.url?.startsWith("mailto:") == true -> "email"
                editingJob?.url?.contains("wa.me") == true || !editingJob?.whatsappNumber.isNullOrBlank() -> "whatsapp"
                !editingJob?.applicationInstructions.isNullOrBlank() -> "instructions"
                else -> "url"
            }
        )
    }
    var applicationValue by remember {
        mutableStateOf(
            when {
                editingJob?.url?.startsWith("mailto:") == true -> editingJob.url.removePrefix("mailto:")
                !editingJob?.whatsappNumber.isNullOrBlank() -> editingJob.whatsappNumber.orEmpty()
                !editingJob?.applicationInstructions.isNullOrBlank() -> editingJob.applicationInstructions.orEmpty()
                else -> editingJob?.url.orEmpty()
            }
        )
    }

    // Locations
    var streetAddress by remember { mutableStateOf(editingJob?.streetAddress ?: "Mwai Kibaki Road") }
    var city by remember { mutableStateOf(editingJob?.city ?: "Dar es Salaam") }
    var region by remember { mutableStateOf(editingJob?.region ?: "Kinondoni") }
    var country by remember { mutableStateOf(editingJob?.country ?: "Tanzania") }
    var postcode by remember { mutableStateOf(editingJob?.postcode ?: "14111") }

    // Description & Editor Mode
    var description by remember {
        mutableStateOf(
            editingJob?.description
                ?: "<h3>Role Overview</h3><p>We are looking for a dedicated and experienced professional to join our team.</p><h3>Key Responsibilities</h3><ul><li>Execute core initiatives and project deliverables</li><li>Collaborate cross-functionally with team members</li></ul><h3>Requirements</h3><ul><li>Relevant education or equivalent practical experience</li><li>Strong communication and problem-solving skills</li></ul>"
        )
    }
    var descEditMode by remember { mutableStateOf("visual") } // "visual" | "html"

    // Attachments
    val attachments = remember {
        mutableStateListOf<JobAttachmentItem>().apply {
            editingJob?.images?.forEach { img ->
                add(JobAttachmentItem(img.url, img.thumbnail ?: img.url, img.name ?: "Attachment", img.type ?: "image"))
            }
        }
    }

    // Advanced Options
    var showAdvanced by remember { mutableStateOf(false) }
    var advancedCategory by remember { mutableStateOf(editingJob?.jobCategory.orEmpty()) }
    var advancedIndustry by remember { mutableStateOf(editingJob?.industry.orEmpty()) }
    var employmentType by remember { mutableStateOf(editingJob?.employmentType ?: "FULL_TIME") }
    var workplaceType by remember { mutableStateOf(editingJob?.workplaceType ?: "Onsite") }
    var educationLevel by remember { mutableStateOf(editingJob?.educationLevel ?: "Any") }
    var experienceMonths by remember { mutableIntStateOf(editingJob?.experienceMonths ?: 24) }
    var skillsText by remember { mutableStateOf(editingJob?.skills?.joinToString(", ").orEmpty()) }
    var benefitsText by remember { mutableStateOf(editingJob?.benefits?.joinToString(", ").orEmpty()) }
    var salaryMin by remember { mutableStateOf(editingJob?.salaryMin?.toInt()?.toString().orEmpty()) }
    var salaryMax by remember { mutableStateOf(editingJob?.salaryMax?.toInt()?.toString().orEmpty()) }
    var salaryCurrency by remember { mutableStateOf(editingJob?.salaryCurrency ?: "TZS") }

    // ---------------------------------------------------------
    // CREATE COMPANY FORM STATE
    // ---------------------------------------------------------
    var newCompName by remember { mutableStateOf("") }
    var newCompIndustry by remember { mutableStateOf("") }
    var newCompUrl by remember { mutableStateOf("") }
    var newCompLogoUrl by remember { mutableStateOf("") }
    var newCompDescription by remember { mutableStateOf("") }
    var newCompStreet by remember { mutableStateOf("") }
    var newCompArea by remember { mutableStateOf("") }
    var newCompCity by remember { mutableStateOf("Dar es Salaam") }
    var newCompDistrict by remember { mutableStateOf("Kinondoni") }
    var newCompPostalCode by remember { mutableStateOf("14111") }
    var newCompCountry by remember { mutableStateOf("TZ") }
    var newCompFounded by remember { mutableStateOf("") }
    var newCompEmployees by remember { mutableStateOf("") }
    var creatingCompany by remember { mutableStateOf(false) }

    // Media Picker for Job Attachments
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val uriStr = uri.toString()
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "file_${System.currentTimeMillis()}"
            attachments.add(
                JobAttachmentItem(
                    url = uriStr,
                    thumbnail = uriStr,
                    name = fileName,
                    type = if (fileName.endsWith(".pdf", ignoreCase = true)) "pdf" else "image"
                )
            )
        }
    }

    // Logo Picker for Company
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newCompLogoUrl = uri.toString()
        }
    }

    var submittingJob by remember { mutableStateOf(false) }

    // ---------------------------------------------------------
    // ACTIONS
    // ---------------------------------------------------------
    fun handleChooseCompany(companyName: String) {
        if (companyName.isBlank()) {
            showMessage("Please select a company", isError = true)
            return
        }
        selectedCompany = companyName.trim()
        step = JobInputStep.JOB_FORM
        scope.launch {
            viewModel.saveUserWork(companyName)
        }
    }

    fun handleCreateCompanySubmit() {
        if (newCompName.isBlank()) { showMessage("Company name is required", isError = true); return }
        if (newCompIndustry.isBlank()) { showMessage("Industry is required", isError = true); return }
        if (newCompDescription.isBlank()) { showMessage("About description is required", isError = true); return }

        creatingCompany = true
        scope.launch {
            val payload = JSONObject().apply {
                put("name", newCompName.trim())
                put("industry", newCompIndustry.trim())
                put("url", newCompUrl.trim())
                put("logoUrl", newCompLogoUrl.trim())
                put("description", newCompDescription.trim())
                put("streetAddress", newCompStreet.trim())
                put("area", newCompArea.trim())
                put("locality", newCompCity.trim())
                put("district", newCompDistrict.trim())
                put("postalCode", newCompPostalCode.trim())
                put("country", newCompCountry)
                put("foundedYear", newCompFounded.trim())
                put("employeeCount", newCompEmployees.trim())
            }.toString()

            val (success, err) = viewModel.createCompany(payload)
            creatingCompany = false
            if (success) {
                selectedCompany = newCompName.trim()
                viewModel.saveUserWork(newCompName.trim())
                step = JobInputStep.JOB_FORM
                showMessage("Company \"$newCompName\" created successfully!")
            } else {
                showMessage(err ?: "Failed to create company", isError = true)
            }
        }
    }

    fun handleJobSubmit() {
        if (!isEditing && selectedCompany.isBlank()) { showMessage("Please select a company", isError = true); return }
        if (title.isBlank()) { showMessage("Job title is required", isError = true); return }
        if (selectedRole.isBlank()) { showMessage("Category / Role is required", isError = true); return }
        if (expiresAt.isBlank()) { showMessage("Expiry date is required", isError = true); return }
        if (applicationValue.isBlank()) { showMessage("Application target is required", isError = true); return }
        if (streetAddress.isBlank()) { showMessage("Street address is required", isError = true); return }
        if (city.isBlank()) { showMessage("City is required", isError = true); return }
        if (region.isBlank()) { showMessage("Region is required", isError = true); return }
        if (country.isBlank()) { showMessage("Country is required", isError = true); return }
        if (postcode.isBlank()) { showMessage("Postcode is required", isError = true); return }

        submittingJob = true
        scope.launch {
            val applyUrl = when (applicationType) {
                "email" -> if (applicationValue.startsWith("mailto:")) applicationValue else "mailto:$applicationValue"
                "whatsapp" -> "https://wa.me/${applicationValue.replace(Regex("[^0-9]"), "")}"
                "instructions" -> "https://jobsreport.online/contact-us"
                else -> applicationValue
            }

            val skillsArray = JSONArray()
            skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { skillsArray.put(it) }

            val benefitsArray = JSONArray()
            benefitsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { benefitsArray.put(it) }

            val imagesArray = JSONArray()
            attachments.forEach { att ->
                val imgObj = JSONObject().apply {
                    put("url", att.url)
                    put("thumbnail", att.thumbnail)
                    put("name", att.name)
                    put("type", att.type)
                }
                imagesArray.put(imgObj)
            }

            val payload = JSONObject().apply {
                if (isEditing) put("jobId", editingJob?.id)
                put("title", title.trim())
                put("company", selectedCompany.trim())
                put("role", selectedRole.trim())
                put("location", if (location.isNotBlank()) location.trim() else city.trim())
                put("url", applyUrl)
                put("salary", if (salary.isNotBlank()) salary.trim() else "Not Disclosed")
                put("expiresAt", expiresAt)
                put("description", description)
                put("is_active", 1)
                put("job_category", if (advancedCategory.isNotBlank()) advancedCategory.trim() else selectedRole.trim())
                put("industry", advancedIndustry.trim())
                put("employment_type", employmentType)
                put("workplace_type", workplaceType)
                put("education_level", educationLevel)
                put("experience_months", experienceMonths)
                put("skills", skillsArray)
                put("benefits", benefitsArray)
                if (salaryMin.isNotBlank()) put("salary_min", salaryMin.toDoubleOrNull())
                if (salaryMax.isNotBlank()) put("salary_max", salaryMax.toDoubleOrNull())
                put("salary_currency", salaryCurrency)
                put("street_address", streetAddress.trim())
                put("city", city.trim())
                put("region", region.trim())
                put("country", country.trim())
                put("postcode", postcode.trim())
                if (applicationType == "whatsapp") put("whatsapp_number", applicationValue.trim())
                if (applicationType == "instructions") put("application_instructions", applicationValue.trim())
                put("images", imagesArray)
            }.toString()

            val (success, err) = viewModel.submitUserJob(
                jsonPayload = payload,
                isEdit = isEditing,
                jobId = editingJob?.id
            )
            submittingJob = false

            if (success) {
                showMessage(if (isEditing) "Job updated successfully!" else "Job posted successfully!")
                delay(1200)
                onClose()
            } else {
                showMessage(err ?: "Failed to submit job. Please check your network.", isError = true)
            }
        }
    }

    // ---------------------------------------------------------
    // RENDER UI
    // ---------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step == JobInputStep.CREATE_COMPANY) {
                            step = JobInputStep.CHOOSE_COMPANY
                        } else if (!isEditing && step == JobInputStep.JOB_FORM && profileWork.isBlank()) {
                            step = JobInputStep.CHOOSE_COMPANY
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (step) {
                            JobInputStep.CHOOSE_COMPANY -> "Choose Company"
                            JobInputStep.CREATE_COMPANY -> "Create Company"
                            JobInputStep.JOB_FORM -> if (isEditing) "Edit Job" else "Post a Job"
                        },
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "JobsReport Professional Employer Portal",
                        color = TextGray500,
                        fontSize = 11.sp
                    )
                }

                if (step == JobInputStep.JOB_FORM && !isEditing) {
                    Text(
                        text = "Change",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { step = JobInputStep.CHOOSE_COMPANY }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // NOTIFICATION BANNER
            AnimatedVisibility(
                visible = messageText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (messageIsError) RedColor.copy(alpha = 0.15f) else EmeraldColor.copy(alpha = 0.15f))
                        .border(1.dp, if (messageIsError) RedColor.copy(alpha = 0.3f) else EmeraldColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (messageIsError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (messageIsError) RedColor else EmeraldColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = messageText.orEmpty(),
                            color = if (messageIsError) RedColor else EmeraldColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // CONTENT BODY
            when (step) {
                JobInputStep.CHOOSE_COMPANY -> {
                    ChooseCompanyStep(
                        profileWork = profileWork,
                        companies = companies,
                        loadingCatalog = loadingCatalog,
                        onSelectCompany = { handleChooseCompany(it) },
                        onCreateCompanyClick = { step = JobInputStep.CREATE_COMPANY }
                    )
                }

                JobInputStep.CREATE_COMPANY -> {
                    CreateCompanyStep(
                        name = newCompName,
                        onNameChange = { newCompName = it },
                        industry = newCompIndustry,
                        onIndustryChange = { newCompIndustry = it },
                        url = newCompUrl,
                        onUrlChange = { newCompUrl = it },
                        logoUrl = newCompLogoUrl,
                        onPickLogo = { logoPickerLauncher.launch("image/*") },
                        onRemoveLogo = { newCompLogoUrl = "" },
                        description = newCompDescription,
                        onDescriptionChange = { newCompDescription = it },
                        street = newCompStreet,
                        onStreetChange = { newCompStreet = it },
                        area = newCompArea,
                        onAreaChange = { newCompArea = it },
                        city = newCompCity,
                        onCityChange = { newCompCity = it },
                        district = newCompDistrict,
                        onDistrictChange = { newCompDistrict = it },
                        postcode = newCompPostalCode,
                        onPostcodeChange = { newCompPostalCode = it },
                        country = newCompCountry,
                        onCountryChange = { newCompCountry = it },
                        foundedYear = newCompFounded,
                        onFoundedYearChange = { newCompFounded = it },
                        employees = newCompEmployees,
                        onEmployeesChange = { newCompEmployees = it },
                        creating = creatingCompany,
                        onCancel = { step = JobInputStep.CHOOSE_COMPANY },
                        onSubmit = { handleCreateCompanySubmit() }
                    )
                }

                JobInputStep.JOB_FORM -> {
                    JobFormStep(
                        isEditing = isEditing,
                        selectedCompany = selectedCompany,
                        onChangeCompany = { step = JobInputStep.CHOOSE_COMPANY },
                        title = title,
                        onTitleChange = { title = it },
                        selectedRole = selectedRole,
                        onRoleChange = { selectedRole = it },
                        roleOptions = categories,
                        location = location,
                        onLocationChange = { location = it },
                        salary = salary,
                        onSalaryChange = { salary = it },
                        expiresAt = expiresAt,
                        onPickDate = {
                            val c = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    expiresAt = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            ).apply {
                                datePicker.minDate = System.currentTimeMillis() - 1000
                            }.show()
                        },
                        streetAddress = streetAddress,
                        onStreetAddressChange = { streetAddress = it },
                        city = city,
                        onCityChange = { city = it },
                        region = region,
                        onRegionChange = { region = it },
                        country = country,
                        onCountryChange = { country = it },
                        postcode = postcode,
                        onPostcodeChange = { postcode = it },
                        applicationType = applicationType,
                        onApplicationTypeChange = { applicationType = it },
                        applicationValue = applicationValue,
                        onApplicationValueChange = { applicationValue = it },
                        attachments = attachments,
                        onAddAttachment = { filePickerLauncher.launch("*/*") },
                        onRemoveAttachment = { attachments.remove(it) },
                        description = description,
                        onDescriptionChange = { description = it },
                        descEditMode = descEditMode,
                        onToggleDescMode = { descEditMode = if (descEditMode == "visual") "html" else "visual" },
                        showAdvanced = showAdvanced,
                        onToggleAdvanced = { showAdvanced = !showAdvanced },
                        advancedCategory = advancedCategory,
                        onAdvancedCategoryChange = { advancedCategory = it },
                        advancedIndustry = advancedIndustry,
                        onAdvancedIndustryChange = { advancedIndustry = it },
                        employmentType = employmentType,
                        onEmploymentTypeChange = { employmentType = it },
                        workplaceType = workplaceType,
                        onWorkplaceTypeChange = { workplaceType = it },
                        educationLevel = educationLevel,
                        onEducationLevelChange = { educationLevel = it },
                        experienceMonths = experienceMonths,
                        onExperienceMonthsChange = { experienceMonths = it },
                        skillsText = skillsText,
                        onSkillsTextChange = { skillsText = it },
                        benefitsText = benefitsText,
                        onBenefitsTextChange = { benefitsText = it },
                        salaryMin = salaryMin,
                        onSalaryMinChange = { salaryMin = it },
                        salaryMax = salaryMax,
                        onSalaryMaxChange = { salaryMax = it },
                        salaryCurrency = salaryCurrency,
                        onSalaryCurrencyChange = { salaryCurrency = it },
                        submitting = submittingJob,
                        onCancel = onClose,
                        onSubmit = { handleJobSubmit() }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 1 COMPONENT: CHOOSE COMPANY
// -------------------------------------------------------------
@Composable
private fun ChooseCompanyStep(
    profileWork: String,
    companies: List<Company>,
    loadingCatalog: Boolean,
    onSelectCompany: (String) -> Unit,
    onCreateCompanyClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var dropdownOpen by remember { mutableStateOf(false) }

    val filteredCompanies = remember(companies, searchQuery) {
        if (searchQuery.isBlank()) companies
        else companies.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "Choose Your Company",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Select the company you're hiring for, or create a new one.",
                    color = TextGray400,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Saved company from profile (if exists)
        if (profileWork.isNotBlank()) {
            item {
                Column {
                    Text(
                        text = "YOUR COMPANY (FROM PROFILE)",
                        color = TextGray500,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryBlue.copy(alpha = 0.15f), BrandPurple.copy(alpha = 0.15f))
                                )
                            )
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .clickable { onSelectCompany(profileWork) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(PrimaryBlue, BrandPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profileWork,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Works at $profileWork",
                                color = TextGray400,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Searchable combobox
        item {
            Column {
                Text(
                    text = if (profileWork.isNotBlank()) "OR CHOOSE ANOTHER COMPANY" else "CHOOSE A COMPANY",
                    color = TextGray500,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite04)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextGray500, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                dropdownOpen = true
                            },
                            placeholder = { Text("Search companies...", color = TextGray600, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { dropdownOpen = !dropdownOpen }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray400)
                        }
                    }
                }

                // Dropdown of matches
                if (dropdownOpen || searchQuery.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DialogCardBg)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                    ) {
                        if (loadingCatalog) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(20.dp))
                            }
                        } else if (filteredCompanies.isEmpty()) {
                            Text(
                                text = "No company found",
                                color = TextGray500,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            filteredCompanies.take(15).forEach { comp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = comp.name
                                            dropdownOpen = false
                                            onSelectCompany(comp.name)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Apartment, contentDescription = null, tint = TextGray400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(comp.name, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        if (!comp.industry.isNullOrBlank()) {
                                            Text(comp.industry, color = TextGray500, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Company Prompt
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "COMPANY NOT LISTED?",
                    color = TextGray500,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite02)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .clickable(onClick = onCreateCompanyClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite04)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Create New Company", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Add your company to the JobsReport catalog", color = TextGray500, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextGray500, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2 COMPONENT: CREATE COMPANY
// -------------------------------------------------------------
@Composable
private fun CreateCompanyStep(
    name: String, onNameChange: (String) -> Unit,
    industry: String, onIndustryChange: (String) -> Unit,
    url: String, onUrlChange: (String) -> Unit,
    logoUrl: String, onPickLogo: () -> Unit, onRemoveLogo: () -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    street: String, onStreetChange: (String) -> Unit,
    area: String, onAreaChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    district: String, onDistrictChange: (String) -> Unit,
    postcode: String, onPostcodeChange: (String) -> Unit,
    country: String, onCountryChange: (String) -> Unit,
    foundedYear: String, onFoundedYearChange: (String) -> Unit,
    employees: String, onEmployeesChange: (String) -> Unit,
    creating: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Create Company", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("Add your company to the catalog, then post your job.", color = TextGray400, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Basic Information
        item {
            FormSectionHeader(title = "BASIC INFORMATION", icon = Icons.Default.Business)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextInput(label = "Company Name *", value = name, onValueChange = onNameChange, placeholder = "e.g. Vodacom Tanzania")
                FormTextInput(label = "Industry *", value = industry, onValueChange = onIndustryChange, placeholder = "e.g. Telecommunications")
                FormTextInput(label = "Website", value = url, onValueChange = onUrlChange, placeholder = "https://company.com")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Founded Year", value = foundedYear, onValueChange = onFoundedYearChange, placeholder = "e.g. 2005")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Employees", value = employees, onValueChange = onEmployeesChange, placeholder = "e.g. 500-1000")
                    }
                }
                FormTextInput(label = "About Company *", value = description, onValueChange = onDescriptionChange, placeholder = "Short description of the company...", maxLines = 4, singleLine = false)
            }
        }

        // Company Logo
        item {
            FormSectionHeader(title = "COMPANY LOGO", icon = Icons.Default.Image)
            if (logoUrl.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite04)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Logo Uploaded", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Remove logo",
                            color = RedColor,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable(onClick = onRemoveLogo)
                                .padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite02)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .clickable(onClick = onPickLogo)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Company Logo (optional)", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Address
        item {
            FormSectionHeader(title = "ADDRESS", icon = Icons.Default.LocationOn)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextInput(label = "Street Address", value = street, onValueChange = onStreetChange, placeholder = "e.g. Plot 1520, Mwai Kibaki Road")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Area", value = area, onValueChange = onAreaChange, placeholder = "e.g. Mikocheni")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "City / Locality", value = city, onValueChange = onCityChange, placeholder = "e.g. Dar es Salaam")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "District", value = district, onValueChange = onDistrictChange, placeholder = "e.g. Kinondoni")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Postal Code", value = postcode, onValueChange = onPostcodeChange, placeholder = "e.g. 14111")
                    }
                }
                FormTextInput(label = "Country", value = country, onValueChange = onCountryChange, placeholder = "e.g. Tanzania")
            }
        }

        // Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceWhite04)
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(PrimaryBlue, BrandPurple)))
                        .clickable(enabled = !creating, onClick = onSubmit),
                    contentAlignment = Alignment.Center
                ) {
                    if (creating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create & Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 3 COMPONENT: JOB FORM
// -------------------------------------------------------------
@Composable
private fun JobFormStep(
    isEditing: Boolean,
    selectedCompany: String,
    onChangeCompany: () -> Unit,
    title: String, onTitleChange: (String) -> Unit,
    selectedRole: String, onRoleChange: (String) -> Unit,
    roleOptions: List<String>,
    location: String, onLocationChange: (String) -> Unit,
    salary: String, onSalaryChange: (String) -> Unit,
    expiresAt: String, onPickDate: () -> Unit,
    streetAddress: String, onStreetAddressChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    region: String, onRegionChange: (String) -> Unit,
    country: String, onCountryChange: (String) -> Unit,
    postcode: String, onPostcodeChange: (String) -> Unit,
    applicationType: String, onApplicationTypeChange: (String) -> Unit,
    applicationValue: String, onApplicationValueChange: (String) -> Unit,
    attachments: List<JobAttachmentItem>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (JobAttachmentItem) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    descEditMode: String, onToggleDescMode: () -> Unit,
    showAdvanced: Boolean, onToggleAdvanced: () -> Unit,
    advancedCategory: String, onAdvancedCategoryChange: (String) -> Unit,
    advancedIndustry: String, onAdvancedIndustryChange: (String) -> Unit,
    employmentType: String, onEmploymentTypeChange: (String) -> Unit,
    workplaceType: String, onWorkplaceTypeChange: (String) -> Unit,
    educationLevel: String, onEducationLevelChange: (String) -> Unit,
    experienceMonths: Int, onExperienceMonthsChange: (Int) -> Unit,
    skillsText: String, onSkillsTextChange: (String) -> Unit,
    benefitsText: String, onBenefitsTextChange: (String) -> Unit,
    salaryMin: String, onSalaryMinChange: (String) -> Unit,
    salaryMax: String, onSalaryMaxChange: (String) -> Unit,
    salaryCurrency: String, onSalaryCurrencyChange: (String) -> Unit,
    submitting: Boolean,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    var roleDropdownOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberColor.copy(alpha = 0.15f))
                            .border(1.dp, AmberColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("EDITING", color = AmberColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = if (isEditing) "Edit Job" else "Post a Job",
                    color = TextWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isEditing) "Update your job listing details." else "Hiring on behalf of $selectedCompany",
                    color = TextGray400,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Section: Company
        item {
            FormSectionHeader(title = "COMPANY YOU'RE HIRING FOR", icon = Icons.Default.Business)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite04)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(PrimaryBlue, BrandPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(selectedCompany, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (!isEditing) {
                    Text(
                        text = "Change",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onChangeCompany)
                    )
                }
            }
        }

        // Section: Job Basics
        item {
            FormSectionHeader(title = "JOB BASICS", icon = Icons.Default.Work)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextInput(label = "Job Title *", value = title, onValueChange = onTitleChange, placeholder = "e.g. Senior Frontend Engineer")

                // Category dropdown
                Column {
                    Text("Category / Role *", color = TextGray400, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceWhite04)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(14.dp))
                            .clickable { roleDropdownOpen = true }
                            .padding(horizontal = 14.dp, vertical = 13.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedRole.ifBlank { "Select Category..." }, color = TextWhite, fontSize = 13.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray400, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = roleDropdownOpen,
                            onDismissRequest = { roleDropdownOpen = false },
                            modifier = Modifier.background(DialogCardBg)
                        ) {
                            roleOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, color = TextWhite, fontSize = 13.sp) },
                                    onClick = {
                                        onRoleChange(opt)
                                        roleDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Location Label", value = location, onValueChange = onLocationChange, placeholder = "e.g. Dar es Salaam")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Salary (Optional)", value = salary, onValueChange = onSalaryChange, placeholder = "e.g. TZS 1,500,000")
                    }
                }

                // Expires At Date Picker
                Column {
                    Text("Expires On *", color = TextGray400, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceWhite04)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(14.dp))
                            .clickable(onClick = onPickDate)
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(expiresAt, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Section: Locations
        item {
            FormSectionHeader(title = "LOCATIONS", icon = Icons.Default.LocationOn)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextInput(label = "Street Address *", value = streetAddress, onValueChange = onStreetAddressChange, placeholder = "e.g. Plot 1520, Mwai Kibaki Road")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "City *", value = city, onValueChange = onCityChange, placeholder = "e.g. Dar es Salaam")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Region *", value = region, onValueChange = onRegionChange, placeholder = "e.g. Kinondoni")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Country *", value = country, onValueChange = onCountryChange, placeholder = "e.g. Tanzania")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextInput(label = "Postcode *", value = postcode, onValueChange = onPostcodeChange, placeholder = "e.g. 14111")
                    }
                }
            }
        }

        // Section: How to Apply
        item {
            FormSectionHeader(title = "HOW TO APPLY", icon = Icons.Default.OpenInNew)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceWhite04)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("url" to "URL", "email" to "Email", "whatsapp" to "WhatsApp", "instructions" to "Instructions").forEach { (typeKey, label) ->
                        val isSelected = applicationType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                .clickable { onApplicationTypeChange(typeKey) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextGray400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                when (applicationType) {
                    "url" -> FormTextInput(
                        label = "Application URL",
                        value = applicationValue,
                        onValueChange = onApplicationValueChange,
                        placeholder = "https://company.com/careers/apply"
                    )
                    "email" -> FormTextInput(
                        label = "Application Email",
                        value = applicationValue,
                        onValueChange = onApplicationValueChange,
                        placeholder = "careers@company.com"
                    )
                    "whatsapp" -> FormTextInput(
                        label = "WhatsApp Number",
                        value = applicationValue,
                        onValueChange = onApplicationValueChange,
                        placeholder = "+255 712 345 678"
                    )
                    "instructions" -> FormTextInput(
                        label = "Application Instructions",
                        value = applicationValue,
                        onValueChange = onApplicationValueChange,
                        placeholder = "e.g. Submit your CV and portfolio to...",
                        maxLines = 4,
                        singleLine = false
                    )
                }
            }
        }

        // Section: Attachments
        item {
            FormSectionHeader(title = "ATTACHMENTS", icon = Icons.Default.AttachFile)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceWhite02)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                        .clickable(onClick = onAddAttachment)
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (attachments.isEmpty()) "Upload files / images (optional)" else "${attachments.size} file(s) attached — Add more",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (attachments.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(attachments) { att ->
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceWhite04)
                                    .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                            ) {
                                if (att.type == "image") {
                                    AsyncImage(
                                        model = att.thumbnail,
                                        contentDescription = att.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                                        Text(att.name.take(8), color = TextGray400, fontSize = 8.sp, maxLines = 1)
                                    }
                                }

                                IconButton(
                                    onClick = { onRemoveAttachment(att) },
                                    modifier = Modifier
                                        .size(22.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedColor, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Job Description
        item {
            FormSectionHeader(title = "JOB DESCRIPTION", icon = Icons.Default.Description)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${description.length} characters", color = TextGray500, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceWhite04)
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "VISUAL",
                            color = if (descEditMode == "visual") Color.White else TextGray400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (descEditMode == "visual") PrimaryBlue else Color.Transparent)
                                .clickable { onToggleDescMode() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "HTML",
                            color = if (descEditMode == "html") Color.White else TextGray400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (descEditMode == "html") PrimaryBlue else Color.Transparent)
                                .clickable { onToggleDescMode() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Formatting Quick Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceWhite04)
                        .border(1.dp, BorderWhite10, RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onDescriptionChange("$description<b>Bold text</b>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDescriptionChange("$description<i>Italic text</i>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDescriptionChange("$description<u>Underlined</u>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDescriptionChange("$description\n<h3>Heading</h3>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("H3", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(
                        onClick = { onDescriptionChange("$description\n<ul><li>Point 1</li><li>Point 2</li></ul>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDescriptionChange("$description\n<ol><li>Step 1</li><li>Step 2</li></ol>") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Number List", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                }

                FormTextInput(
                    label = "",
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = "Describe the role, responsibilities, requirements...",
                    maxLines = 14,
                    singleLine = false
                )
            }
        }

        // Section: Advanced Options (Collapsible)
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onToggleAdvanced)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADVANCED OPTIONS", color = TextGray400, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray400, modifier = Modifier.size(18.dp))
                }

                if (showAdvanced) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Category", value = advancedCategory, onValueChange = onAdvancedCategoryChange, placeholder = "e.g. IT")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Employment Type", value = employmentType, onValueChange = onEmploymentTypeChange, placeholder = "FULL_TIME")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Workplace", value = workplaceType, onValueChange = onWorkplaceTypeChange, placeholder = "Onsite")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Education", value = educationLevel, onValueChange = onEducationLevelChange, placeholder = "Degree / Diploma")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Exp (Months)", value = experienceMonths.toString(), onValueChange = { onExperienceMonthsChange(it.toIntOrNull() ?: 0) }, placeholder = "24")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Industry", value = advancedIndustry, onValueChange = onAdvancedIndustryChange, placeholder = "Telecom")
                            }
                        }

                        FormTextInput(label = "Skills (comma-separated)", value = skillsText, onValueChange = onSkillsTextChange, placeholder = "React, Kotlin, Jetpack Compose")
                        FormTextInput(label = "Benefits (comma-separated)", value = benefitsText, onValueChange = onBenefitsTextChange, placeholder = "Health Insurance, Annual Bonus")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Min Salary", value = salaryMin, onValueChange = onSalaryMinChange, placeholder = "1000000")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Max Salary", value = salaryMax, onValueChange = onSalaryMaxChange, placeholder = "3000000")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextInput(label = "Currency", value = salaryCurrency, onValueChange = onSalaryCurrencyChange, placeholder = "TZS")
                            }
                        }
                    }
                }
            }
        }

        // Section: Bottom Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceWhite04)
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(PrimaryBlue, BrandPurple)))
                        .clickable(enabled = !submitting, onClick = onSubmit),
                    contentAlignment = Alignment.Center
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditing) "Update Job" else "Publish Job", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// REUSABLE FORM COMPONENTS
// -------------------------------------------------------------
@Composable
private fun FormSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextGray400,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun FormTextInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = TextGray400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray600, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderWhite10,
                focusedContainerColor = SurfaceWhite04,
                unfocusedContainerColor = SurfaceWhite04,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines
        )
    }
}
