package language.lexer

data class TokenWithOffset(val token: Token, val startOffset: Int, val endOffset: Int)

class Lexer(private val input: String) {
    var tokenizedWithError = false
        private set

    private var currentIndex = 0

    fun tokenize(): List<TokenWithOffset> {
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
                    tokenizedWithError = true
                    skipUntilTokenEnd()
                    tokens.add(TokenWithOffset(UnrecognizedToken("Unexpected symbol."), startOffset, currentIndex))
                }
            }
            skipWhitespace()
        }
        return tokens
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
        val token: Token
        while (currentIndex < input.length && peek() != QUOTE) {
            sb.append(nextChar())
        }
        if (currentIndex < input.length && peek() == QUOTE) {
            nextChar()
            token = StringLiteralToken(sb.toString())
        } else {
            tokenizedWithError = true
            token = UnrecognizedToken("Expected '$QUOTE'.")
        }
        return TokenWithOffset(token, startOffset, currentIndex)
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
        tokenizedWithError = true
        skipUntilTokenEnd()
        return TokenWithOffset(UnrecognizedToken("Unexpected symbol."), startOffset, currentIndex)
    }
}
