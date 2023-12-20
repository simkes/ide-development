import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        highlighting.Color.BLACK -> ViewConfig.defaultTextColor
    }
}

val verticalDividerModifier: Modifier.() -> Modifier = {
    this.fillMaxHeight().width(1.dp)
}

val ASCII_RANGE = 9..128
const val OPENED_DOCUMENTS_LIMIT = 2
const val SPACES_IN_TAB = 2

object ViewConfig {
    val fontFamily = FontFamily.Monospace
    val defaultFontSize = 12.sp
    val defaultColor = Color.Black
    val defaultTextStyle = TextStyle(
        fontSize = defaultFontSize, fontFamily = fontFamily, color = defaultColor
    )
    val bigFontSize = 14.sp
    val defaultTextColor = Color(0xFFd0cfc8)
    val errorLinkColor = Color(0xFFae7313)
    val errorIconColor = Color(0xFFff5c33)
    val darkBackgroundColor = Color(0xFF333333)
    val lightMainBackgroundColor = Color(0xFF666666)
    val widgetBackgroundColor = Color(0xFF4d4d4d)
}