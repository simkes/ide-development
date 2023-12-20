package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
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
            App.workingDirectoryPath = dialog.selectedFile.toPath()
            App.workingDirectory = App.vfs.getFile(App.workingDirectoryPath!!.toUri())
            App.uiModel.root.value = App.vfs.listDirectory(App.workingDirectoryPath!!)
        } else if (App.workingDirectoryPath == null) {
            noSourceDirectoryChosenDialogVisible.value = true
        }
        fileChooseDialogVisible.value = false
    }
}

@Composable
fun FileEntryDialog(modifier: Modifier = Modifier) = with(App.uiModel) {
    if (fileEntryDialogVisible.value) {
        Dialog(
            dispose = {},
            create = { ComposeDialog() }
        ) {
            Column(modifier = modifier.width(200.dp).height(200.dp)) {
                TextField("Enter name of file", onValueChange = {
                    input = it
                })
            }
            Row {
                Button(onClick = {
                    fileEntryDialogVisible.value = false
                }) {
                    Text("OK")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    input = ""
                    fileEntryDialogVisible.value = false
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}