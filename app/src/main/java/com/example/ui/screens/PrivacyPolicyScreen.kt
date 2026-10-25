package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.AppViewModel
import com.example.state.NavRoute
import com.example.ui.theme.AmberYellow
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgDeep
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple

@Composable
fun PrivacyPolicyScreen(viewModel: AppViewModel) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Breadcrumbs ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "HOME",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(NavRoute.INTELLIGENCE_FEED) }
                )
                Text(text = "/", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = "PRIVACY POLICY",
                    color = LightBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. Hero Section ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Privacy",
                            tint = LightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DATA PROTECTION & PRIVACY",
                            color = LightBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Privacy Policy",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Last Updated: June 14, 2026",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "JobsReport.online (\"we\", \"our\", or \"us\") values your privacy and is committed to protecting your personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard information when you access or use our website, services, job listings, notifications, and related features.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // --- 3. Section 1: Information We Collect ---
        item {
            PolicySection(
                number = "1",
                title = "Information We Collect"
            ) {
                Text(
                    text = "We may collect information directly from users, automatically through website usage, and from third-party service providers.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val bullets = listOf(
                    "Name and email address when contacting us.",
                    "Browser type, operating system, and device information.",
                    "IP address and approximate geographic location.",
                    "Website usage statistics and analytics data.",
                    "Notification subscription preferences.",
                    "Information submitted through forms and feedback channels."
                )
                bullets.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "• ", color = LightBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = item, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        // --- 4. Section 2: How We Use Your Information ---
        item {
            PolicySection(
                number = "2",
                title = "How We Use Your Information"
            ) {
                Text(
                    text = "We use collected information for legitimate business purposes, including:",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                val bullets = listOf(
                    "Providing job listings and career opportunities.",
                    "Improving website functionality and user experience.",
                    "Sending job alerts and platform notifications.",
                    "Monitoring website performance and security.",
                    "Preventing abuse, fraud, and unauthorized access.",
                    "Complying with legal obligations."
                )
                bullets.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "• ", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = item, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        // --- 5. Section 3: Cookies and Tracking Technologies ---
        item {
            PolicySection(
                number = "3",
                title = "Cookies and Tracking Technologies"
            ) {
                Text(
                    text = "JobsReport.online may use cookies, local storage, analytics tools, and similar technologies to improve performance, remember user preferences, analyze traffic patterns, and personalize content. Users can manage cookie preferences through browser settings.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 6. Section 4: Google AdSense and Advertising ---
        item {
            PolicySection(
                number = "4",
                title = "Google AdSense and Advertising"
            ) {
                Text(
                    text = "We may display advertisements through Google AdSense and other advertising networks. These providers may use cookies and similar technologies to deliver relevant advertisements based on browsing behavior and interests. Third-party advertising partners may collect information according to their own privacy policies.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 7. Section 5: Analytics Services ---
        item {
            PolicySection(
                number = "5",
                title = "Analytics Services"
            ) {
                Text(
                    text = "We may use analytics tools, including Google Analytics, to understand visitor behavior, monitor website performance, and improve our services. Analytics providers may collect anonymized information about interactions with our website.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 8. Section 6: Push Notifications ---
        item {
            PolicySection(
                number = "6",
                title = "Push Notifications"
            ) {
                Text(
                    text = "If you subscribe to push notifications, we may send updates regarding job opportunities, career news, platform announcements, and related information. You may unsubscribe at any time through browser or device notification settings.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 9. Section 7: Third-Party Links ---
        item {
            PolicySection(
                number = "7",
                title = "Third-Party Links"
            ) {
                Text(
                    text = "Job listings may contain links to external employer websites, application portals, and third-party services. We do not control and are not responsible for the privacy practices or content of those websites.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 10. Section 8: Data Security ---
        item {
            PolicySection(
                number = "8",
                title = "Data Security"
            ) {
                Text(
                    text = "We implement reasonable technical and organizational measures to protect information from unauthorized access, alteration, disclosure, or destruction. However, no online platform can guarantee absolute security.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 11. Section 9: Data Retention ---
        item {
            PolicySection(
                number = "9",
                title = "Data Retention"
            ) {
                Text(
                    text = "Information is retained only for as long as necessary to fulfill the purposes described in this policy, comply with legal obligations, resolve disputes, and enforce agreements.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 12. Section 10: User Rights ---
        item {
            PolicySection(
                number = "10",
                title = "User Rights"
            ) {
                Text(
                    text = "Depending on applicable laws, users may have rights to request access to, correction of, or deletion of personal information. Requests may be submitted through our contact channels.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 13. Section 11: Children's Privacy ---
        item {
            PolicySection(
                number = "11",
                title = "Children's Privacy"
            ) {
                Text(
                    text = "JobsReport.online is intended for general audiences seeking employment information and is not directed toward children under the age of 13. We do not knowingly collect personal information from children.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 14. Section 12: Changes to This Privacy Policy ---
        item {
            PolicySection(
                number = "12",
                title = "Changes to This Privacy Policy"
            ) {
                Text(
                    text = "We may update this Privacy Policy from time to time. Updates will be posted on this page together with the revised effective date. Continued use of the website after updates constitutes acceptance of the revised policy.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 15. Section 13: Contact Us ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Contact",
                        tint = LightBlue,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "13. Contact Us",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "If you have questions regarding this Privacy Policy or your personal information, please contact us at jjovinatha@gmail.com.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:jjovinatha@gmail.com")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTACT US",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    number: String,
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        color = LightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
