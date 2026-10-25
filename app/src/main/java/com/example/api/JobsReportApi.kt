package com.example.api

import com.example.data.CategoryJobsResponse
import com.example.data.Company
import com.example.data.HomeResponse
import com.example.data.JobCategory
import com.example.data.JobOpportunity
import com.example.data.LocationInfo
import com.example.data.MarketReport
import com.example.data.MarketResponse
import com.example.data.CreateStoryPayload
import com.example.data.LoginPayload
import com.example.data.RegisterPayload
import com.example.data.ForgotPasswordPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import okhttp3.RequestBody
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface JobsReportApi {
    @GET("api/home")
    suspend fun getHomeData(@Query("country") country: String): HomeResponse

    @GET("api/market")
    suspend fun getMarketData(@Query("limit") limit: Int = 5): MarketResponse

    @GET("api/categories")
    suspend fun getCategories(): List<JobCategory>

    @GET("api/companies-jobs")
    suspend fun getCompanies(): List<Company>

    @GET("api/company-jobs/{id}")
    suspend fun getCompanyJobs(@Path("id") companyId: String): List<JobOpportunity>

    @GET("api/category-jobs")
    suspend fun getCategoryJobs(@Query("category") category: String): CategoryJobsResponse

    @GET("api/reports")
    suspend fun getReports(): List<MarketReport>

    @GET("api/reports/{slug}")
    suspend fun getReportDetail(@Path("slug") slug: String): MarketReport

    @GET("api/locations")
    suspend fun getLocations(): List<LocationInfo>

    @GET("api/stories")
    suspend fun getStoriesRaw(): ResponseBody

    @GET("api/stories/{slug}")
    suspend fun getStoryDetail(@Path("slug") slug: String): ResponseBody

    @POST("api/stories/{id}/view")
    suspend fun recordStoryView(@Path("id") id: String): ResponseBody

    @POST("api/stories")
    suspend fun createStory(
        @Header("Authorization") auth: String?,
        @Body payload: CreateStoryPayload
    ): ResponseBody

    @POST("api/stories")
    suspend fun createStoryRaw(
        @Header("Authorization") auth: String?,
        @Body payload: RequestBody
    ): ResponseBody

    @DELETE("api/stories/{id}")
    suspend fun deleteStory(
        @Header("Authorization") auth: String?,
        @Path("id") id: String
    ): ResponseBody

    @Multipart
    @POST("api/upload")
    suspend fun uploadMedia(
        @Header("Authorization") auth: String?,
        @Part file: MultipartBody.Part,
        @Part thumbnail: MultipartBody.Part?,
        @Part name: MultipartBody.Part,
        @Part altText: MultipartBody.Part
    ): ResponseBody

    @POST("api/auth/user/login")
    suspend fun login(@Body payload: LoginPayload): ResponseBody

    @POST("api/auth/user/login")
    suspend fun loginRaw(@Body payload: RequestBody): ResponseBody

    @POST("api/auth/user/register")
    suspend fun register(@Body payload: RegisterPayload): ResponseBody

    @POST("api/auth/user/register")
    suspend fun registerRaw(@Body payload: RequestBody): ResponseBody

    @GET("api/auth/user/me")
    suspend fun getAuthUserMe(@Header("Authorization") auth: String): ResponseBody

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body payload: ForgotPasswordPayload): ResponseBody

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): ResponseBody

    @GET("api/users/{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: String, @Query("limit") limit: Int = 100): ResponseBody

    @GET("api/users/{id}/stories")
    suspend fun getUserStories(@Path("id") id: String): ResponseBody

    @POST("api/users/{id}/follow")
    suspend fun followUser(
        @Header("Authorization") auth: String?,
        @Path("id") id: String
    ): ResponseBody

    @DELETE("api/users/{id}/follow")
    suspend fun unfollowUser(
        @Header("Authorization") auth: String?,
        @Path("id") id: String
    ): ResponseBody

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") auth: String?,
        @Body payload: RequestBody
    ): ResponseBody

    @DELETE("api/user/jobs/{id}")
    suspend fun deleteJob(
        @Header("Authorization") auth: String?,
        @Path("id") id: String
    ): ResponseBody

    @GET("api/companies")
    suspend fun getAllCompaniesRaw(@Query("all") all: Boolean = true): ResponseBody

    @POST("api/companies")
    suspend fun createCompanyRaw(
        @Header("Authorization") auth: String?,
        @Body payload: RequestBody
    ): ResponseBody

    @POST("api/user/jobs")
    suspend fun createUserJob(
        @Header("Authorization") auth: String?,
        @Body payload: RequestBody
    ): ResponseBody

    @PUT("api/users/{userId}/jobs")
    suspend fun updateUserJob(
        @Header("Authorization") auth: String?,
        @Path("userId") userId: String,
        @Body payload: RequestBody
    ): ResponseBody

    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") auth: String?,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): ResponseBody

    @POST("api/notifications")
    suspend fun updateNotifications(
        @Header("Authorization") auth: String?,
        @Body payload: RequestBody
    ): ResponseBody

    @DELETE("api/notifications")
    suspend fun deleteNotification(
        @Header("Authorization") auth: String?,
        @Query("id") id: String
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://jobsreport.online/"

        fun create(tokenProvider: (() -> String?)? = null): JobsReportApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = okhttp3.Interceptor { chain ->
                val origReq = chain.request()
                val token = tokenProvider?.invoke()
                val reqBuilder = origReq.newBuilder()
                if (!token.isNullOrBlank()) {
                    val clean = token.removePrefix("Bearer ").removePrefix("bearer ").trim()
                    if (origReq.header("Authorization").isNullOrBlank()) {
                        reqBuilder.header("Authorization", "Bearer $clean")
                    }
                    if (origReq.header("Cookie").isNullOrBlank()) {
                        reqBuilder.header("Cookie", "jobsreport_token=$clean; token=$clean")
                    }
                }
                chain.proceed(reqBuilder.build())
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(JobsReportApi::class.java)
        }
    }
}
