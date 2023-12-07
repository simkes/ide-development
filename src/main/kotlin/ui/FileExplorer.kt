package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FileExplorer(uiModel: UIModel, modifier: Modifier) = with(uiModel) {
    Column(modifier = modifier.width(200.dp)) {
        FileTree(uiModel, modifier = modifier.weight(9f))
        Button(modifier = modifier.weight(1f), onClick = {
            fileChooseDialogVisible.value = true
        }) {
            Text("Choose directory")
        }
    }
}