import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import ui.*

class App {
    @Composable
    @Preview
    fun run(uiModel: UiModel) {
        LaunchedEffect(Unit) {
            while (true) {
                uiModel.caretVisible.value = !uiModel.caretVisible.value
                delay(500)
            }
        }

        MaterialTheme {
            if (uiModel.fileChooseDialogVisible.value) {
                FileChooserDialog(uiModel)
            }

            if (uiModel.noSourceDirectoryChosenDialogVisible.value) {
                NoSourceDirectoryChosenDialog(uiModel)
                // TODO: dialog does not disappear when new choosing dialog appears
            }

            if (uiModel.root.value != null) {
                Row {
                    FileExplorer(uiModel, Modifier)
                    Column(modifier = Modifier.weight(2f)) {
                        EditorBar(
                            uiModel,
                            Modifier
                        )
                        EditorCanvas(
                            uiModel,
                            Modifier
                        )
                    }
                }
            } else if (!uiModel.fileChooseDialogVisible.value && !uiModel.noSourceDirectoryChosenDialogVisible.value) {
                assert(false)
            }
        }
    }
}

fun main() {
    val app = App()
    application {
        Window(onCloseRequest = {
            exitApplication()
        }) {
            app.run(UiModel(this@application, GlobalScope))
        }
    }
}