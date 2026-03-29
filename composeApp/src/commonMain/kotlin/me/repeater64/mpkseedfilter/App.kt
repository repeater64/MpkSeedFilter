package me.repeater64.mpkseedfilter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch

import mpkseedfilter.composeapp.generated.resources.Res
import mpkseedfilter.composeapp.generated.resources.compose_multiplatform

// Create this once and reuse it
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun fetchServerData(input: String, count: Int): MyResponse {
    // Note: Change this URL to wherever your Ktor server is actually hosted!
    // If testing locally, it might be "http://localhost:8080/api/do-work"
    val serverUrl = "http://localhost:8080/api/do-work"

    return httpClient.post(serverUrl) {
        contentType(ContentType.Application.Json)
        setBody(MyRequest(inputData = input, count = count))
    }.body() // This automatically deserializes the JSON back into MyResponse
}

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var serverResponse by remember { mutableStateOf<String>("Waiting for action...") }

    Column {
        Text(text = serverResponse)

        Button(onClick = {
            // Launch a coroutine to do the network work without freezing the UI
            coroutineScope.launch {
                try {
                    serverResponse = "Loading..."
                    val response = fetchServerData("Hello Server", 5)
                    serverResponse = response.resultMessage
                } catch (e: Exception) {
                    serverResponse = "Error: ${e.message}"
                }
            }
        }) {
            Text("Send Request to Server")
        }
    }
}