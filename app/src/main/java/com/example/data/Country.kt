package com.example.data

data class Country(
    val code: String,
    val name: String,
    val flag: String
)

val defaultCountriesList = listOf(
    Country("TZ", "Tanzania", "🇹🇿"),
    Country("US", "United States", "🇺🇸"),
    Country("GB", "United Kingdom", "🇬🇧"),
    Country("KE", "Kenya", "🇰🇪"),
    Country("UG", "Uganda", "🇺🇬"),
    Country("ZA", "South Africa", "🇿🇦"),
    Country("NG", "Nigeria", "🇳🇬"),
    Country("IN", "India", "🇮🇳"),
    Country("CA", "Canada", "🇨🇦"),
    Country("DE", "Germany", "🇩🇪"),
    Country("FR", "France", "🇫🇷"),
    Country("AU", "Australia", "🇦🇺"),
    Country("AE", "United Arab Emirates", "🇦🇪"),
    Country("SA", "Saudi Arabia", "🇸🇦"),
    Country("SG", "Singapore", "🇸🇬"),
    Country("RW", "Rwanda", "🇷🇼"),
    Country("ET", "Ethiopia", "🇪🇹"),
    Country("GH", "Ghana", "🇬🇭"),
    Country("EG", "Egypt", "🇪🇬")
)
