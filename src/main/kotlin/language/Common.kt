package language

import language.parser.ASTNode

// each level of analysis also includes all previous
enum class Level {
    NONE,
    LEXICAL,
    SYNTAX,
    SEMANTIC
}
data class AnalysisError(val node: ASTNode, val errorMessage: String)