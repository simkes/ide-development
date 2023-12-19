package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import highlighterColorToComposeColor
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalTextApi::class)
@Composable
fun EditorCanvas(
    uiModel: UiModel,
    modifier: Modifier
) = with(uiModel) {
    val textMeasurer = rememberTextMeasurer()
    val caretVisible = remember { caretVisible }

    val horizontalScrollState = rememberScrollState(0)
    val verticalScrollState = rememberScrollState(0)

    Box(modifier = modifier.onPreviewKeyEvent { handleKeyEvent(it) }) {
        Canvas(modifier = modifier.focusable(true)
            .clickable { requester.requestFocus() }
            .focusRequester(requester)
            .fillMaxSize()
            .scrollable(verticalScrollState, Orientation.Vertical)
            .scrollable(horizontalScrollState, Orientation.Horizontal)
        ) {
            text.let {
                val textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                val (colored, underlined) = viewModel.highlighters
                val highlighters = colored.map { highlighter ->
                    AnnotatedString.Range(
                        SpanStyle(
                            color = highlighterColorToComposeColor(highlighter.color),
                        ),
                        highlighter.startOffset,
                        highlighter.endOffset
                    )
                }
                val measuredText = textMeasurer.measure(
                    AnnotatedString(it, spanStyles = highlighters),
                    style = textStyle
                )
                val lines = it.split("\n")
                val (caretLine, caretPos) = viewModel.getCaret()
                val charHeight = measuredText.size.height / max(1, lines.size)
                val caretY = charHeight * caretLine.toFloat()
                var caretX = 0f
                if (lines.getOrNull(caretLine) != null) {
                    val lineStr = AnnotatedString(
                        lines[caretLine].substring(
                            0,
                            min(caretPos, lines[caretLine].length)
                        )
                    )
                    caretX = textMeasurer.measure(
                        lineStr,
                        style = textStyle
                    ).size.width.toFloat()
                }

                translate(
                    60f - horizontalScrollState.value,
                    60f - verticalScrollState.value
                ) {
                    // drawing line numbers
                    for ((index, _) in lines.withIndex()) {
                        val lineNumberString = AnnotatedString((index + 1).toString())
                        val lineNumberLayout = textMeasurer.measure(
                            lineNumberString,
                            style = textStyle.copy(color = Color.Gray)
                        )
                        drawText(
                            lineNumberLayout,
                            topLeft = Offset(-55f, (index * charHeight).toFloat())
                        )
                    }

                    drawText(measuredText)

                    if (caretVisible.value) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(caretX, caretY),
                            end = Offset(caretX, caretY + charHeight),
                            strokeWidth = 1f
                        )
                    }

                    underlined.forEach { highlighter ->
                        if (measuredText.size.width >= highlighter.endOffset) {
                            if (highlighter.underlinedAfterEndOffset) {
                                // drawing short red line after end offset
                                val y =
                                    measuredText.getLineBottom(measuredText.getLineForOffset(highlighter.endOffset))
                                val x = measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                val lineLength = 20f
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(x, y),
                                    end = Offset(x + lineLength, y),
                                    strokeWidth = 2f
                                )
                            } else {
                                val y =
                                    measuredText.getLineBottom(measuredText.getLineForOffset(highlighter.startOffset))
                                val x1 = measuredText.getHorizontalPosition(highlighter.startOffset, true)
                                val x2 = measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(x1, y),
                                    end = Offset(x2, y),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}