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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.theme.BorderWhite5
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple

@Composable
fun DisclaimerScreen(viewModel: AppViewModel) {
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
                    text = "DISCLAIMER",
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
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Legal",
                            tint = AmberYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LEGAL INFORMATION",
                            color = AmberYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Disclaimer",
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
                }
            }
        }

        // --- 3. General Disclaimer Card ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AmberYellow,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "General Disclaimer",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "The information provided on JobsReport.online is for general informational and educational purposes only. While we strive to keep the information accurate and up to date, we make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability, or availability of the information contained on the website.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        // --- 4. Job Listings & Employment Information ---
        item {
            DisclaimerSection(
                accentColor = PrimaryBlue,
                title = "Job Listings & Employment Information"
            ) {
                Text(
                    text = "JobsReport.online publishes job vacancies, career opportunities, and employment-related information sourced from various channels including employer websites, recruitment portals, government announcements, and public notices. We do not guarantee that any job listing will still be available at the time of application, as employers may withdraw or modify vacancies without notice.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Users are strongly advised to verify all job details, requirements, deadlines, and application procedures directly with the respective employers or recruiting organizations before applying.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 5. No Employment or Agency Relationship ---
        item {
            DisclaimerSection(
                accentColor = VioletPurple,
                title = "No Employment or Agency Relationship"
            ) {
                Text(
                    text = "JobsReport.online is an independent information platform. We are not an employment agency, recruitment firm, or hiring organization. We do not employ, recommend, or endorse any job seeker, employer, or organization listed on our platform. Any communication, application, or interaction between users and employers is solely between those parties.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 6. Third-Party Links ---
        item {
            DisclaimerSection(
                accentColor = EmeraldGreen,
                title = "Third-Party Links & External Websites"
            ) {
                Text(
                    text = "Our website may contain links to external websites, employer career portals, application platforms, and third-party services. These links are provided for convenience and informational purposes only. We have no control over the content, availability, security, or privacy practices of external sites.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The inclusion of any link does not imply endorsement, recommendation, or approval by JobsReport.online. Users access external links at their own risk.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 7. No Guarantee of Results ---
        item {
            DisclaimerSection(
                accentColor = Color(0xFFF43F5E), // Rose red
                title = "No Guarantee of Results"
            ) {
                Text(
                    text = "JobsReport.online does not guarantee that use of our website will result in employment, interviews, job offers, scholarships, admissions, or any other outcomes. Application outcomes depend entirely on the hiring decisions of respective employers and organizations.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 8. Financial Disclaimer ---
        item {
            DisclaimerSection(
                accentColor = AmberYellow,
                title = "Financial & Payment Disclaimer"
            ) {
                Text(
                    text = "JobsReport.online does not charge job seekers for accessing job listings or career information. We do not request, collect, or process payments related to job applications. Users should never pay money to any person or organization claiming to offer employment through our platform.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If you encounter any request for payment in connection with a job listing found on JobsReport.online, please report it immediately.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 9. Limitation of Liability ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AmberYellow.copy(alpha = 0.05f))
                    .border(1.dp, AmberYellow.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = AmberYellow,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Limitation of Liability",
                            color = AmberYellow,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Under no circumstances shall JobsReport.online, its owners, operators, contributors, or affiliates be liable for any direct, indirect, incidental, consequential, special, or exemplary damages arising from or in connection with the use of this website or reliance on any information provided.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This includes, but is not limited to, damages for loss of opportunities, loss of income, emotional distress, or any other losses resulting from:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val bulletPoints = listOf(
                            "Use or inability to use the website",
                            "Reliance on information published on the platform",
                            "Errors, omissions, or inaccuracies in job listings",
                            "Links to third-party websites or services",
                            "Actions taken based on information found on this website"
                        )

                        bulletPoints.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    color = AmberYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = point,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 10. Changes to Disclaimer ---
        item {
            DisclaimerSection(
                accentColor = Color(0xFF06B6D4), // Cyan
                title = "Changes to This Disclaimer"
            ) {
                Text(
                    text = "We reserve the right to update or modify this Disclaimer at any time without prior notice. Changes will be effective immediately upon posting. Continued use of the website after modifications constitutes acceptance of the updated Disclaimer.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        // --- 11. Contact Us ---
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
                        imageVector = Icons.Default.Description,
                        contentDescription = "Questions",
                        tint = LightBlue,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Questions About This Disclaimer?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "If you have questions, concerns, or require clarification regarding this Disclaimer, please contact us.",
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
private fun DisclaimerSection(
    accentColor: Color,
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
                        .width(4.dp)
                        .height(20.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
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
