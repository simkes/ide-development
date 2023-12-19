package language

import language.lexer.BoolTypeToken
import language.lexer.NumberTypeToken
import language.lexer.StringTypeToken

// each level of analysis also includes all previous
enum class Level {
    NONE,
    LEXICAL,
    SYNTAX,
    SEMANTIC
}

enum class Type {
    NUMBER,
    STRING,
    BOOL,
    UNKNOWN
}

val tokenToType = mapOf(
    NumberTypeToken to Type.NUMBER,
    StringTypeToken to Type.STRING,
    BoolTypeToken to Type.BOOL
)