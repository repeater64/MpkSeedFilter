package me.repeater64.mpkseedfilter

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import me.repeater64.mpkseedfilter.dto.request.SeedsRequest
import me.repeater64.mpkseedfilter.dto.request.SeedsRequestResponse

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(JSON)
    }
}

suspend fun fetchSeeds(request: SeedsRequest): SeedsRequestResponse {
    val serverUrl = "http://localhost:8080/api/get-seeds" // TODO change to actual backend URL

    return httpClient.post(serverUrl) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()
}