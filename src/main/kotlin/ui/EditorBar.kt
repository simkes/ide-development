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
fun EditorBar(openedDocuments: List<Document>, onClick: (Document) -> Unit, onClose: (Document) -> Unit, modifier: Modifier) {
    Row(modifier = modifier) {
        println("Abobus: ${openedDocuments.size}")
        openedDocuments.forEach {
            Row(modifier = Modifier.clickable {
                onClick(it)
            }) {
                Text(it.fileURI.toPath().fileName.toString())
                Icon(Icons.Default.Close, "", modifier = Modifier.clickable {
                    onClose(it)
                })
            }
        }
    }
}