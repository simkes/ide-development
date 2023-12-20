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
fun NoSourceDirectoryChosenDialog(uiModel: UiModel) = with(uiModel) {
    AlertDialog(
        onDismissRequest = {
            applicationScope.exitApplication()
        },
        buttons = {
            Row {
                Button(
                    onClick = {
                        noSourceDirectoryChosenDialogVisible.value = false
                        fileChooseDialogVisible.value = true
                    }
                ) {
                    Text("Choose again")
                }
                Button(
                    onClick = {
                        applicationScope.exitApplication()
                    }
                ) {
                    Text("Exit")
                }
            }
        },
        text = {
            Text("You did not choose the initial directory for your project. Please choose again or exit the IDE.")
        },
        title = {
            Text("Project directory is not specified")
        }
    )
}

@Composable
fun FileChooserDialog(uiModel: UiModel) = with(uiModel) {
    if (fileChooseDialogVisible.value) {
        val dialog = JFileChooser()
        dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialog.showOpenDialog(null)
        if (dialog.selectedFile != null) {
            root.value = App.vfs.listDirectory(dialog.selectedFile.toPath())
        } else if (root.value == null) {
            noSourceDirectoryChosenDialogVisible.value = true
        }
        fileChooseDialogVisible.value = false
    }
}
