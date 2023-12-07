package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoSourceDirectoryChosenDialog(uiModel: UIModel) = AlertDialog(
    onDismissRequest = {
        uiModel.applicationScope.exitApplication()
    },
    buttons = {
        Row {
            Button(
                onClick = {
                    uiModel.noSourceDirectoryChosenDialogVisible.value = false
                    uiModel.fileChooseDialogVisible.value = true
                }
            ) {
                Text("Choose again")
            }
            Button(
                onClick = {
                    uiModel.applicationScope.exitApplication()
                }
            ) {
                Text("Exit")
            }
        }
    },
    text = {
        Text("You did not chose the initial directory for your project. Please choose again or exit the IDE.")
    },
    title = {
        Text("Project directory is not specified")
    }
)

@Composable
fun FileChooserDialog(uiModel: UIModel) {
    val dialog = JFileChooser()
    dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    dialog.isVisible = true
    dialog.showOpenDialog(null)
    if (dialog.selectedFile != null) {
        uiModel.root.value = uiModel.viewModel.virtualFileSystem.listDirectory(dialog.selectedFile.toPath())
    } else if (uiModel.root.value == null) {
        uiModel.noSourceDirectoryChosenDialogVisible.value = true
    }
    uiModel.fileChooseDialogVisible.value = false
}