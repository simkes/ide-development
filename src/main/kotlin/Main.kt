@file:OptIn(
    ExperimentalTextApi::class,
)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import editor.EditorViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import ui.*

class App {
    @Composable
    @Preview
    fun run(uiModel: UIModel) {
        val fileChooseDialogVisible = remember { mutableStateOf(true) }
        val noSourceDirectoryChosenDialogVisible = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                uiModel.caretVisible.value = !uiModel.caretVisible.value
                delay(500)
            }
        }

        MaterialTheme {
            if (fileChooseDialogVisible.value) {
                fileDialog(fileSelected = {
                    uiModel.root.value = uiModel.viewModel.virtualFileSystem.listDirectory(it.toPath())
                    fileChooseDialogVisible.value = false
                }, fileNotSelected = {
                    if (uiModel.root.value == null) {
                        noSourceDirectoryChosenDialogVisible.value = true
                    }
                    fileChooseDialogVisible.value = false
                })
            }

            if (noSourceDirectoryChosenDialogVisible.value) {
                uiModel.applicationScope.NoSourceDirectoryChosenDialog {
                    noSourceDirectoryChosenDialogVisible.value = false
                    fileChooseDialogVisible.value = true
                }
            } else {
                Row {
                    FileExplorer(uiModel, Modifier)
                    Column(modifier = Modifier.weight(2f)) {
                        EditorBar(
                            uiModel,
                            Modifier.weight(1f)
                        )
                        EditorCanvas(
                            uiModel,
                            Modifier.weight(3f)
                        )
                    }
                }
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
            app.run(UIModel(this@application, GlobalScope))
        }
    }
}