package com.mievento.ideal.data.api

import com.mievento.ideal.data.models.*
import com.mievento.ideal.utils.TokenManager
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/verify-phone")
    suspend fun verifyPhone(
        @Header("Authorization") token: String,
        @Body request: VerifyPhoneRequest
    ): Response<AuthResponse>

    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<AuthResponse>

    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<AuthResponse>

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<AuthResponse>

    // Event endpoints
    @POST("events")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body request: CreateEventRequest
    ): Response<EventResponse>

    @GET("events")
    suspend fun getEvents(@Header("Authorization") token: String): Response<EventListResponse>

    // 🔥 NUEVO: Obtener eventos con filtros
    @GET("events")
    suspend fun getEventsWithFilters(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<EventListResponse>

    @GET("events/{id}")
    suspend fun getEvent(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<EventResponse>

    // 🔥 NUEVO: Alias para getEvent (compatibilidad con EventRepository)
    suspend fun getEventById(id: Int): EventResponse {
        // Este método será implementado en el companion object
        throw NotImplementedError("Use getEvent instead")
    }

    @PUT("events/{id}")
    suspend fun updateEvent(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateEventRequest
    ): Response<EventResponse>

    @DELETE("events/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<EventResponse>

    // 🔥 NUEVO: Actualizar estado del evento (CLAVE PARA PUBLICAR)
    @PATCH("events/{id}/status")
    suspend fun updateEventStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusData: Map<String, String>
    ): Response<EventResponse>

    // Event Summary endpoint
    @GET("events/{id}/summary")
    suspend fun getEventSummary(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<EventSummaryResponse>

    // 🔥 NUEVO: Alertas de presupuesto
    @GET("events/{id}/budget-alert")
    suspend fun getBudgetAlert(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Map<String, Any>>

    // Guest endpoints
    @POST("guests/event/{event_id}")
    suspend fun createGuest(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: CreateGuestRequest
    ): Response<GuestResponse>

    @GET("guests/event/{event_id}")
    suspend fun getGuests(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<GuestResponse>

    @PUT("guests/{id}")
    suspend fun updateGuest(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateGuestRequest
    ): Response<GuestResponse>

    @DELETE("guests/{id}")
    suspend fun deleteGuest(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<GuestResponse>

    // Update guest confirmation
    @PATCH("guests/{id}/confirmation")
    suspend fun updateGuestConfirmation(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): Response<GuestResponse>

    // Task endpoints
    @POST("tasks/event/{event_id}")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: CreateTaskRequest
    ): Response<TaskResponse>

    @GET("tasks/event/{event_id}")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<TaskResponse>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateTaskRequest
    ): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<TaskResponse>

    // Update task status
    @PATCH("tasks/{id}/status")
    suspend fun updateTaskStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: Map<String, String>
    ): Response<TaskResponse>

    // Provider endpoints
    @POST("providers/event/{event_id}")
    suspend fun createProvider(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: CreateProviderRequest
    ): Response<ProviderResponse>

    @GET("providers/event/{event_id}")
    suspend fun getProviders(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<ProviderResponse>

    @PUT("providers/{id}")
    suspend fun updateProvider(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateProviderRequest
    ): Response<ProviderResponse>

    @DELETE("providers/{id}")
    suspend fun deleteProvider(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ProviderResponse>

    // Expense endpoints
    @POST("expenses/event/{event_id}")
    suspend fun createExpense(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: CreateExpenseRequest
    ): Response<ExpenseResponse>

    @GET("expenses/event/{event_id}")
    suspend fun getExpenses(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<ExpenseResponse>

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateExpenseRequest
    ): Response<ExpenseResponse>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ExpenseResponse>

    // Reschedule endpoints
    @POST("reschedule/event/{event_id}")
    suspend fun createRescheduleRequest(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: CreateRescheduleRequest
    ): Response<RescheduleResponse>

    @GET("reschedule/user")
    suspend fun getUserRescheduleRequests(
        @Header("Authorization") token: String
    ): Response<RescheduleResponse>

    @GET("reschedule/admin")
    suspend fun getAdminRescheduleRequests(
        @Header("Authorization") token: String
    ): Response<RescheduleResponse>

    @PATCH("reschedule/{id}/respond")
    suspend fun respondToRescheduleRequest(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body response: Map<String, String>
    ): Response<RescheduleResponse>

    // Image endpoints
    @Multipart
    @POST("images/events/{event_id}")
    suspend fun uploadEventImage(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Part image: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<EventImageResponse>

    @GET("images/events/{event_id}")
    suspend fun getEventImages(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<EventImageResponse>

    // Notification endpoints
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<NotificationResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<NotificationResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadNotificationCount(
        @Header("Authorization") token: String
    ): Response<NotificationResponse>

    // Participation endpoints
    @GET("participation/available-events")
    suspend fun getAvailableEvents(
        @Header("Authorization") token: String
    ): Response<ParticipationResponse>

    @POST("participation/events/{event_id}/request")
    suspend fun requestParticipation(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Body request: RequestParticipationRequest
    ): Response<ParticipationResponse>

    @GET("participation/my-participations")
    suspend fun getMyParticipations(
        @Header("Authorization") token: String
    ): Response<ParticipationResponse>

    @GET("participation/events/{event_id}/participants")
    suspend fun getEventParticipants(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int
    ): Response<ParticipationResponse>

    @PATCH("participation/events/{event_id}/users/{user_id}/respond")
    suspend fun respondToParticipation(
        @Header("Authorization") token: String,
        @Path("event_id") eventId: Int,
        @Path("user_id") userId: Int,
        @Body request: RespondParticipationRequest
    ): Response<ParticipationResponse>

    companion object {
        private const val BASE_URL = "http://3.208.254.174/api/"

        fun create(tokenManager: TokenManager): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}