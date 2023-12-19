import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import highlighting.Highlighter

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

@OptIn(ExperimentalComposeUiApi::class)
val arrowEventToDirection = mapOf(
    Key.DirectionUp to Direction.UP,
    Key.DirectionDown to Direction.DOWN,
    Key.DirectionLeft to Direction.LEFT,
    Key.DirectionRight to Direction.RIGHT
)

val highlighterColorToComposeColor = { color: highlighting.Color ->
    when (color) {
        highlighting.Color.RED -> Color(0xFFBF2308)
        highlighting.Color.ORANGE -> Color(0xFFae7313)
        highlighting.Color.PURPLE -> Color(0xFF9d6c7c)
        highlighting.Color.GREEN -> Color(0xFF7d9726)
        highlighting.Color.BLUE -> Color(0xFF5f9182)
        highlighting.Color.BLACK -> Color(0xFF878573)
    }
}

val ASCII_RANGE = 9 .. 128
const val OPENED_DOCUMENTS_LIMIT = 2
