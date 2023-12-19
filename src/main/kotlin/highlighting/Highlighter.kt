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
    open val endOffset: Int,
    open val errorMessage: String? = null
)

data class ColoredHighlighter(
    override val startOffset: Int,
    override val endOffset: Int,
    val color: Color = Color.BLACK,
    override val errorMessage: String? = null
) : Highlighter(startOffset, endOffset)

data class UnderlinedHighlighter(
    override val startOffset: Int,
    override val endOffset: Int,
    val underlinedAfterEndOffset: Boolean,
    override val errorMessage: String? = null
) : Highlighter(startOffset, endOffset)