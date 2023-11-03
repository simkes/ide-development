import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key

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

val ASCII_RANGE = 9 .. 128