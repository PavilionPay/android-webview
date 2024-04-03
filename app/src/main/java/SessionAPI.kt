import com.google.gson.annotations.SerializedName
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SessionAPI {
    @POST("gaming/mobility/sdk/api/patronsession/new")
    suspend fun createSession(
            @Header("Accept") accept: String = "application/json",
            @Header("Content-Type") contentType: String = "application/json",
            @Header("Authorization") auth: String = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYmYiOjE3MTIzMzgyNjYsImV4cCI6MTc0Mzg3NDI2NiwiaWF0IjoxNzEyMzM4MjY2LCJpc3MiOiI3YTAzYmZjYS1hZDgwLTQ0NjgtYmMzOS1kNjY0MjQ0ZDc0ZTUiLCJhdWQiOiJ2aXAtYXBpLWRldiJ9.6PnRE9Kc_vdbNL8NsdTKuZqpfdMCJCxmUvTMLlBLEic",
            @Body request: RequestBody
    ): SessionInfo
}

data class SessionInfo(
    @SerializedName("sessionId") val sessionId: String
)