package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun getRoleIcon(name: String, slug: String = ""): ImageVector {
    val lower = (name + " " + slug).lowercase()
    return when {
        "code" in lower || "tech" in lower || "software" in lower || "dev" in lower || "eng" in lower -> Icons.Default.Code
        "chart" in lower || "data" in lower || "analytic" in lower -> Icons.Default.BarChart
        "finance" in lower || "account" in lower || "calculator" in lower || "tax" in lower || "bank" in lower -> Icons.Default.Calculate
        "design" in lower || "palette" in lower || "art" in lower || "creative" in lower || "ui" in lower -> Icons.Default.Palette
        "support" in lower || "headphone" in lower || "call" in lower || "customer" in lower -> Icons.Default.Headphones
        "hr" in lower || "user" in lower || "people" in lower || "talent" in lower || "recruit" in lower -> Icons.Default.Group
        "security" in lower || "shield" in lower || "cyber" in lower || "safety" in lower -> Icons.Default.Security
        "logistics" in lower || "truck" in lower || "transport" in lower || "supply" in lower -> Icons.Default.LocalShipping
        "health" in lower || "med" in lower || "nurse" in lower || "doctor" in lower || "care" in lower -> Icons.Default.MedicalServices
        "sale" in lower || "market" in lower || "growth" in lower || "trend" in lower -> Icons.Default.TrendingUp
        "education" in lower || "teach" in lower || "book" in lower || "train" in lower -> Icons.Default.MenuBook
        "build" in lower || "real estate" in lower || "const" in lower || "corp" in lower -> Icons.Default.Business
        "legal" in lower || "law" in lower || "scale" in lower || "compliance" in lower -> Icons.Default.Balance
        "agri" in lower || "farm" in lower || "eco" in lower || "leaf" in lower -> Icons.Default.Eco
        "food" in lower || "hotel" in lower || "restaurant" in lower || "chef" in lower -> Icons.Default.Restaurant
        "zap" in lower || "electric" in lower || "energy" in lower -> Icons.Default.FlashOn
        else -> Icons.Default.Work
    }
}
