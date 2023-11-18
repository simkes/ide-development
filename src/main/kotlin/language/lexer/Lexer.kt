package language.lexer

class Lexer(private val input: String) {
    var tokenizedWithError = false
        private set

    private var currentIndex = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        skipWhitespace()
        while (currentIndex < input.length) {
            val char = peek()
            when {
                char.isDigit() -> tokens.add(recognizeConstant())
                char.isLetter() -> tokens.add(recognizeWord())
                char == QUOTE -> tokens.add(recognizeStringLiteral())
                isSpecialSymbol(char) -> tokens.add(recognizeSpecialSymbol())
                else -> {
                    tokenizedWithError = true
                    tokens.add(UnrecognizedToken("Unexpected character '$char' at position $currentIndex."))
                    skipToNextToken()
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

    private fun skipToNextToken() {
        while(currentIndex < input.length && !peek().isWhitespace()){
            currentIndex++
        }
    }

    private fun recognizeConstant(): ConstantToken {
        val sb = StringBuilder()
        while (currentIndex < input.length && Character.isDigit(peek())) {
            sb.append(nextChar())
        }
        return ConstantToken(sb.toString().toInt())
    }

    private fun recognizeWord(): Token {
        val sb = StringBuilder()
        while (currentIndex < input.length && (Character.isDigit(peek()) || Character.isLetter(peek()))) {
            sb.append(nextChar())
        }
        return when (val str = sb.toString()) {
            TRUE -> BoolToken(true)
            FALSE -> BoolToken(false)
            in keywordToToken -> keywordToToken.getValue(str)
            else -> IdentifierToken(str)
        }
    }

    private fun recognizeStringLiteral(): Token {
        val sb = StringBuilder()
        nextChar()
        while (currentIndex < input.length && peek() != QUOTE) {
            sb.append(nextChar())
        }
        if (currentIndex < input.length && peek() == QUOTE) {
            nextChar()
        } else {
            tokenizedWithError = true
            return UnrecognizedToken("Expected '$QUOTE'.")
        }
        return StringLiteralToken(sb.toString())
    }

    private fun recognizeSpecialSymbol(): Token {
        if (currentIndex + 1 < input.length) {
            val twoCharSymbol = "" + peek() + input[currentIndex + 1]
            if (twoCharSymbol in specialSymbolToToken.keys) {
                currentIndex += 2
                return specialSymbolToToken.getValue(twoCharSymbol)
            }
        }
        val char = nextChar().toString()
        if (specialSymbolToToken.containsKey(char))
            return specialSymbolToToken.getValue(char)

        tokenizedWithError = true
        val pos = currentIndex - 1
        skipToNextToken()
        return UnrecognizedToken("Unexpected character '$char' at position $pos.")
    }
}
