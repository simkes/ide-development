package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import editor.Document
import kotlin.io.path.toPath

@Composable
fun EditorBar(uiModel: UIModel, modifier: Modifier) {
    Row(modifier = modifier) {
        uiModel.docs.value.forEach {
            Row(modifier = Modifier.clickable {
                uiModel.emit { OpenFileInEditorEvent(it.fileURI) }
            }) {
                Text(it.fileURI.toPath().fileName.toString())
                Icon(Icons.Default.Close, "", modifier = Modifier.clickable {
                    uiModel.emit { CloseFileInEditorEvent(it.fileURI) }
                })
            }
        }
    }
}