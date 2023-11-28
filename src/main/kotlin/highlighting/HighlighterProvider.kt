package highlighting

import language.AnalysisError
import language.CodeAnalyzer
import language.Level
import language.lexer.*

object HighlighterProvider {

    /**
     * Creates code highlighters based on code analysis.
     * Returns 2 sorted highlighters lists: first - lexical highlighting (language units like keywords),
     * second - syntax and semantic errors highlighting (underlining errors and providing messages).
     * @param level level to which code analysis is performed. (LEXICAL will perform only 1 stage - tokenization)
     */
    fun getHighlighters(code: String, level: Level): Pair<List<Highlighter>, List<Highlighter>> {
        val (tokens, errors) = CodeAnalyzer.analyze(code, level)
        val lexicalHighlighters = tokens.map { tk -> createHighlighter(tk) }
        val errorHighlighters =
            errors.map { error -> createHighlighter(error, tokens) }.sortedBy { highlighter -> highlighter.startOffset }
        return Pair(lexicalHighlighters, errorHighlighters)
    }

    /**
     * Same as [getHighlighters], but merged into one list and sorted by start position.
     */
    fun getHighlightersMerged(code: String, level: Level): List<Highlighter> {
        val (highlighters1, highlighters2) = getHighlighters(code, level)
        return (highlighters1 + highlighters2).sortedBy { h -> h.startOffset }
    }

    private fun createHighlighter(tokenWithOffset: TokenWithOffset): Highlighter {
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

    private fun createHighlighter(analysisError: AnalysisError, tokens: List<TokenWithOffset>): Highlighter {
        val startOffset = tokens[analysisError.node.start].startOffset
        val endOffset = tokens[analysisError.node.end].endOffset
        return Highlighter(startOffset, endOffset, Color.BLACK, true, analysisError.errorMessage)
    }
}