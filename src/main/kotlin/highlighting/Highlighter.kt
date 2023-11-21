package highlighting

import language.lexer.*

enum class Color {
    RED, // errors
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

// lexical (token) highlighting
fun createHighlighter(tokenWithOffset: TokenWithOffset): Highlighter {
    val startOffset = tokenWithOffset.startOffset
    val endOffset = tokenWithOffset.endOffset
    return when (tokenWithOffset.token) {
        is IdentifierToken -> Highlighter(startOffset, endOffset, Color.PURPLE)
        is ConstantToken -> Highlighter(startOffset, endOffset, Color.BLUE)
        is StringLiteralToken -> Highlighter(startOffset, endOffset, Color.GREEN)
        is BoolToken, is KeywordToken -> Highlighter(startOffset, endOffset, Color.ORANGE)
        is UnrecognizedToken -> Highlighter(
            startOffset,
            endOffset,
            Color.RED,
            false,
            tokenWithOffset.token.errorMessage
        )

        else -> Highlighter(startOffset, endOffset)
    }
}