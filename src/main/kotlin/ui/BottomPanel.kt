package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import verticalDividerModifier

@Composable
fun BottomPanel(uiModel: UiModel, modifier: Modifier = Modifier) = with(uiModel) {
    Row(modifier = modifier) {
        Column {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Error, "")
            }
        }
        Divider(modifier = Modifier.verticalDividerModifier())
        LazyColumn {
            (1..10).forEach {
                item {
                    Text(it.toString())
                }
            }
        }
    }
}