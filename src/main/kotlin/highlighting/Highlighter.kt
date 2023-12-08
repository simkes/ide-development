package highlighting

enum class Color {
    RED, // unrecognized tokens (errors), unresolved symbols
    ORANGE, // keywords and bool values
    PURPLE, // identifiers
    GREEN, // string literals
    BLUE, // constants
    BLACK // rest - operation, types, punctuation
}

sealed class Highlighter(
    open val startOffset: Int,
    open val endOffset: Int
)

data class ColoredHighlighter(
    override val startOffset: Int,
    override val endOffset: Int,
    val color: Color = Color.BLACK,
    val errorMessage: String? = null
) : Highlighter(startOffset, endOffset)

data class UnderlinedHighlighter(
    override val startOffset: Int,
    override val endOffset: Int,
    val underlinedAfterEndOffset: Boolean,
    val errorMessage: String
) : Highlighter(startOffset, endOffset)