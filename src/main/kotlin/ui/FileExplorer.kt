package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FileExplorer(uiModel: UiModel, modifier: Modifier = Modifier) = with(uiModel) {
    Column(modifier = modifier) {
        FileTree(uiModel, modifier = modifier)
    }
}