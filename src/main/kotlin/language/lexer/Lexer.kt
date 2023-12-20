package language.lexer

import language.ErrorType
import language.LexicalError

data class TokenWithOffset(val token: Token, val startOffset: Int, val endOffset: Int)

class Lexer(private val input: String) {

    private val errors = mutableListOf<language.Error>()

    private var currentIndex = 0

    fun tokenize(): Pair<List<TokenWithOffset>, MutableList<language.Error>> {
        val tokens = mutableListOf<TokenWithOffset>()
        skipWhitespace()
        while (currentIndex < input.length) {
            val char = peek()
            when {
                char.isDigit() -> tokens.add(recognizeConstant())
                char.isLetter() -> tokens.add(recognizeWord())
                char == QUOTE -> tokens.add(recognizeStringLiteral())
                isSpecialSymbol(char) -> tokens.add(recognizeSpecialSymbol())
                else -> {
                    val startOffset = currentIndex
                    skipUntilTokenEnd()
                    val unrecognizedTokenWithOffset = TokenWithOffset(UnrecognizedToken, startOffset, currentIndex)
                    tokens.add(unrecognizedTokenWithOffset)
                    errors.add(LexicalError(unrecognizedTokenWithOffset, ErrorType.UNEXPECTED_SYMBOL))
                }
            }
            skipWhitespace()
        }
        return Pair(tokens, errors)
    }

    private fun peek(): Char = if (currentIndex < input.length) input[currentIndex] else 0.toChar()
    private fun nextChar(): Char = if (currentIndex < input.length) input[currentIndex++] else 0.toChar()
    private fun skipWhitespace() {
        while (currentIndex < input.length && peek().isWhitespace()) {
            currentIndex++
        }
    }

    private fun skipUntilTokenEnd() {
        while (currentIndex < input.length && !peek().isWhitespace()) {
            currentIndex++
        }
    }

    private fun recognizeConstant(): TokenWithOffset {
        val sb = StringBuilder()
        val startOffset = currentIndex
        while (currentIndex < input.length && Character.isDigit(peek())) {
            sb.append(nextChar())
        }
        val token = ConstantToken(sb.toString().toInt())
        return TokenWithOffset(token, startOffset, currentIndex)
    }

    private fun recognizeWord(): TokenWithOffset {
        val sb = StringBuilder()
        val startOffset = currentIndex
        while (currentIndex < input.length && (Character.isDigit(peek()) || Character.isLetter(peek()))) {
            sb.append(nextChar())
        }
        val token = when (val str = sb.toString()) {
            TRUE -> BoolToken(true)
            FALSE -> BoolToken(false)
            in keywordToToken -> keywordToToken.getValue(str)
            else -> IdentifierToken(str)
        }
        return TokenWithOffset(token, startOffset, currentIndex)
    }

    private fun recognizeStringLiteral(): TokenWithOffset {
        val sb = StringBuilder()
        val startOffset = currentIndex
        nextChar()
        while (currentIndex < input.length && peek() != QUOTE) {
            sb.append(nextChar())
        }
        val quoteChar = nextChar()
        val token = TokenWithOffset(StringLiteralToken(sb.toString()), startOffset, currentIndex)
        if (quoteChar != QUOTE) {
            errors.add(LexicalError(token, ErrorType.EXPECTED_QUOTE))
        }
        return token
    }

    private fun recognizeSpecialSymbol(): TokenWithOffset {
        val startOffset = currentIndex
        val token: Token
        if (currentIndex + 1 < input.length) {
            val twoCharSymbol = "" + peek() + input[currentIndex + 1]
            if (twoCharSymbol in specialSymbolToToken.keys) {
                currentIndex += 2
                token = specialSymbolToToken.getValue(twoCharSymbol)
                return TokenWithOffset(token, startOffset, currentIndex)
            }
        }
        val char = nextChar().toString()
        if (specialSymbolToToken.containsKey(char)) {
            token = specialSymbolToToken.getValue(char)
            return TokenWithOffset(token, startOffset, currentIndex)
        }
        skipUntilTokenEnd()
        val tokenWithOffset = TokenWithOffset(UnrecognizedToken, startOffset, currentIndex)
        errors.add(LexicalError(tokenWithOffset, ErrorType.UNEXPECTED_SYMBOL))
        return tokenWithOffset
    }
}
