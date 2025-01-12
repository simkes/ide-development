package ui

import ViewConfig
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import vfs.FileTreeNode
import kotlin.io.path.absolute

/**
 * Tree-like listing of files in a directory.
 * Draws files and directories as nodes in a scrollable lazy column.
 * Dir nodes expand/collapse upon click.
 *
 * It may be quite inefficient representation for more complex projects, but it works :)
 *
 * @param modifier Compose modifier to align the tree properly
 */
@Composable
fun FileTree(uiModel: UiModel, modifier: Modifier) = with(uiModel) {
    val rootNode = root.value!!
    val expandedNodes = remember { mutableStateListOf(rootNode) }
    LazyColumn(modifier = modifier) {
        Node(
            rootNode,
            1,
            isExpanded = {
                expandedNodes.contains(it)
            },
            switchExpanded = {
                if (expandedNodes.contains(it)) {
                    expandedNodes.remove(it)
                } else {
                    if (it.kind == FileTreeNode.TreeNodeKind.UNINITIALIZED) it.gatherContent(it)
                    expandedNodes.add(it)
                }
            },
        ) {
            emit { OpenFileInEditorEvent(it.path.absolute().toUri()) }
        }
    }
}

private fun LazyListScope.Nodes(
    nodes: List<FileTreeNode>,
    depth: Int,
    isExpanded: (FileTreeNode) -> Boolean,
    switchExpanded: (FileTreeNode) -> Unit,
    onFileClick: (FileTreeNode) -> Unit
) {
    nodes.forEach {
        Node(it, depth, isExpanded, switchExpanded, onFileClick)
    }
}

private fun LazyListScope.Node(
    node: FileTreeNode,
    depth: Int,
    isExpanded: (FileTreeNode) -> Boolean,
    switchExpanded: (FileTreeNode) -> Unit,
    onFileClick: (FileTreeNode) -> Unit
) {
    val modifier = if (node.kind != FileTreeNode.TreeNodeKind.FILE) {
        Modifier.clickable {
            switchExpanded(node)
        }
    } else {
        Modifier.clickable {
            onFileClick(node)
        }
    }
    item {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("New") { TODO() },
                ContextMenuItem("Copy") { TODO() },
                ContextMenuItem("Copy path") { TODO() },
                ContextMenuItem("Move") { TODO() },
                ContextMenuItem("Delete") { TODO() },
            )
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // spacer adds left padding to visualise tree-like structure
                Spacer(modifier = Modifier.width(8.dp * depth))
                when (node.kind) {
                    FileTreeNode.TreeNodeKind.FILE -> Icon(Icons.Outlined.Description, "", tint = ViewConfig.defaultTextColor, modifier = Modifier.height(15.dp).width(15.dp))
                    else -> Icon(Icons.Outlined.Folder, "", tint = ViewConfig.defaultTextColor, modifier = Modifier.height(15.dp).width(15.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    node.fileName.toString(),
                    modifier = modifier,
                    style = ViewConfig.defaultTextStyle.copy(fontFamily = FontFamily.Default),
                    color = ViewConfig.defaultTextColor,
                    fontSize = ViewConfig.bigFontSize
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
    if (isExpanded(node)) {
        Nodes(
            node.children.sortedBy { it.fileName },
            depth + 1,
            isExpanded,
            switchExpanded,
            onFileClick
        )
    }
}
