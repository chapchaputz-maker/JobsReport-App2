package com.example.data

import com.squareup.moshi.Json

data class JobCategory(
    @Json(name = "name") val name: String,
    @Json(name = "slug") val slug: String,
    @Json(name = "count") val count: Int? = 0,
    @Json(name = "activeCount") val activeCount: Int? = 0
) {
    val totalCount: Int
        get() = activeCount ?: count ?: 0
}

data class JobAttachment(
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String? = "Attachment",
    @Json(name = "type") val type: String? = "image",
    @Json(name = "thumbnail") val thumbnail: String? = null
)

data class JobOpportunity(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "company") val company: String,
    @Json(name = "location") val location: String? = "Remote",
    @Json(name = "salary") val salary: String? = null,
    @Json(name = "role") val role: String? = "General",
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "active") val active: Boolean? = true,
    @Json(name = "postedAt") val postedAt: String? = "Recent",
    @Json(name = "expiresAt") val expiresAt: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "companyWebsite") val companyWebsite: String? = null,
    @Json(name = "whatsapp_number") val whatsappNumber: String? = null,
    @Json(name = "application_instructions") val applicationInstructions: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "images") val images: List<JobAttachment>? = emptyList(),
    @Json(name = "employment_type") val employmentType: String? = "FULL_TIME",
    @Json(name = "workplace_type") val workplaceType: String? = "Onsite",
    @Json(name = "education_level") val educationLevel: String? = "Degree / Diploma",
    @Json(name = "experience_months") val experienceMonths: Int? = 24,
    @Json(name = "city") val city: String? = "Dar es Salaam",
    @Json(name = "region") val region: String? = "Dar es Salaam",
    @Json(name = "country") val country: String? = "Tanzania",
    @Json(name = "job_category") val jobCategory: String? = "Technology & Engineering",
    @Json(name = "street_address") val streetAddress: String? = null,
    @Json(name = "postcode") val postcode: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "skills") val skills: List<String>? = emptyList(),
    @Json(name = "benefits") val benefits: List<String>? = emptyList(),
    @Json(name = "salary_min") val salaryMin: Double? = null,
    @Json(name = "salary_max") val salaryMax: Double? = null,
    @Json(name = "salary_currency") val salaryCurrency: String? = "TZS"
)

fun JobOpportunity.isJobExpired(): Boolean {
    if (this.active == false) return true
    val exp = this.expiresAt?.trim() ?: return false
    if (exp.isEmpty()) return false
    return try {
        val datePart = exp.take(10)
        if (datePart.matches(Regex("""^\d{4}-\d{2}-\d{2}$"""))) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val expDate = sdf.parse(datePart)
            if (expDate != null) {
                val cal = java.util.Calendar.getInstance()
                cal.time = expDate
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                cal.set(java.util.Calendar.MILLISECOND, 999)
                java.util.Date().after(cal.time)
            } else false
        } else {
            false
        }
    } catch (_: Exception) {
        false
    }
}

fun JobOpportunity.resolveApplicationUrl(): String {
    val rawUrl = this.url?.trim()
    if (rawUrl.isNullOrBlank()) {
        return "https://jobsreport.online/careerredirect?id=${this.id}"
    }
    if (rawUrl.startsWith("mailto:", ignoreCase = true) ||
        rawUrl.startsWith("http://", ignoreCase = true) ||
        rawUrl.startsWith("https://", ignoreCase = true)
    ) {
        return rawUrl
    }
    if (rawUrl.startsWith("/")) {
        return "https://jobsreport.online$rawUrl"
    }
    return "https://jobsreport.online/$rawUrl"
}

data class CategoryJobsResponse(
    @Json(name = "jobs") val jobs: List<JobOpportunity>? = emptyList()
)

data class LocationInfo(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "region") val region: String? = null,
    @Json(name = "postcode") val postcode: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "activeJobsCount") val activeJobsCount: Int? = 0
)

val sampleLocations = listOf(
    LocationInfo(id = "loc-1", name = "Dar es Salaam", region = "Dar es Salaam", country = "Tanzania", postcode = "11000", activeJobsCount = 142),
    LocationInfo(id = "loc-2", name = "Arusha", region = "Arusha", country = "Tanzania", postcode = "23000", activeJobsCount = 58),
    LocationInfo(id = "loc-3", name = "Mwanza", region = "Lake Zone", country = "Tanzania", postcode = "33000", activeJobsCount = 44),
    LocationInfo(id = "loc-4", name = "Dodoma", region = "Central", country = "Tanzania", postcode = "41000", activeJobsCount = 31),
    LocationInfo(id = "loc-5", name = "Zanzibar", region = "Zanzibar Urban/West", country = "Tanzania", postcode = "71000", activeJobsCount = 26),
    LocationInfo(id = "loc-6", name = "Nairobi", region = "Nairobi County", country = "Kenya", postcode = "00100", activeJobsCount = 89),
    LocationInfo(id = "loc-7", name = "Mombasa", region = "Coast", country = "Kenya", postcode = "80100", activeJobsCount = 35),
    LocationInfo(id = "loc-8", name = "Kampala", region = "Central Region", country = "Uganda", postcode = "10101", activeJobsCount = 42),
    LocationInfo(id = "loc-9", name = "Kigali", region = "Kigali City", country = "Rwanda", postcode = "00250", activeJobsCount = 19)
)

data class Company(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String? = null,
    @Json(name = "logoUrl") val logoUrl: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "streetAddress") val streetAddress: String? = null,
    @Json(name = "area") val area: String? = null,
    @Json(name = "locality") val locality: String? = null,
    @Json(name = "district") val district: String? = null,
    @Json(name = "postalCode") val postalCode: String? = null,
    @Json(name = "postalArea") val postalArea: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "industry") val industry: String? = null,
    @Json(name = "foundedYear") val foundedYear: String? = null,
    @Json(name = "employeeCount") val employeeCount: String? = null,
    @Json(name = "totalJobs") val totalJobs: Int? = 0,
    @Json(name = "activeJobs") val activeJobs: Int? = 0
)

data class ReportStats(
    @Json(name = "companies") val companies: Int? = 0,
    @Json(name = "growth") val growth: Int? = 0
)

data class DemandPoint(
    @Json(name = "name") val name: String,
    @Json(name = "demand") val demand: Int = 0
)

data class DistributionPoint(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: Int = 0
)

data class MarketReport(
    @Json(name = "id") val id: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "excerpt") val excerpt: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "category") val category: String? = "MARKET INTEL",
    @Json(name = "role") val role: String? = "Market Intelligence",
    @Json(name = "country") val country: String? = null,
    @Json(name = "date") val date: String? = "2026",
    @Json(name = "createdAt") val createdAt: String? = null,
    @Json(name = "updatedAt") val updatedAt: String? = null,
    @Json(name = "monthYear") val monthYear: String? = "August 2026",
    @Json(name = "author") val author: String? = "JobsReport Intelligence",
    @Json(name = "readTime") val readTime: String? = "3 min read",
    @Json(name = "image") val image: String? = null,
    @Json(name = "stats") val stats: ReportStats? = null,
    @Json(name = "chartData") val chartData: List<DemandPoint>? = null,
    @Json(name = "distribution") val distribution: List<DistributionPoint>? = null,
    @Json(name = "companies") val companies: List<Company>? = null,
    @Json(name = "jobs") val jobs: List<JobOpportunity>? = null
) {
    val displaySummary: String
        get() = excerpt ?: summary ?: ""

    val displayDate: String
        get() = monthYear ?: date ?: "2026"

    val displayCategory: String
        get() = category ?: role ?: "MARKET INTEL"
}

data class HomeResponse(
    @Json(name = "reports") val reports: List<MarketReport>? = emptyList(),
    @Json(name = "spotlightCompanies") val spotlightCompanies: List<String>? = emptyList()
)

data class MarketResponse(
    @Json(name = "activeJobs") val activeJobs: List<JobOpportunity>? = null,
    @Json(name = "jobs") val jobs: List<JobOpportunity>? = null
)

// Sample initial mock data for immediate smooth rendering
val sampleJobCategories = listOf(
    JobCategory("Software & Engineering", "software-engineering", activeCount = 42),
    JobCategory("Data & Analytics", "data-analytics", activeCount = 28),
    JobCategory("Finance & Accounting", "finance-accounting", activeCount = 35),
    JobCategory("Design & Creative", "design-creative", activeCount = 19),
    JobCategory("Customer Support", "customer-support", activeCount = 24),
    JobCategory("HR & Talent", "hr-talent", activeCount = 15),
    JobCategory("Cybersecurity", "cybersecurity", activeCount = 18),
    JobCategory("Logistics & Supply", "logistics-supply", activeCount = 22),
    JobCategory("Healthcare & Medical", "healthcare-medical", activeCount = 31),
    JobCategory("Sales & Marketing", "sales-marketing", activeCount = 45),
    JobCategory("Education & Training", "education-training", activeCount = 14),
    JobCategory("Construction & Real Estate", "construction-real-estate", activeCount = 20),
    JobCategory("Legal & Compliance", "legal-compliance", activeCount = 12),
    JobCategory("Agriculture & Forestry", "agriculture-forestry", activeCount = 26),
    JobCategory("Hospitality & Culinary", "hospitality-culinary", activeCount = 17)
)

val sampleJobOpportunities = listOf(
    JobOpportunity(
        id = "job-101",
        title = "Senior Kotlin Mobile Architect",
        company = "TechCorp Tanzania",
        location = "Dar es Salaam, Tanzania",
        salary = "TZS 4,500,000 - 6,800,000 / mo",
        role = "Engineering",
        logoUrl = "https://media.jobsreport.online/file_0000000084b47243aec7e8cf3cbeb6bd.png",
        description = "<p>TechCorp Tanzania is seeking an experienced <b>Senior Kotlin Mobile Architect</b> to lead our Android application engineering initiatives across East Africa.</p><p><b>Key Responsibilities:</b></p><ul><li>Architect and maintain high-performance Jetpack Compose Android applications.</li><li>Implement clean MVVM architecture, Room database caching, and offline-first data pipelines.</li><li>Collaborate with backend teams to integrate RESTful microservices.</li></ul><p><b>Requirements:</b> Minimum 5 years Android development experience, strong Kotlin Coroutines expertise, and proven track record in financial or telecom domain.</p>",
        companyWebsite = "https://techcorp.co.tz",
        whatsappNumber = "+255712345678",
        applicationInstructions = "Submit your updated CV and GitHub portfolio via WhatsApp or apply directly through our corporate recruitment portal.",
        url = "https://techcorp.co.tz/careers/kotlin-architect",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        educationLevel = "Bachelor's Degree in Computer Science",
        experienceMonths = 60,
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Software & Engineering",
        postedAt = "2 hours ago",
        expiresAt = "2026-09-30",
        images = listOf(
            JobAttachment(
                url = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800",
                name = "Office Environment & Engineering Specs.pdf",
                type = "pdf"
            ),
            JobAttachment(
                url = "https://images.unsplash.com/photo-1531403009284-440f080d1e12?w=800",
                name = "Tech Stack Architecture Diagram.jpg",
                type = "image"
            )
        )
    ),
    JobOpportunity(
        id = "job-102",
        title = "Corporate Financial Controller",
        company = "CRDB Bank Plc",
        location = "Arusha, Tanzania",
        salary = "TZS 5,200,000 / mo",
        role = "Finance",
        logoUrl = null,
        description = "<p>CRDB Bank Plc is looking for a dynamic <b>Corporate Financial Controller</b> to oversee regional commercial banking portfolio analytics and financial compliance in Northern Zone offices.</p><p><b>Responsibilities:</b> Manage quarterly reporting, audit readiness, regulatory compliance with Bank of Tanzania guidelines, and financial forecasting.</p>",
        companyWebsite = "https://crdbbank.co.tz",
        whatsappNumber = null,
        applicationInstructions = "Please send your CV and cover letter directly to careers@crdbbank.co.tz",
        url = "mailto:careers@crdbbank.co.tz",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        educationLevel = "CPA / Master's in Finance",
        experienceMonths = 48,
        city = "Arusha",
        country = "Tanzania",
        jobCategory = "Finance & Accounting",
        postedAt = "1 day ago",
        expiresAt = "2026-09-15"
    ),
    JobOpportunity(
        id = "job-103",
        title = "Lead UI/UX Systems Designer",
        company = "Vodacom East Africa",
        location = "Remote / Tanzania",
        salary = "$3,500 - $5,000 / mo",
        role = "Design",
        logoUrl = null,
        description = "<p>Vodacom East Africa invites applications for <b>Lead UI/UX Systems Designer</b> to scale our digital ecosystem design system across web and mobile platforms.</p>",
        companyWebsite = "https://vodacom.co.tz",
        whatsappNumber = "+255754000111",
        applicationInstructions = "Share your Figma portfolio link along with your application.",
        url = "https://vodacom.co.tz/careers",
        employmentType = "CONTRACT",
        workplaceType = "Remote",
        educationLevel = "Degree or equivalent portfolio",
        experienceMonths = 36,
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Design & Creative",
        postedAt = "3 days ago",
        expiresAt = "2026-10-01"
    ),
    JobOpportunity(
        id = "job-104",
        title = "Regional Supply Chain Manager",
        company = "Kilimanjaro Logistics",
        location = "Mwanza, Tanzania",
        salary = "TZS 3,800,000 / mo",
        role = "Logistics",
        logoUrl = null,
        description = "<p>Oversee logistics operations, fleet allocation, freight transit compliance, and warehousing across Lake Zone hubs.</p>",
        companyWebsite = "https://kilimanjarologistics.co.tz",
        whatsappNumber = null,
        applicationInstructions = "Apply online through the Kilimanjaro Logistics talent site.",
        url = "https://kilimanjarologistics.co.tz/jobs",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        educationLevel = "Bachelor's in Supply Chain / Logistics",
        experienceMonths = 36,
        city = "Mwanza",
        country = "Tanzania",
        jobCategory = "Logistics & Supply",
        postedAt = "4 days ago",
        expiresAt = "2026-09-20"
    ),
    JobOpportunity(
        id = "job-105",
        title = "Cloud Infrastructure Security Lead",
        company = "Global CyberTech",
        location = "Worldwide Remote",
        salary = "$80,000 - $110,000 / yr",
        role = "Security",
        logoUrl = null,
        description = "<p>Global CyberTech is hiring a <b>Cloud Infrastructure Security Lead</b> to secure multi-cloud AWS & GCP production clusters, manage IAM permissions, and lead SOC compliance.</p>",
        companyWebsite = "https://globalcybertech.com",
        whatsappNumber = null,
        applicationInstructions = "Submit application through Global CyberTech security careers board.",
        url = "https://globalcybertech.com/careers/sec-lead",
        employmentType = "FULL_TIME",
        workplaceType = "Remote",
        educationLevel = "B.Sc / CISSP / AWS Security Certified",
        experienceMonths = 60,
        city = "Worldwide",
        country = "Worldwide",
        jobCategory = "Cybersecurity",
        postedAt = "5 days ago",
        expiresAt = "2026-10-15"
    ),
    JobOpportunity(
        id = "job-106",
        title = "Full Stack React & Node Developer",
        company = "Safaricom Innovation Lab",
        location = "Nairobi, Kenya",
        salary = "KES 280,000 - 350,000 / mo",
        role = "Engineering",
        description = "<p>Build high-concurrency microservices and customer portal frontends for M-PESA ecosystem digital products.</p>",
        companyWebsite = "https://safaricom.co.ke",
        url = "https://safaricom.co.ke/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        city = "Nairobi",
        country = "Kenya",
        jobCategory = "Software & Engineering",
        postedAt = "1 day ago"
    ),
    JobOpportunity(
        id = "job-107",
        title = "Senior Data Analyst & BI Specialist",
        company = "Equity Bank Group",
        location = "Nairobi, Kenya",
        salary = "KES 320,000 / mo",
        role = "Analytics",
        description = "<p>Lead business intelligence analytics and SQL data pipelines across East African retail branch networks.</p>",
        companyWebsite = "https://equitygroupholdings.com",
        url = "https://equitygroupholdings.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Nairobi",
        country = "Kenya",
        jobCategory = "Data & Analytics",
        postedAt = "2 days ago"
    ),
    JobOpportunity(
        id = "job-108",
        title = "Mobile Money Operations Manager",
        company = "MTN Uganda",
        location = "Kampala, Uganda",
        salary = "UGX 6,500,000 / mo",
        role = "Operations",
        description = "<p>Oversee agent liquidity management, compliance, and fraud monitoring for MoMo digital wallet services.</p>",
        companyWebsite = "https://mtn.co.ug",
        url = "https://mtn.co.ug/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Kampala",
        country = "Uganda",
        jobCategory = "Banking & Finance",
        postedAt = "3 days ago"
    ),
    JobOpportunity(
        id = "job-109",
        title = "AI & Machine Learning Researcher",
        company = "Kigali Tech Hub",
        location = "Kigali, Rwanda",
        salary = "$2,800 / mo",
        role = "AI Research",
        description = "<p>Develop Natural Language Processing models for African language translation and agriculture computer vision models.</p>",
        companyWebsite = "https://kigalitech.rw",
        url = "https://kigalitech.rw/jobs",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        city = "Kigali",
        country = "Rwanda",
        jobCategory = "Software & Engineering",
        postedAt = "4 days ago"
    ),
    JobOpportunity(
        id = "job-110",
        title = "Head of Digital Marketing & Brand",
        company = "Tigo Tanzania",
        location = "Dar es Salaam, Tanzania",
        salary = "TZS 4,200,000 / mo",
        role = "Marketing",
        description = "<p>Drive digital campaign growth, social channel engagement, performance advertising, and PR across Tanzania.</p>",
        companyWebsite = "https://tigo.co.tz",
        url = "https://tigo.co.tz/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Marketing & Sales",
        postedAt = "2 days ago"
    ),
    JobOpportunity(
        id = "job-111",
        title = "Hospital Operations Executive",
        company = "Aga Khan Health Services",
        location = "Dar es Salaam, Tanzania",
        salary = "TZS 3,500,000 / mo",
        role = "Healthcare",
        description = "<p>Manage clinical facilities, patient flow efficiency, medical inventory procurement, and compliance protocols.</p>",
        companyWebsite = "https://agakhanhospitals.org",
        url = "https://agakhanhospitals.org/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Healthcare & Medicine",
        postedAt = "5 days ago"
    ),
    JobOpportunity(
        id = "job-112",
        title = "DevOps & Kubernetes Engineer",
        company = "Paystack Africa",
        location = "Lagos / Remote",
        salary = "$4,000 - $6,000 / mo",
        role = "Engineering",
        description = "<p>Maintain high-availability payment gateway infrastructure with Terraform, Docker, and AWS EKS.</p>",
        companyWebsite = "https://paystack.com",
        url = "https://paystack.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Remote",
        city = "Lagos",
        country = "Nigeria",
        jobCategory = "Software & Engineering",
        postedAt = "1 day ago"
    ),
    JobOpportunity(
        id = "job-113",
        title = "Senior Product Manager",
        company = "Flutterwave",
        location = "Worldwide Remote",
        salary = "$5,500 / mo",
        role = "Product",
        description = "<p>Lead cross-border merchant payout experiences and API integration developer toolkits.</p>",
        companyWebsite = "https://flutterwave.com",
        url = "https://flutterwave.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Remote",
        city = "Worldwide",
        country = "Worldwide",
        jobCategory = "Management",
        postedAt = "3 days ago"
    ),
    JobOpportunity(
        id = "job-114",
        title = "Agribusiness Operations Specialist",
        company = "Kilimo Tech Africa",
        location = "Dodoma, Tanzania",
        salary = "TZS 2,800,000 / mo",
        role = "Agriculture",
        description = "<p>Implement IoT sensor soil monitoring networks and farmer cooperative supply-chain logistics.</p>",
        companyWebsite = "https://kilimotech.co.tz",
        url = "https://kilimotech.co.tz/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Dodoma",
        country = "Tanzania",
        jobCategory = "Agriculture & Forestry",
        postedAt = "6 days ago"
    ),
    JobOpportunity(
        id = "job-115",
        title = "Renewable Energy Solar Engineer",
        company = "Zola Electric",
        location = "Arusha, Tanzania",
        salary = "TZS 3,200,000 / mo",
        role = "Engineering",
        description = "<p>Design commercial and industrial solar microgrid installations across East Africa.</p>",
        companyWebsite = "https://zolaelectric.com",
        url = "https://zolaelectric.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Arusha",
        country = "Tanzania",
        jobCategory = "Engineering & Telecom",
        postedAt = "4 days ago"
    ),
    JobOpportunity(
        id = "job-116",
        title = "Corporate Tax & Legal Advisor",
        company = "PwC East Africa",
        location = "Dar es Salaam, Tanzania",
        salary = "TZS 4,800,000 / mo",
        role = "Legal & Compliance",
        description = "<p>Advise multinational clients on cross-border tax compliance, transfer pricing, and corporate restructuring.</p>",
        companyWebsite = "https://pwc.com/tz",
        url = "https://pwc.com/tz/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Legal & Compliance",
        postedAt = "1 day ago"
    ),
    JobOpportunity(
        id = "job-117",
        title = "Human Resources Director",
        company = "NMB Bank Plc",
        location = "Dar es Salaam, Tanzania",
        salary = "TZS 6,000,000 / mo",
        role = "Human Resources",
        description = "<p>Lead talent acquisition, employee development, and organizational culture for over 3,000 staff members.</p>",
        companyWebsite = "https://nmbbank.co.tz",
        url = "https://nmbbank.co.tz/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Dar es Salaam",
        country = "Tanzania",
        jobCategory = "Human Resources",
        postedAt = "3 days ago"
    ),
    JobOpportunity(
        id = "job-118",
        title = "Customer Success Lead",
        company = "Zendesk Global",
        location = "Worldwide Remote",
        salary = "$4,200 / mo",
        role = "Customer Experience",
        description = "<p>Manage enterprise client onboarding, retention, and support resolution for EMEA region enterprise customers.</p>",
        companyWebsite = "https://zendesk.com",
        url = "https://zendesk.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Remote",
        city = "Worldwide",
        country = "Worldwide",
        jobCategory = "Customer Service",
        postedAt = "2 days ago"
    ),
    JobOpportunity(
        id = "job-119",
        title = "Cybersecurity SOC Lead Specialist",
        company = "Stanbic Bank Kenya",
        location = "Nairobi, Kenya",
        salary = "KES 380,000 / mo",
        role = "Security",
        description = "<p>Lead the Security Operations Center in threat hunting, incident response, and SIEM monitoring.</p>",
        companyWebsite = "https://stanbicbank.co.ke",
        url = "https://stanbicbank.co.ke/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Nairobi",
        country = "Kenya",
        jobCategory = "Cybersecurity",
        postedAt = "4 days ago"
    ),
    JobOpportunity(
        id = "job-120",
        title = "E-Commerce Growth Manager",
        company = "Jumia East Africa",
        location = "Nairobi, Kenya",
        salary = "KES 250,000 / mo",
        role = "E-Commerce",
        description = "<p>Drive vendor marketplace acquisition, campaign merchandising, and basket size optimization.</p>",
        companyWebsite = "https://jumia.co.ke",
        url = "https://jumia.co.ke/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        city = "Nairobi",
        country = "Kenya",
        jobCategory = "Marketing & Sales",
        postedAt = "5 days ago"
    ),
    JobOpportunity(
        id = "job-121",
        title = "Hotel & Resort General Manager",
        company = "Serena Hotels & Resorts",
        location = "Zanzibar, Tanzania",
        salary = "$3,800 / mo",
        role = "Hospitality",
        description = "<p>Direct luxury beach resort operations, guest satisfaction, VIP logistics, and revenue management.</p>",
        companyWebsite = "https://serenahotels.com",
        url = "https://serenahotels.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Zanzibar",
        country = "Tanzania",
        jobCategory = "Hospitality & Culinary",
        postedAt = "1 week ago"
    ),
    JobOpportunity(
        id = "job-122",
        title = "Mining & Geological Engineer",
        company = "Barrick Gold Corporation",
        location = "Kahama, Tanzania",
        salary = "TZS 5,800,000 / mo",
        role = "Mining",
        description = "<p>Supervise open-pit resource extraction, safety protocol audits, and geological survey telemetry.</p>",
        companyWebsite = "https://barrick.com",
        url = "https://barrick.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Kahama",
        country = "Tanzania",
        jobCategory = "Engineering & Telecom",
        postedAt = "3 days ago"
    ),
    JobOpportunity(
        id = "job-123",
        title = "Principal Curriculum Developer",
        company = "African Leadership University",
        location = "Kigali, Rwanda",
        salary = "$3,000 / mo",
        role = "Education",
        description = "<p>Design project-based software engineering and entrepreneurship curricula for undergraduate scholars.</p>",
        companyWebsite = "https://alueducation.com",
        url = "https://alueducation.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Hybrid",
        city = "Kigali",
        country = "Rwanda",
        jobCategory = "Education & Training",
        postedAt = "4 days ago"
    ),
    JobOpportunity(
        id = "job-124",
        title = "Telecom Radio Network Engineer",
        company = "Airtel Africa",
        location = "Kampala, Uganda",
        salary = "UGX 5,500,000 / mo",
        role = "Telecom",
        description = "<p>Optimize 4G/5G cell tower coverage, frequency planning, and transmission backhaul links.</p>",
        companyWebsite = "https://airtel.ug",
        url = "https://airtel.ug/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Onsite",
        city = "Kampala",
        country = "Uganda",
        jobCategory = "Engineering & Telecom",
        postedAt = "2 days ago"
    ),
    JobOpportunity(
        id = "job-125",
        title = "Senior Technical Writer",
        company = "GitLab Global",
        location = "Worldwide Remote",
        salary = "$4,500 / mo",
        role = "Documentation",
        description = "<p>Create clear API documentation, developer guides, and DevOps workflow tutorials for open source contributors.</p>",
        companyWebsite = "https://gitlab.com",
        url = "https://gitlab.com/careers",
        employmentType = "FULL_TIME",
        workplaceType = "Remote",
        city = "Worldwide",
        country = "Worldwide",
        jobCategory = "Design & Creative",
        postedAt = "1 day ago"
    )
)

val sampleMarketReports = listOf(
    MarketReport(
        id = "rep-01",
        slug = "q3-2026-east-africa-tech-salary-index",
        title = "Q3 2026 East Africa Tech & Fintech Salary Index",
        summary = "In-depth analysis of developer compensation, remote hiring benchmarks, and talent migration across Dar es Salaam, Nairobi, and Kigali.",
        excerpt = "In-depth analysis of developer compensation, remote hiring benchmarks, and talent migration across Dar es Salaam, Nairobi, and Kigali.",
        category = "SALARY BENCHMARK",
        role = "Software & Engineering",
        country = "Tanzania",
        date = "August 2026",
        monthYear = "August 2026",
        author = "JobsReport Research",
        readTime = "4 min read"
    ),
    MarketReport(
        id = "rep-02",
        slug = "banking-digital-financial-services-hiring-surge",
        title = "Banking & Digital Financial Services Hiring Surge",
        summary = "How mobile money interoperability and AI fraud prevention are driving high demand for data engineers and security analysts.",
        excerpt = "How mobile money interoperability and AI fraud prevention are driving high demand for data engineers and security analysts.",
        category = "SECTOR REPORT",
        role = "Finance & Accounting",
        country = "Tanzania",
        date = "July 2026",
        monthYear = "July 2026",
        author = "Market Telemetry",
        readTime = "5 min read"
    ),
    MarketReport(
        id = "rep-03",
        slug = "2026-remote-work-global-employer-trends",
        title = "2026 Remote Work & Global Employer Trends",
        summary = "Key insights on international employers contracting tech, design, and customer support talent in emerging regional markets.",
        excerpt = "Key insights on international employers contracting tech, design, and customer support talent in emerging regional markets.",
        category = "GLOBAL TRENDS",
        role = "Remote Engineering",
        country = "Kenya",
        date = "August 2026",
        monthYear = "August 2026",
        author = "Talent Intelligence Unit",
        readTime = "3 min read"
    ),
    MarketReport(
        id = "rep-04",
        slug = "south-africa-cybersecurity-talent-landscape",
        title = "South Africa Enterprise Cybersecurity Talent Demand",
        summary = "Analysis of SOC analyst shortages and cloud security architecture hiring trends across Johannesburg and Cape Town.",
        excerpt = "Analysis of SOC analyst shortages and cloud security architecture hiring trends across Johannesburg and Cape Town.",
        category = "CYBERSECURITY",
        role = "Security & Cloud",
        country = "South Africa",
        date = "July 2026",
        monthYear = "July 2026",
        author = "Security Intelligence Ops",
        readTime = "6 min read"
    ),
    MarketReport(
        id = "rep-05",
        slug = "nigeria-fintech-developer-compensation",
        title = "Nigeria Fintech Developer Compensation & Market Velocity",
        summary = "Quarterly benchmark tracking backend API engineers, mobile payment integration leads, and compliance technology talent.",
        excerpt = "Quarterly benchmark tracking backend API engineers, mobile payment integration leads, and compliance technology talent.",
        category = "FINTECH",
        role = "Software & Engineering",
        country = "Nigeria",
        date = "June 2026",
        monthYear = "June 2026",
        author = "Lagos Tech Desk",
        readTime = "4 min read"
    )
)

val sampleSpotlightCompanies = listOf(
    "CRDB Bank", "Vodacom Tanzania", "TechCorp East Africa", "Kilimanjaro Logistics", "Standard Chartered", "NMB Bank"
)

val sampleCompanies = listOf(
    Company(
        id = "comp-101",
        name = "TechCorp Tanzania",
        url = "https://techcorp.co.tz",
        logoUrl = "https://media.jobsreport.online/file_0000000084b47243aec7e8cf3cbeb6bd.png",
        description = "TechCorp Tanzania is East Africa's leading software development and enterprise cloud solutions provider, building mobile financial apps and AI infrastructure.",
        streetAddress = "Haile Selassie Road, Plot 45",
        area = "Oysterbay",
        locality = "Dar es Salaam",
        district = "Kinondoni",
        postalCode = "14111",
        postalArea = "Kinondoni",
        country = "TZ",
        industry = "Technology & Software",
        foundedYear = "2018",
        employeeCount = "150-250",
        totalJobs = 12,
        activeJobs = 4
    ),
    Company(
        id = "comp-102",
        name = "CRDB Bank Plc",
        url = "https://crdbbank.co.tz",
        logoUrl = null,
        description = "CRDB Bank Plc is the largest commercial bank in Tanzania, offering retail, commercial, corporate, agency, and digital banking solutions across East Africa.",
        streetAddress = "Azikiwe Street, CRDB Tower",
        area = "City Centre",
        locality = "Dar es Salaam",
        district = "Ilala",
        postalCode = "11101",
        country = "TZ",
        industry = "Banking & Financial Services",
        foundedYear = "1996",
        employeeCount = "3,000+",
        totalJobs = 25,
        activeJobs = 8
    ),
    Company(
        id = "comp-103",
        name = "Vodacom East Africa",
        url = "https://vodacom.co.tz",
        logoUrl = null,
        description = "Vodacom Tanzania is a leading telecommunications company providing voice, data, M-Pesa financial services, and cloud solutions to millions of customers.",
        streetAddress = "Ursino Estate, Old Bagamoyo Road",
        area = "Victoria",
        locality = "Dar es Salaam",
        district = "Kinondoni",
        postalCode = "14100",
        country = "TZ",
        industry = "Telecommunications & Fintech",
        foundedYear = "1999",
        employeeCount = "1,200+",
        totalJobs = 18,
        activeJobs = 6
    ),
    Company(
        id = "comp-104",
        name = "Kilimanjaro Logistics",
        url = "https://kilimanjarologistics.co.tz",
        logoUrl = null,
        description = "Integrated freight forwarding, cold chain supply, and fleet logistics company servicing Tanzania, Kenya, Uganda, and Zambia transit corridors.",
        streetAddress = "Nyerere Road Industrial Area",
        area = "Vingunguti",
        locality = "Dar es Salaam",
        district = "Ilala",
        postalCode = "12100",
        country = "TZ",
        industry = "Logistics & Supply Chain",
        foundedYear = "2012",
        employeeCount = "200-500",
        totalJobs = 8,
        activeJobs = 3
    ),
    Company(
        id = "comp-105",
        name = "Global CyberTech",
        url = "https://globalcybertech.com",
        logoUrl = null,
        description = "Global cybersecurity firm delivering managed threat detection, zero-trust cloud architecture, and SOC audit compliance for enterprise clients worldwide.",
        streetAddress = "500 Howard Street",
        area = "Downtown",
        locality = "San Francisco",
        district = "CA",
        postalCode = "94105",
        country = "USA",
        industry = "Cybersecurity",
        foundedYear = "2015",
        employeeCount = "500-1,000",
        totalJobs = 30,
        activeJobs = 10
    )
)

// ============================================================
// STORY MODELS & TELEMETRY
// ============================================================
data class StoryUser(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "profile_image_url") val profileImageUrl: String? = null,
    @Json(name = "cover_image_url") val coverImageUrl: String? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "work") val work: String? = null,
    @Json(name = "education") val education: String? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "website") val website: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "is_verified") val isVerified: Any? = null,
    @Json(name = "followers_count") val followersCount: Int? = 0,
    @Json(name = "following_count") val followingCount: Int? = 0,
    @Json(name = "jobs_count") val jobsCount: Int? = 0,
    @Json(name = "stories_count") val storiesCount: Int? = 0,
    @Json(name = "is_following") val isFollowing: Boolean? = false,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "job_adverts_count") val jobAdvertsCount: Int? = 0,
    @Json(name = "joined_date") val joinedDate: String? = null,
    @Json(name = "skills") val skills: List<String>? = null
)

val StoryUser.isUserVerified: Boolean
    get() = when (val v = this.isVerified) {
        true, 1, 1L, "1", "true", "TRUE" -> true
        is Number -> v.toInt() == 1
        is CharSequence -> v.toString().trim().let { it == "1" || it.equals("true", ignoreCase = true) }
        else -> false
    }

data class StoryType(
    @Json(name = "id") val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "slug") val slug: String = "",
    @Json(name = "type") val type: String = "text",
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "media_thumb") val mediaThumb: String? = null,
    @Json(name = "text_content") val textContent: String? = null,
    @Json(name = "background_style") val backgroundStyle: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expires_at") val expiresAt: String,
    @Json(name = "is_active") val isActive: Any? = 1,
    @Json(name = "views_count") val viewsCount: Int? = 0,
    @Json(name = "user") val user: StoryUser? = null
)

data class CreateStoryPayload(
    @Json(name = "type") val type: String,
    @Json(name = "media_url") val mediaUrl: String? = null,
    @Json(name = "media_thumb") val mediaThumb: String? = null,
    @Json(name = "text_content") val textContent: String? = null,
    @Json(name = "background_style") val backgroundStyle: String? = null,
    @Json(name = "expires_at") val expiresAt: String
)

data class StoriesResponse(
    @Json(name = "stories") val stories: List<StoryType>? = null
)

data class UploadResponse(
    @Json(name = "url") val url: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "dataUrl") val dataUrl: String? = null,
    @Json(name = "error") val error: String? = null
)

val STORY_BACKGROUNDS = listOf(
    "linear-gradient(135deg, #1877F2, #0055FF)",
    "linear-gradient(135deg, #F3425F, #E41E3F)",
    "linear-gradient(135deg, #45BD62, #31A24C)",
    "linear-gradient(135deg, #F7B928, #E3A300)",
    "linear-gradient(135deg, #A033FF, #7B1FA2)",
    "linear-gradient(135deg, #FF7E5F, #FEB47B)",
    "linear-gradient(135deg, #00C6FF, #0072FF)",
    "linear-gradient(135deg, #2193b0, #6dd5ed)",
    "linear-gradient(135deg, #42275a, #734b6d)",
    "linear-gradient(135deg, #000000, #434343)"
)

fun parseStoryServerTime(value: String?): Long {
    val s = value?.trim().orEmpty()
    if (s.isEmpty()) return System.currentTimeMillis()
    return try {
        if (s.contains("T")) {
            val iso = if (!s.endsWith("Z") && !s.contains("+") && !s.substring(10).contains("-")) s + "Z" else s
            java.time.Instant.parse(iso).toEpochMilli()
        } else if (s.matches(Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}.*"""))) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(s)?.time ?: System.currentTimeMillis()
        } else {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            sdf.parse(s.take(10))?.time ?: System.currentTimeMillis()
        }
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

fun formatStoryTime(createdAt: String?): String {
    val t = parseStoryServerTime(createdAt)
    val diff = System.currentTimeMillis() - t
    if (diff < 60_000) return "Just now"
    if (diff < 3_600_000) return "${diff / 60_000}min"
    if (diff < 86_400_000) return "${diff / 3_600_000}hrs"
    return "${diff / 86_400_000}days"
}

fun formatStoryCount(n: Int): String {
    if (n >= 1_000_000) return String.format(java.util.Locale.US, "%.1fM", n / 1_000_000f)
    if (n >= 1_000) return String.format(java.util.Locale.US, "%.1fK", n / 1_000f)
    return n.toString()
}

fun getDefaultStoryAvatar(name: String, seed: String): String {
    val colors = listOf("1877F2", "45BD62", "F3425F", "F7B928", "9360F7")
    val hash = seed.toCharArray().fold(0) { acc, c -> acc + c.code }
    val color = colors[Math.abs(hash) % colors.size]
    val init = (name.ifBlank { "U" }).take(1).uppercase()
    return "https://ui-avatars.com/api/?name=${java.net.URLEncoder.encode(init, "UTF-8")}&background=${color}&color=fff&size=128&bold=true&rounded=true"
}

fun isStoryExpired(story: StoryType): Boolean {
    val active = story.isActive
    if (active == 0 || active == 0L || active == false || active == "0" || active == "false") return true
    val expires = story.expiresAt.trim()
    if (expires.isEmpty()) return false
    return parseStoryServerTime(expires) < System.currentTimeMillis()
}

val sampleStories = emptyList<StoryType>()

// ============================================================
// USER AUTH MODELS (JobsReport UserAuth.tsx Parity)
// ============================================================
data class LoginPayload(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "remember") val remember: Boolean? = true
)

data class RegisterPayload(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "username") val username: String? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "location") val location: String? = null
)

data class ForgotPasswordPayload(
    @Json(name = "email") val email: String
)

data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: StoryUser? = null
)

