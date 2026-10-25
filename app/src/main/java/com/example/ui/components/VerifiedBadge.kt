package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Native Jetpack Compose equivalent of VerifiedBadge.tsx
 * Displays the verified scalloped starburst badge with an inner white checkmark.
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 15.dp,
    title: String = "Verified Account",
    color: Color = Color(0xFF1877F2)
) {
    if (color == Color(0xFF1877F2)) {
        // Fast-path vector drawable with crisp rendering
        Icon(
            painter = painterResource(id = R.drawable.ic_verified_badge),
            contentDescription = title,
            tint = Color.Unspecified,
            modifier = modifier.size(size)
        )
    } else {
        // Dynamic color fallback via Canvas
        Canvas(modifier = modifier.size(size)) {
            val scale = this.size.width / 24f

            // Scaled scalloped badge path matching VerifiedBadge.tsx
            val badgePath = Path().apply {
                moveTo(10.85f * scale, 2.14f * scale)
                quadraticTo(12.00f * scale, 1.00f * scale, 13.15f * scale, 2.14f * scale)
                quadraticTo(14.30f * scale, 3.29f * scale, 15.90f * scale, 2.92f * scale)
                quadraticTo(17.50f * scale, 2.54f * scale, 17.92f * scale, 4.10f * scale)
                quadraticTo(18.34f * scale, 5.66f * scale, 19.90f * scale, 6.10f * scale)
                quadraticTo(21.46f * scale, 6.54f * scale, 21.08f * scale, 8.10f * scale)
                quadraticTo(20.71f * scale, 9.70f * scale, 21.86f * scale, 10.85f * scale)
                quadraticTo(23.00f * scale, 12.00f * scale, 21.86f * scale, 13.15f * scale)
                quadraticTo(20.71f * scale, 14.30f * scale, 21.08f * scale, 15.90f * scale)
                quadraticTo(21.46f * scale, 17.50f * scale, 19.90f * scale, 17.92f * scale)
                quadraticTo(18.34f * scale, 18.34f * scale, 17.92f * scale, 19.90f * scale)
                quadraticTo(17.50f * scale, 21.46f * scale, 15.90f * scale, 21.08f * scale)
                quadraticTo(14.30f * scale, 20.71f * scale, 13.15f * scale, 21.86f * scale)
                quadraticTo(12.00f * scale, 23.00f * scale, 10.85f * scale, 21.86f * scale)
                quadraticTo(9.70f * scale, 20.71f * scale, 8.10f * scale, 21.08f * scale)
                quadraticTo(6.54f * scale, 21.46f * scale, 6.10f * scale, 19.90f * scale)
                quadraticTo(5.66f * scale, 18.34f * scale, 4.10f * scale, 17.92f * scale)
                quadraticTo(2.54f * scale, 17.50f * scale, 2.92f * scale, 15.90f * scale)
                quadraticTo(3.29f * scale, 14.30f * scale, 2.14f * scale, 13.15f * scale)
                quadraticTo(1.00f * scale, 12.00f * scale, 2.14f * scale, 10.85f * scale)
                quadraticTo(3.29f * scale, 9.70f * scale, 2.92f * scale, 8.10f * scale)
                quadraticTo(2.54f * scale, 6.54f * scale, 4.10f * scale, 6.10f * scale)
                quadraticTo(5.66f * scale, 5.66f * scale, 6.10f * scale, 4.10f * scale)
                quadraticTo(6.54f * scale, 2.54f * scale, 8.10f * scale, 2.92f * scale)
                quadraticTo(9.70f * scale, 3.29f * scale, 10.85f * scale, 2.14f * scale)
                close()
            }
            drawPath(path = badgePath, color = color)

            // White Checkmark stroke
            val checkPath = Path().apply {
                moveTo(7.3f * scale, 12.3f * scale)
                lineTo(10.4f * scale, 15.4f * scale)
                lineTo(16.7f * scale, 8.7f * scale)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(
                    width = 2.5f * scale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
