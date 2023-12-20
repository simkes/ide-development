package ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.io.path.toPath

@Composable
fun EditorBar(uiModel: UiModel, modifier: Modifier) = with(uiModel) {
    Row(modifier = modifier) {
        docs.value.forEach {
            Row(modifier = Modifier.clickable {
                emit { OpenFileInEditorEvent(it.fileURI) }
            }.background(color = Color.LightGray).border(BorderStroke(Dp.Hairline, Color.Black)).padding(1.dp)) {
                Text(it.fileURI.toPath().fileName.toString())
                Icon(Icons.Default.Close, "", modifier = Modifier.clickable {
                    emit { CloseFileInEditorEvent(it.fileURI) }
                }.height(20.dp))
            }
        }
    }
}