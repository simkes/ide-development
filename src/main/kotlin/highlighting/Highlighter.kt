package highlighting

enum class Color {
    RED, // unrecognized tokens (errors)
    ORANGE, // keywords and bool values
    PURPLE, // identifiers
    GREEN, // string literals
    BLUE, // constants
    BLACK // rest - operation, types, punctuation
}

data class Highlighter(
    val startOffset: Int,
    val endOffset: Int,
    val color: Color = Color.BLACK,
    val underlined: Boolean = false,
    val errorMessage: String = ""
)