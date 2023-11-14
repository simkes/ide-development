import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import editor.EditorViewModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ui.*

class CaretNavigationTest {
    private val model = EditorViewModel

    private fun assertCaretEvent(event: UiEvent, desiredPos: Pair<Int, Int>) {
        runBlocking {
            event.process()
        }
        assert(model.getCaret() == desiredPos) { "Actual pos: ${model.getCaret()}, desired pos: $desiredPos" }
    }

    private fun String.insertIntoEditor() {
        var line = 0
        var char = 0
        this.forEach {
            if (it == '\n') {
                line++
                char = 0
                assertCaretEvent(NewlineKeyEvent(), Pair(line, char))
            } else {
                char++
            }
            assertCaretEvent(TextInsertionEvent(it.toString()), Pair(line, char))
        }
    }

    @AfterEach
    fun clean() {
        model.purge()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `caret moves on single line`() {
        val txt = "Lorem ipsum dolor sit amet"
        assertCaretEvent(TextInsertionEvent(txt), Pair(0, txt.length))
        repeat(txt.length * 10) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionRight), Pair(0, txt.length))
        }
        for (i in 1..txt.length + 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionLeft), Pair(0, maxOf(0, txt.length - i)))
        }
        repeat(txt.length * 10) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionLeft), Pair(0, 0))
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `caret moves only vertically`() {
        val txt = "Meow\nWoof\nQuac\nkCro\nack."
        val lines = txt.count { it == '\n' }
        txt.insertIntoEditor()
        val char = 4
        repeat(txt.length * 10) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionDown), Pair(lines, char))
        }
        for (i in 1..lines) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionUp), Pair(lines - i, 4))
        }
        repeat(txt.length * 10) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionUp), Pair(0, 4))
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `caret moves horizontally between lines`() {
        val txt = "Meow\nWoof\nQuac\nkCro\nack."
        val lines = txt.count { it == '\n' }
        txt.insertIntoEditor()
        var line = lines
        var char = 4
        for (i in txt.indices) {
            if (txt[txt.length - 1 - i] == '\n') {
                line--
                char = 4
            } else {
                char--
            }
            assertCaretEvent(ArrowKeyEvent(Key.DirectionLeft), Pair(line, char))
        }
        for (i in txt.indices) {
            if (txt[i] == '\n') {
                line++
                char = 0
            } else {
                char++
            }
            assertCaretEvent(ArrowKeyEvent(Key.DirectionRight), Pair(line, char))
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `offset is handled between lines of different length`() {
        val txt = "Lorem ipsum\n" +
                " dolor sit amet\n, " +
                "consetetur sadipscing\n" +
                " elitr, sed diam nonumy eirmod tempor invidunt\n" +
                " ut labore et dolore magna aliquyam erat"
        val linesLengths = txt.split('\n').map { it.length }
        txt.insertIntoEditor()
        var line = linesLengths.size - 1
        var char = linesLengths.last()
        val lessMinLength = linesLengths.min() * 3 / 4

        // move caret to the position less than length of minimal line
        repeat(model.getCaret().second - lessMinLength) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionLeft), Pair(line, char - it - 1))
        }

        // go up and down ensuring position is preserved
        char = lessMinLength
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionUp), Pair(--line, char))
        }
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionDown), Pair(++line, char))
        }

        // move caret to the end of the last line
        repeat(linesLengths.last() - model.getCaret().second) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionRight), Pair(line, ++char))
        }

        // go up and down ensuring position is minOf(lastLine.length, curLine.length)
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionUp), Pair(--line, minOf(char, linesLengths[line])))
        }
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionDown), Pair(++line, minOf(char, linesLengths[line])))
        }

        // move caret to the first line
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionUp), Pair(--line, minOf(char, linesLengths[line])))
        }
        char = linesLengths[line]
        // ensure that the caret position is updated and now preserves only current change
        // TODO: similar test for direction right
        assertCaretEvent(ArrowKeyEvent(Key.DirectionLeft), Pair(line, --char))
        repeat(linesLengths.size - 1) {
            assertCaretEvent(ArrowKeyEvent(Key.DirectionDown), Pair(++line, char))
        }
    }

    @Test
    fun `deletion correctly eliminates newlines`() {
        val txt = "foo\nbar\nbaz".also { it.insertIntoEditor() }.split("\n")
        var line = txt.size - 1
        var char = txt.last().length

        // forEachReversed would be more correct, but who cares
        txt.forEach {
            repeat(it.length) {
                assertCaretEvent(BackspaceKeyEvent(), Pair(line, --char))
            }
            char = txt.last().length
            if (it == txt.last()) {
                assertCaretEvent(BackspaceKeyEvent(), Pair(0, 0))
            } else {
                assertCaretEvent(BackspaceKeyEvent(), Pair(--line, char))
            }
        }
    }
}