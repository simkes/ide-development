package ui

import ViewConfig
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import highlighterColorToComposeColor
import org.jetbrains.skia.Region
import javax.swing.text.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun EditorCanvas(
    uiModel: UiModel,
    modifier: Modifier = Modifier
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
            .scrollable(verticalScrollState, Orientation.Vertical, reverseDirection = true)
            .scrollable(horizontalScrollState, Orientation.Horizontal)
            .background(color = ViewConfig.darkBackgroundColor)
        ) {
            this.clipRect {
                text.let {
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
                        style = ViewConfig.defaultTextStyle
                    )
                    val lines = it.split("\n")
                    val (caretLine, caretPos) = viewModel.getCaret()
                    val charHeight = measuredText.getLineBottom(0)
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
                            style = ViewConfig.defaultTextStyle
                        ).size.width.toFloat()

                        translate(
                            60f - horizontalScrollState.value,
                            0f - verticalScrollState.value
                        ) {
                            // drawing line numbers
                            for ((index, _) in lines.withIndex()) {
                                val lineNumberString = AnnotatedString((index + 1).toString())
                                val lineNumberLayout = textMeasurer.measure(
                                    lineNumberString,
                                    style = ViewConfig.defaultTextStyle.copy(color = Color.Gray)
                                )
                                drawText(
                                    lineNumberLayout,
                                    topLeft = Offset(-55f, (index * charHeight))
                                )
                            }

                            drawText(measuredText)

                            if (caretVisible.value) {
                                drawLine(
                                    color = Color.White,
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
                                            measuredText.getLineBottom(
                                                measuredText.getLineForOffset(
                                                    highlighter.endOffset
                                                )
                                            )
                                        val x =
                                            measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                        val lineLength = 20f
                                        drawLine(
                                            color = Color.Red,
                                            start = Offset(x, y),
                                            end = Offset(x + lineLength, y),
                                            strokeWidth = 2f
                                        )
                                    } else {
                                        val y1 = measuredText.getLineForOffset(highlighter.startOffset)
                                        val y2 = measuredText.getLineForOffset(highlighter.endOffset)
                                        val x1 = measuredText.getHorizontalPosition(
                                            highlighter.startOffset,
                                            true
                                        )
                                        val x2 =
                                            measuredText.getHorizontalPosition(highlighter.endOffset, true)
                                        if (y1 == y2) { // error is on one line
                                            val y = measuredText.getLineBottom(y1)
                                            drawLine(
                                                color = Color.Red,
                                                start = Offset(x1, y),
                                                end = Offset(x2, y),
                                                strokeWidth = 2f
                                            )
                                        } else {
                                            for (y in y1 + 1 until y2) {
                                                val lineX1 = measuredText.getHorizontalPosition(
                                                    measuredText.getLineStart(y), true
                                                )
                                                val lineX2 = measuredText.getHorizontalPosition(
                                                    measuredText.getLineEnd(y), true
                                                )
                                                val lineY = measuredText.getLineBottom(y)
                                                drawLine(
                                                    color = Color.Red,
                                                    start = Offset(lineX1, lineY),
                                                    end = Offset(lineX2, lineY),
                                                    strokeWidth = 2f
                                                )
                                            }
                                            val y1LineEnd = measuredText.getHorizontalPosition(
                                                measuredText.getLineEnd(y1), true
                                            )
                                            val lineY1 = measuredText.getLineBottom(y1)
                                            drawLine(
                                                color = Color.Red,
                                                start = Offset(x1, lineY1),
                                                end = Offset(y1LineEnd, lineY1),
                                                strokeWidth = 2f
                                            )
                                            val y2LineStart = measuredText.getHorizontalPosition(
                                                measuredText.getLineStart(y2), true
                                            )
                                            val lineY2 = measuredText.getLineBottom(y2)
                                            drawLine(
                                                color = Color.Red,
                                                start = Offset(y2LineStart, lineY2),
                                                end = Offset(x2, lineY2),
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
        }
    }
}