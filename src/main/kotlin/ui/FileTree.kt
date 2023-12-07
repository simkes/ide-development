package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun FileTree(uiModel: UIModel, modifier: Modifier) = with(uiModel) {
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
    val modifier = if (node.kind == FileTreeNode.TreeNodeKind.DIRECTORY) {
        Modifier.clickable {
            switchExpanded(node)
        }
    } else {
        Modifier.clickable {
            onFileClick(node)
        }
    }
    item {
        Row {
            // spacer adds left padding to visualise tree-like structure
            Spacer(modifier = Modifier.width(8.dp * depth))
            when (node.kind) {
                FileTreeNode.TreeNodeKind.DIRECTORY -> Icon(Icons.Default.Folder, "")
                FileTreeNode.TreeNodeKind.FILE -> Icon(Icons.Default.Description, "")
            }
            Text(
                node.fileName.toString(),
                modifier = modifier
            )
        }
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
