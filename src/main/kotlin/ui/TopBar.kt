package ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(model: UiModel, modifier: Modifier) = with(model) {
    Row(modifier = modifier) {
        TopBarEntry("File") {
            DropdownMenuItem(onClick = { fileChooseDialogVisible.value = true }) {
                Text("Change Working Directory")
            }
            DropdownMenuItem(onClick = { /* Handle menu item click */ }) {
                Text("New File")
            }
        }

        TopBarEntry("Edit") {
            DropdownMenuItem(onClick = { }) {
                Text("Undo")
            }
            DropdownMenuItem(onClick = { }) {
                Text("Redo")
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TopBarEntry(
    name: String,
    modifier: Modifier = Modifier,
    items: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val hoverModifier: Modifier.() -> Modifier = {
        this
            .onPointerEvent(PointerEventType.Enter) { isExpanded = true }
            .onPointerEvent(PointerEventType.Exit) { isExpanded = false }
    }

    Text(
        name,
        modifier = modifier.padding(horizontal = 4.dp).hoverModifier()

    )

    if (isExpanded) {
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.hoverModifier(),
            content = items
        )
    }
}