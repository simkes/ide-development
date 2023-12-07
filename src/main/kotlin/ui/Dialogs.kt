package ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import java.io.File
import javax.swing.JFileChooser

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ApplicationScope.NoSourceDirectoryChosenDialog(onChooseAgain: () -> Unit) = AlertDialog(
    onDismissRequest = {
        exitApplication()
    },
    buttons = {
        Row {
            Button(
                onClick = {
                    onChooseAgain()
                }
            ) {
                Text("Choose again")
            }
            Button(
                onClick = {
                    exitApplication()
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
fun fileDialog(fileSelected: (File) -> Unit, fileNotSelected: () -> Unit) {
    val dialog = JFileChooser()
    dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    dialog.isVisible = true
    dialog.showOpenDialog(null)
    if (dialog.selectedFile != null) {
        fileSelected(dialog.selectedFile)
    } else {
        fileNotSelected()
    }
}