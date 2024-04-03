package com.pavilion.websdktest

import SessionAPI
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.Keep
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to allow remote debugging of the WebView through a Chromium browser on this machine
        WebView.setWebContentsDebuggingEnabled(true)

        setContent {
            SampleView()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SampleView() {
        var showingWebview by remember { mutableStateOf(WhichScreen.Launcher) }
        val padding = 20.dp
        
        // Use a simple Crossfade animation to toggle between the views;
        // a more complex app could use some form of navigation or modal.
        Crossfade(
            targetState = showingWebview
        ) { isShowing ->
            when (isShowing) {
                WhichScreen.Launcher ->
                    // A basic view to make it easy to stop and relaunch the WebView to try another session
                    Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Native Launcher View", fontSize = 30.sp)

                        Spacer(modifier = Modifier.size(padding))

                        Text("VIP Preferred", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Button(onClick = { showingWebview = WhichScreen.ExistingUser }) {
                            Text("Launch Webview for Existing User")
                        }
                        Button(onClick = { showingWebview = WhichScreen.NewUser }) {
                            Text("Launch Webview for New User")
                        }

                        Spacer(modifier = Modifier.size(padding))

                        Text("VIP Online", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Button(onClick = { showingWebview = WhichScreen.VIPOExistingUser }) {
                            Text("Launch Webview for Existing User")
                        }
                        Button(onClick = { showingWebview = WhichScreen.VIPONewUser }) {
                            Text("Launch Webview for New User")
                        }
                    }

                WhichScreen.ExistingUser -> WebViewScreen(initialUrl = "file:///android_asset/sessionCreationExistingUser.html") { showingWebview = WhichScreen.Launcher }
                WhichScreen.NewUser -> WebViewScreen(initialUrl = "file:///android_asset/sessionCreationNewUser.html") { showingWebview = WhichScreen.Launcher }
                WhichScreen.VIPOExistingUser -> WebViewScreen(initialUrl ="file:///android_asset/vipOnlineExistingUser.html") { showingWebview = WhichScreen.Launcher }
                WhichScreen.VIPONewUser -> WebViewScreen(initialUrl = "file:///android_asset/vipOnlineNewUser.html") { showingWebview = WhichScreen.Launcher }
            }
        }
    }

    @Preview
    @Composable
    fun SampleViewPreview() {
        SampleView()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WebViewScreen(initialUrl: String, goBack: () -> Unit) {
        // Display a simple Scaffold view with a top bar and a WebView.
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("") },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = Color(0xFF0000FF),
                        titleContentColor = Color(0xFFFFFFFF)
                    ),
                    modifier = Modifier,
                    navigationIcon = {
                        IconButton(onClick = { goBack() }) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFFFFFFFF))
                        }
                    }
                )
            },
            content = { pv ->

                // This app uses a local file that aids in VIP session creation.
                // A production app may create the VIP session in another fashion
                // and launch the VIP SDK directly in the WebView instead.
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            webViewClient = object: WebViewClient() {
                                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {

                                    request.url.path?.let {
                                        if (!it.contains(".chunk.")
                                            && !it.contains(".woff2")
                                            && !it.contains(".ttf")
                                            && !it.contains(".svg")
                                            && !it.contains(".css")
                                            && !it.contains(".js")
                                            && !it.contains(".ico")
                                            && !it.contains(".png")
                                            && !it.contains("css")
                                            && !it.contains("workflow/event")
                                            && !it.contains("sentry")
                                            && !it.contains("gtag")
                                            && !it.contains("g/collect")
                                        ) {

                                            println(request.method + " " + request.url)
                                        }
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }
                            }

                            // These settings are required for the VIP SDK to function
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            addJavascriptInterface(JavaScriptInterface(this), "jsi")
                            loadUrl(initialUrl)
                        }
                    }, modifier = Modifier
                        .padding(pv)
                        .fillMaxSize(),
                    update = {
                        // Reload the base page whenever the WebView is "relaunched"
                        it.loadUrl(initialUrl)
                    })
            }
        )
    }
}

/**
 * Used only for local session creation within the app; not required for the VIP SDK, which will create its
 * session elsewhere.
 */
class JavaScriptInterface(private val webView: WebView) {
    private val coroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    @JavascriptInterface
    @Keep
    fun giveMeASessionIdNewUser(apiUrl: String) {
        Log.d("WebSDKTester", "Trace A")

        val api = Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(
                    OkHttpClient().newBuilder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(HttpLoggingInterceptor().apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            })
                            .build()
                )
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build().create(SessionAPI::class.java)

        coroutineScope.launch {
            try {
                val sessionInfo = api.createSession(
                    request =
                    """
                    {
                        "PatronId": "1224e6b0-7560-4350-9106-d69403c4f115",
                        "FirstName": "John",
                        "MiddleInitial": "",
                        "LastName": "Mark",
                        "DateOfBirth": "11/14/1990",
                        "Email": "John.Mark@testorg.com",
                        "MobilePhone": "3023492103",
                        "StreetName": "6900 Preston Rd",
                        "City": "Plano",
                        "State": "TX",
                        "Zip": "75024",
                        "Country": "USA",
                        "IdType": "SS",
                        "IdNumber": "333445555",
                        "IdState": "TX",
                        "transactionId": "MOCK231218T${(100000..999999).random()}",
                        "transactionAmount": 10,
                        "RoutingNumber": "061000052",
                        "AccountNumber": "",
                        "walletBalance": "1000",
                        "RemainingDailyDeposit": "1000",
                        "productType": 0,
                        "threatMetrixForceFail": false
                    }
                """.trimIndent().toRequestBody()
                )
                webView.post {
                    webView.evaluateJavascript("GotSessionId('${sessionInfo.sessionId}')", null)
                }
            } catch (e: Exception) {
                Log.d("WebSDKTester", "Something went wrong: $e")
            }
        }

    }
}

enum class WhichScreen {
    Launcher,
    ExistingUser,
    NewUser,
    VIPOExistingUser,
    VIPONewUser
}