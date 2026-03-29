package me.repeater64.mpkseedfilter

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.*
import io.ktor.http.*
import me.repeater64.mpkseedfilter.dto.request.SeedsRequest
import me.repeater64.mpkseedfilter.dto.request.SeedsRequestResponse
import me.repeater64.mpkseedfilter.filtering.database.LoadedNumAccessesDatabase
import me.repeater64.mpkseedfilter.filtering.database.LoadedSeedDatabase
import me.repeater64.mpkseedfilter.filtering.database.NumAccessesDatabase
import me.repeater64.mpkseedfilter.filtering.database.SeedDatabase

fun main() {
    LoadedSeedDatabase.loadFromDisk()
    LoadedNumAccessesDatabase.loadFromDisk()

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    // Configure CORS so GitHub Pages can talk to this server
    install(CORS) {
        allowHost("localhost:8081", schemes = listOf("http")) // TODO replace with yourusername.github.io or smth, and schemes with https
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
    }

    routing {
        post("/api/get-seeds") {
            val requestData = call.receive<SeedsRequest>()

            println("Received request: $requestData")
            // TODO process request properly

            call.respond(SeedsRequestResponse(emptyList()))
        }
    }
}