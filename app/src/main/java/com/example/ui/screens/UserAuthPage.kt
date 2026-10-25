package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.state.AppViewModel
import com.example.state.NavRoute
import kotlinx.coroutines.launch

// Exact brand colors from UserAuth.tsx
private val BrandDarkBg = Color(0xFF050B18)
private val BrandBlue = Color(0xFF1877F2)
private val BrandBlueHover = Color(0xFF166FE5)
private val BrandPurpleMid = Color(0xFF7B4DFF)
private val BrandPurpleEnd = Color(0xFFA033FF)
private val Gray400 = Color(0xFF9CA3AF)
private val Gray500 = Color(0xFF6B7280)
private val InputBg = Color(0x08FFFFFF)
private val BorderWhite10 = Color(0x1AFFFFFF)
private val ButtonSecondaryBg = Color(0x0DFFFFFF)
private val EmeraldSuccess = Color(0xFF34D399)

enum class AuthView {
    LOGIN,
    REGISTER,
    FORGOT
}

// ============================================================
// BRAND HEADER (Exact parity with UserAuth.tsx BrandHeader)
// ============================================================
@Composable
fun BrandHeader(size: Dp = 44.dp) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://media.jobsreport.online/file_0000000084b47243aec7e8cf3cbeb6bd.png",
            contentDescription = "JobsReport Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "JobsReport",
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.72f).sp,
            letterSpacing = (-0.5).sp,
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(BrandBlue, BrandPurpleMid, BrandPurpleEnd)
                )
            )
        )
    }
}

// ============================================================
// USER AUTH ROOT PAGE (Exact parity with UserAuthPage.tsx)
// ============================================================
@Composable
fun UserAuthPage(
    viewModel: AppViewModel,
    initialView: AuthView = AuthView.LOGIN,
    onClose: (() -> Unit)? = null
) {
    var view by remember { mutableStateOf(initialView) }

    // Intercept hardware / system back navigation
    BackHandler {
        if (view != AuthView.LOGIN) {
            view = AuthView.LOGIN
        } else if (onClose != null) {
            onClose()
        } else {
            viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandDarkBg)
    ) {
        when (view) {
            AuthView.REGISTER -> {
                Register(
                    viewModel = viewModel,
                    onBackToLogin = { view = AuthView.LOGIN },
                    onClose = onClose
                )
            }
            AuthView.FORGOT -> {
                ForgotPassword(
                    viewModel = viewModel,
                    onBackToLogin = { view = AuthView.LOGIN }
                )
            }
            AuthView.LOGIN -> {
                Login(
                    viewModel = viewModel,
                    onNavigateToRegister = { view = AuthView.REGISTER },
                    onNavigateToForgotPassword = { view = AuthView.FORGOT },
                    onClose = onClose
                )
            }
        }
    }
}

// ============================================================
// LOGIN COMPONENT (Exact parity with UserAuth.tsx Login)
// ============================================================
@Composable
fun Login(
    viewModel: AppViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Optional Close button
        if (onClose != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                BrandHeader(size = 44.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Log in to share job stories, save opportunities, and connect with professionals.",
                    color = Gray400,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Error Banner
                AnimatedVisibility(
                    visible = error.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Email Input
                AuthInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Input
                AuthInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword },
                    onDone = {
                        focusManager.clearFocus()
                    },
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Options Row: Remember Me & Forgot Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!loading) rememberMe = !rememberMe
                        }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            enabled = !loading,
                            colors = CheckboxDefaults.colors(
                                checkedColor = BrandBlue,
                                uncheckedColor = Gray500,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remember me",
                            color = Gray400,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Forgot password?",
                        color = BrandBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            if (!loading) onNavigateToForgotPassword()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (loading) BrandBlue.copy(alpha = 0.6f) else BrandBlue)
                        .clickable(enabled = !loading) {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Please enter both email and password."
                                return@clickable
                            }
                            focusManager.clearFocus()
                            error = ""
                            loading = true
                            scope.launch {
                                val res = viewModel.login(email.trim(), password, rememberMe)
                                loading = false
                                if (res.success) {
                                    Toast.makeText(context, res.message ?: "Logged in successfully!", Toast.LENGTH_SHORT).show()
                                    if (onClose != null) {
                                        onClose()
                                    } else {
                                        viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED)
                                    }
                                } else {
                                    error = res.message ?: "Login failed. Please check credentials."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Log In",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer: Sign Up Switch
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Gray400,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign Up",
                        color = BrandBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (!loading) onNavigateToRegister()
                        }
                    )
                }
            }
        }
    }
}

// ============================================================
// REGISTER COMPONENT (Exact parity with UserAuth.tsx Register)
// ============================================================
@Composable
fun Register(
    viewModel: AppViewModel,
    onBackToLogin: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(viewModel.selectedCountry) }
    var agreeTerms by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        if (onClose != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandHeader(size = 44.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Join JobsReport to share career stories, discover opportunities, and connect with employers.",
                    color = Gray400,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = error.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFCA5A5),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Full Name
                AuthInputField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Full Name",
                    leadingIcon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Professional Headline / Role
                AuthInputField(
                    value = headline,
                    onValueChange = { headline = it },
                    placeholder = "Professional Role (e.g. Software Engineer)",
                    leadingIcon = Icons.Default.Work,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email
                AuthInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password
                AuthInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password (minimum 6 characters)",
                    leadingIcon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword },
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location / Country
                AuthInputField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "Location / City (e.g. Dar es Salaam, Tanzania)",
                    leadingIcon = Icons.Default.LocationOn,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    onDone = { focusManager.clearFocus() },
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Terms agreement
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!loading) agreeTerms = !agreeTerms
                        }
                ) {
                    Checkbox(
                        checked = agreeTerms,
                        onCheckedChange = { agreeTerms = it },
                        enabled = !loading,
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrandBlue,
                            uncheckedColor = Gray500,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I agree to JobsReport Terms of Service & Privacy Policy",
                        color = Gray400,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Submit Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (loading) BrandBlue.copy(alpha = 0.6f) else BrandBlue)
                        .clickable(enabled = !loading) {
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                error = "Please complete all required fields."
                                return@clickable
                            }
                            if (password.length < 6) {
                                error = "Password must be at least 6 characters."
                                return@clickable
                            }
                            if (!agreeTerms) {
                                error = "Please accept the Terms of Service to proceed."
                                return@clickable
                            }
                            focusManager.clearFocus()
                            error = ""
                            loading = true
                            scope.launch {
                                val res = viewModel.register(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password,
                                    headline = headline.trim().ifBlank { null },
                                    location = location.trim().ifBlank { null }
                                )
                                loading = false
                                if (res.success) {
                                    Toast.makeText(context, res.message ?: "Account created!", Toast.LENGTH_SHORT).show()
                                    if (onClose != null) {
                                        onClose()
                                    } else {
                                        viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED)
                                    }
                                } else {
                                    error = res.message ?: "Registration failed."
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Gray400,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Log In",
                        color = BrandBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            if (!loading) onBackToLogin()
                        }
                    )
                }
            }
        }
    }
}

// ============================================================
// FORGOT PASSWORD COMPONENT (Exact parity with UserAuth.tsx ForgotPassword)
// ============================================================
@Composable
fun ForgotPassword(
    viewModel: AppViewModel,
    onBackToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var isSent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader(size = 40.dp)

            Spacer(modifier = Modifier.height(24.dp))

            if (isSent) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(54.dp)
                    )

                    Text(
                        text = "Reset Link Sent!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "If an account with $email exists, a reset link has been sent. Check your inbox and spam folder.",
                        color = Gray400,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ButtonSecondaryBg)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                            .clickable { onBackToLogin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Back to Login",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    text = "Find Your Account",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your email to search for your account.",
                    color = Gray400,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthInputField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    onDone = {
                        focusManager.clearFocus()
                        if (email.isNotBlank()) {
                            loading = true
                            scope.launch {
                                viewModel.forgotPassword(email.trim())
                                loading = false
                                isSent = true
                            }
                        }
                    },
                    enabled = !loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ButtonSecondaryBg)
                            .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                            .clickable(enabled = !loading) { onBackToLogin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Submit Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (loading) BrandBlue.copy(alpha = 0.6f) else BrandBlue)
                            .clickable(enabled = !loading) {
                                if (email.trim().isNotEmpty()) {
                                    focusManager.clearFocus()
                                    loading = true
                                    scope.launch {
                                        viewModel.forgotPassword(email.trim())
                                        loading = false
                                        isSent = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Send Reset Link",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// REUSABLE AUTH INPUT FIELD (Exact styling from UserAuth.tsx)
// ============================================================
@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InputBg)
            .border(
                width = 1.dp,
                color = if (isFocused) BrandBlue else BorderWhite10,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) BrandBlue else Gray500,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Gray500,
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default
                    ),
                    cursorBrush = SolidColor(BrandBlue),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onDone?.invoke() }
                    ),
                    visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                )
            }

            if (isPassword && onTogglePassword != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPassword) "Hide password" else "Show password",
                    tint = if (showPassword) BrandBlue else Gray500,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onTogglePassword() }
                )
            }
        }
    }
}
