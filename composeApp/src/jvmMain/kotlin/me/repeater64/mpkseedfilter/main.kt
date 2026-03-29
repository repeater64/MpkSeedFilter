package me.repeater64.mpkseedfilter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MPK Seed Filter",
    ) {
        App()
    }
}