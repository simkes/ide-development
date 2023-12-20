package ui

import ViewConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.io.path.toPath

@Composable
fun EditorBar(uiModel: UiModel, modifier: Modifier) = with(uiModel) {
    Row(modifier = modifier) {
        docs.value.forEach {
            Row(modifier = Modifier.clickable {
                emit { OpenFileInEditorEvent(it.fileURI) }
            }.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(it.fileURI.toPath().fileName.toString(), color = ViewConfig.defaultTextColor, fontSize = ViewConfig.defaultFontSize)
                Icon(Icons.Default.Close, "", modifier = Modifier.clickable {
                    emit { CloseFileInEditorEvent(it.fileURI) }
                }.height(15.dp).width(15.dp), tint = ViewConfig.defaultTextColor)
            }
        }
    }
}