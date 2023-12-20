import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            FileChooserDialog(uiModel)

            if (uiModel.noSourceDirectoryChosenDialogVisible.value) {
                NoSourceDirectoryChosenDialog(uiModel)
                // TODO: dialog does not disappear when new choosing dialog appears
            }

            if (uiModel.root.value != null) {
                Column {
                    TopBar(uiModel, Modifier)
                    Divider()
                    Row(modifier = Modifier.weight(3f)) {
                        FileExplorer(uiModel, Modifier.width(150.dp))
                        Divider(modifier = Modifier.verticalDividerModifier())
                        Column(modifier = Modifier) {
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
                    Divider()
                    BottomPanel(uiModel, modifier = Modifier.weight(1f))
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