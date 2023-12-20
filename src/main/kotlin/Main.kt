import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import ui.*

fun main() {
    application {
        App.init(this)
        Window(onCloseRequest = {
            exitApplication()
        }) {
            App.run()
        }
    }
}