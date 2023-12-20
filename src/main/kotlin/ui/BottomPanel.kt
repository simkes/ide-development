package ui

import ViewConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import highlighting.Highlighter
import verticalDividerModifier

data class ErrorStringHolder(
    val locationString: AnnotatedString,
    val location: Pair<Int, Int>,
    val errorString: AnnotatedString
)

@Composable
fun BottomPanel(uiModel: UiModel, modifier: Modifier = Modifier) = with(uiModel) {
    viewModel.text.value
    // this is a crutch:
    // need recomposition, but require it to happen only when both text and highlighters are updated.
    // If both are mutableStates, one of them can be not updated when the other one calls recomposition
    val errorMessages = viewModel.highlighters.let {
        val mapToErrorMsgs: List<Highlighter>.() -> List<ErrorStringHolder> = {
            this.filter { it.errorMessage != null }.map { highlighter ->
                val lineNum = viewModel.getLineNumber(highlighter.startOffset)
                val lineStart = viewModel.getLineStart(lineNum)
                val positionString = AnnotatedString(
                    "Line ${lineNum + 1}, pos ${highlighter.startOffset - lineStart}",
                    spanStyle = SpanStyle(
                        color = ViewConfig.errorLinkColor,
                        fontFamily = ViewConfig.fontFamily,
                        fontSize = ViewConfig.defaultFontSize,
                        textDecoration = TextDecoration.Underline
                    )
                )
                val errorString = AnnotatedString(
                    ": ${highlighter.errorMessage}", spanStyle = SpanStyle(
                        color = ViewConfig.defaultTextColor,
                        fontFamily = ViewConfig.fontFamily,
                        fontSize = ViewConfig.defaultFontSize
                    )
                )
                ErrorStringHolder(positionString, Pair(lineNum, highlighter.startOffset - lineStart), errorString)
            }
        }
        it.first.mapToErrorMsgs() + it.second.mapToErrorMsgs()
    }
    Row(modifier = modifier.background(color = ViewConfig.lightMainBackgroundColor)) {
        Column  {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Error, "", tint = ViewConfig.errorIconColor)
            }
        }
        Divider(modifier = Modifier.verticalDividerModifier())
        LazyColumn {
            errorMessages.forEach { (locationString, location, errorString) ->
                item {
                    Row {
                        ClickableText(locationString) {
                            uiModel.emit { SetCaretEvent(location.first, location.second) }
                        }
                        Text(errorString)
                    }
                }
            }
        }
    }
}