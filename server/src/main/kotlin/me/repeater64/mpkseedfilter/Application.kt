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
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.repeater64.mpkseedfilter.dto.request.SeedsRequest
import me.repeater64.mpkseedfilter.dto.request.SeedsRequestResponse
import me.repeater64.mpkseedfilter.filtering.database.LoadedNumAccessesDatabase
import me.repeater64.mpkseedfilter.filtering.database.LoadedSeedDatabase
import me.repeater64.mpkseedfilter.filtering.database.NumAccessesDatabase
import me.repeater64.mpkseedfilter.filtering.database.SeedDatabase
import me.repeater64.mpkseedfilter.requests.RequestHandler
import kotlin.time.Duration.Companion.minutes

val databaseMutex = Mutex()
var anyAccessesSinceLastSave = false

fun main() {
    LoadedSeedDatabase.loadFromDisk()
    LoadedNumAccessesDatabase.loadFromDisk()

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    install(ContentNegotiation) {
        json(JSON)
    }

    // Configure CORS so GitHub Pages can talk to this server
    install(CORS) {
        allowHost("localhost:8081", schemes = listOf("http")) // TODO replace with yourusername.github.io or smth, and schemes with https
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
    }

    // Periodic save of num accesses database
    launch {
        while (isActive) {
            delay(5.minutes)

            // Requests will wait whilst this save runs. Should be fast.
            if (anyAccessesSinceLastSave) {
                anyAccessesSinceLastSave = false
                databaseMutex.withLock {
                    LoadedNumAccessesDatabase.saveToDisk()
                }
            }
        }
    }

    // Detect shutdown to save numAccesses
    monitor.subscribe(ApplicationStopping) {
        println("Server stopping, saving NumAccesses database...")

        runBlocking {
            if (anyAccessesSinceLastSave) {
                anyAccessesSinceLastSave = false
                databaseMutex.withLock {
                    LoadedNumAccessesDatabase.saveToDisk()
                }
            }
        }
        println("Final save complete.")
    }

    routing {
        post("/api/get-seeds") {
            val requestData = call.receive<SeedsRequest>()

            val responseData = databaseMutex.withLock {
                RequestHandler.handleRequest(requestData)
                anyAccessesSinceLastSave = true
            }

            call.respond(responseData)
        }
    }
}