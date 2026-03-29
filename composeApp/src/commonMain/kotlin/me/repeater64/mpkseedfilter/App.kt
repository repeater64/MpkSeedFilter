package me.repeater64.mpkseedfilter

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import me.repeater64.mpkseedfilter.dto.bastion.BastionIndexedByInfo
import me.repeater64.mpkseedfilter.dto.bastion.BastionType
import me.repeater64.mpkseedfilter.dto.bastion.ramparts.TreasureRamparts
import me.repeater64.mpkseedfilter.dto.request.BastionRequestInfo
import me.repeater64.mpkseedfilter.dto.request.SeedType
import me.repeater64.mpkseedfilter.dto.request.SeedsRequest


@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    var serverResponse by remember { mutableStateOf<String>("Waiting for action...") }

    Column {
        Text(text = serverResponse)

        Button(onClick = {
            coroutineScope.launch {
                try {
                    serverResponse = "Loading..."
                    val response = fetchSeeds(SeedsRequest(SeedType.BASTION, "someUser", BastionRequestInfo(BastionIndexedByInfo(BastionType.TREASURE, TreasureRamparts))))
                    serverResponse = response.seeds.joinToString(", ") { it.seed.toString() } + "\nAmount: ${response.seeds.size}"
                } catch (e: Exception) {
                    serverResponse = "Error: ${e.message}"
                }
            }
        }) {
            Text("Send Request to Server")
        }
    }
}