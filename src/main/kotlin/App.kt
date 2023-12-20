import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import editor.EditorViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import ui.*
import vfs.VirtualFile
import vfs.VirtualFileSystemImpl
import java.nio.file.Path

object App {
    var workingDirectoryPath: Path? = null
    lateinit var workingDirectory: VirtualFile
    val vfs = VirtualFileSystemImpl(GlobalScope)
    val editorViewModel = EditorViewModel(GlobalScope)

    lateinit var uiModel: UiModel
    fun init(applicationScope: ApplicationScope) {
        uiModel = UiModel(applicationScope, editorViewModel, GlobalScope)
    }

    @Composable
    @Preview
    fun run() {
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

            FileEntryDialog()

            if (uiModel.root.value != null) {
                Column {
                    TopBar(uiModel, Modifier)
                    Divider()
                    Row(modifier = Modifier.weight(3f)) {
                        FileExplorer(uiModel, Modifier.width(150.dp))
                        Divider(modifier = Modifier.verticalDividerModifier())
                        Column(modifier = Modifier) {
                            if (uiModel.isEditorOpened) {
                                EditorBar(
                                    uiModel,
                                    Modifier
                                )
                                EditorCanvas(
                                    uiModel,
                                    Modifier
                                )
                            } else {
                                EmptyEditor(Modifier.fillMaxSize())
                            }
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