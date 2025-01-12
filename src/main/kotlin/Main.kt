import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        App.init(this)
        Window(onCloseRequest = {
            exitApplication()
        },
            title = "YAIDE") {
            App.run()
        }
    }
}