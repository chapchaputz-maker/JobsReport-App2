package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgCard
import com.example.ui.theme.BorderWhite10
import com.example.ui.theme.LightBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletPurple
import kotlin.math.abs

@Composable
fun JobsPagination(
    currentPage: Int,
    totalPages: Int,
    totalJobs: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalJobs == 0) return

    if (totalPages <= 1) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Showing page 1 of 1 • $totalJobs total jobs available",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. "See More Jobs" Button (if currentPage < totalPages)
        if (currentPage < totalPages) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.25f),
                                    VioletPurple.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { onPageChange(currentPage + 1) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SEE MORE JOBS",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "See More",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "Showing page $currentPage of $totalPages • $totalJobs total jobs available",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Numeric Page Selector Bar (Prev [1] [2] ... [N] Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prev Button
            val canPrev = currentPage > 1
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                    .alpha(if (canPrev) 1f else 0.3f)
                    .clickable(enabled = canPrev) { onPageChange(currentPage - 1) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Prev",
                        tint = if (canPrev) TextSecondary else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PREV",
                        color = if (canPrev) TextSecondary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Numeric Buttons
            val pagesToDisplay = (1..totalPages).filter { p ->
                p == 1 || p == totalPages || abs(p - currentPage) <= 2
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pagesToDisplay.forEachIndexed { idx, p ->
                    if (idx > 0 && pagesToDisplay[idx - 1] != p - 1) {
                        Text(
                            text = "...",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    val isSelected = p == currentPage
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlue else BgCard)
                            .border(
                                1.dp,
                                if (isSelected) LightBlue else BorderWhite10,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onPageChange(p) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$p",
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Next Button
            val canNext = currentPage < totalPages
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgCard)
                    .border(1.dp, BorderWhite10, RoundedCornerShape(12.dp))
                    .alpha(if (canNext) 1f else 0.3f)
                    .clickable(enabled = canNext) { onPageChange(currentPage + 1) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NEXT",
                        color = if (canNext) TextSecondary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = if (canNext) TextSecondary else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
