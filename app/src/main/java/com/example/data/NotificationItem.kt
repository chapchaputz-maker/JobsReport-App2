package com.example.data

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val isUnread: Boolean = true,
    val type: String = "INTEL" // INTEL, JOB, SYSTEM
)

val sampleNotifications = listOf(
    NotificationItem(
        id = "1",
        title = "Real-Time Ingestion Sync Complete",
        description = "Ingested 142 new verified job listings across East Africa and Worldwide.",
        timeAgo = "2m ago",
        type = "INTEL"
    ),
    NotificationItem(
        id = "2",
        title = "New Corporate Employer Registered",
        description = "TechCorp Tanzania posted 5 senior software architecture vacancies.",
        timeAgo = "15m ago",
        type = "JOB"
    ),
    NotificationItem(
        id = "3",
        title = "Market Report Generated",
        description = "Q3 2026 Tech & Engineering Salary Benchmark Report is now active.",
        timeAgo = "1h ago",
        type = "SYSTEM"
    )
)
