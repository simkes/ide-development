package highlighting

import language.*
import language.lexer.*
import language.parser.Expr

object HighlighterProvider {

    /**
     * Creates code highlighters based on code analysis.
     * Returns 2 sorted highlighters lists: first - token colored highlighting (language units like keywords),
     * second - underlining errors highlighting.
     * @param level level to which code analysis is performed. (LEXICAL will perform only 1 stage - tokenization)
     */
    fun getHighlighters(code: String, level: Level): Pair<List<ColoredHighlighter>, List<UnderlinedHighlighter>> {
        val (tokensWithOffset, errors) = CodeAnalyzer.analyze(code, level)
        val tokenHighlighters = tokensWithOffset.map { tk -> createHighlighter(tk) }.sortedBy { h -> h.startOffset }
        val errorHighlighters = errors.map { er -> createHighlighter(er, tokensWithOffset) }.sortedBy { h -> h.startOffset }

        val errorColoredHighlightersByOffset = errorHighlighters.mapNotNull { it as? ColoredHighlighter }
            .associateBy { Pair(it.startOffset, it.endOffset) }

        val coloredHighlighters = mutableListOf<ColoredHighlighter>()
        for (tokenHighlighter in tokenHighlighters) {
            if (tokenHighlighter is ColoredHighlighter) {
                val key = Pair(tokenHighlighter.startOffset, tokenHighlighter.endOffset)
                val errorColoredHighlighter = errorColoredHighlightersByOffset[key]
                if (errorColoredHighlighter != null) {
                    coloredHighlighters.add(errorColoredHighlighter)
                } else {
                    coloredHighlighters.add(tokenHighlighter)
                }
            }
        }

        val underlinedHighlighters = errorHighlighters.filterIsInstance<UnderlinedHighlighter>()
        return Pair(coloredHighlighters, underlinedHighlighters)
    }

    private fun createHighlighter(tokenWithOffset: TokenWithOffset): Highlighter {
        val startOffset = tokenWithOffset.startOffset
        val endOffset = tokenWithOffset.endOffset

        return when (tokenWithOffset.token) {
            is IdentifierToken -> ColoredHighlighter(startOffset, endOffset, Color.PURPLE)
            is ConstantToken -> ColoredHighlighter(startOffset, endOffset, Color.BLUE)
            is StringLiteralToken -> ColoredHighlighter(startOffset, endOffset, Color.GREEN)
            is BoolToken, is KeywordToken -> ColoredHighlighter(startOffset, endOffset, Color.ORANGE)
            else -> ColoredHighlighter(startOffset, endOffset)
        }
    }

    private fun createHighlighter(error: Error, tokens: List<TokenWithOffset>): Highlighter {
        if (error is LexicalError) {
            val token = error.token
            if (error.type == ErrorType.UNEXPECTED_SYMBOL) return ColoredHighlighter(
                token.startOffset,
                token.endOffset,
                Color.RED,
                "Unexpected symbol."
            )
            return UnderlinedHighlighter(token.startOffset, token.endOffset, true, "Expected '\"'.")
        }

        if (error is SyntaxError) {
            val tokenIndex = error.tokenIndex
            val startOffset = tokens[tokenIndex].startOffset
            val endOffset = tokens[tokenIndex].endOffset
            if (error.type == ErrorType.UNEXPECTED_TOKEN) {
                val tk = (error as UnexpectedToken).token
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Unexpected token ${tokenToName(tk)}."
                )
            }

            if (error.type == ErrorType.EXPECTED_TOKEN) {
                val expected = (error as ExpectedToken).expectedToken
                val actual = error.actualToken
                val errMessage =
                    if (actual == null) "Expected ${tokenToName(expected)}." else "Expected ${tokenToName(expected)}. Found ${
                        tokenToName(actual)
                    }."
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    true,
                    errMessage
                )
            }

            return UnderlinedHighlighter(startOffset, endOffset, true, "Expected expression.")
        }

        // semantic errors
        val node = (error as SemanticError).node
        val startOffset = tokens[node.start].startOffset
        val endOffset = tokens[node.end].endOffset

        when (error.type) {
            ErrorType.CONFLICTING_DECLARATION -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Conflicting declaration of ${(node as Expr.SymbolName).identifier.name}."
                )
            }

            ErrorType.UNRESOLVED_SYMBOL -> {
                return ColoredHighlighter(
                    startOffset,
                    endOffset,
                    Color.RED,
                    "Unresolved symbol ${(node as Expr.SymbolName).identifier.name}."
                )
            }

            ErrorType.ASSIGNMENT_TYPE_MISMATCH -> {
                val expectedType = (error as AssignmentTypeMismatch).expected
                val actualType = error.actual
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Type mismatch. Expected: ${typeToName[expectedType]}. Found: ${typeToName[actualType]}."
                )
            }

            ErrorType.CONDITION_TYPE_MISMATCH -> {
                val actualType = (error as ConditionTypeMismatch).actual
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Type mismatch. Expected: ${typeToName[Type.BOOL]}. Found: ${typeToName[actualType]}."
                )
            }

            ErrorType.MISSING_RETURN_STATEMENT -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Missing return statement in function body."
                )
            }

            ErrorType.UNEXPECTED_RETURN_STATEMENT -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Unexpected return statement outside function body."
                )
            }

            ErrorType.UNRESOLVED_OVERLOAD -> {
                val funcName = (error as UnresolvedOverload).name
                val args = error.providedTypes

                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Unresolved overload: ${createFunctionSignature(funcName, args)}."
                )
            }

            ErrorType.IGNORED_RETURN_VALUE -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Ignoring return value of function."
                )
            }

            ErrorType.NO_RETURN_VALUE -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Procedure does not return a value."
                )
            }

            ErrorType.OPERAND_TYPE_MISMATCH -> {
                val op = (error as OperandTypeMismatch).operator
                val providedTypesStr = error.providedTypes.joinToString(separator = ", ") { type -> typeToName[type]!! }
                val errMessage = if (op is EqOpToken || op is NotEqOpToken) {
                    "Type mismatch. Provided types: $providedTypesStr."
                } else {
                    "Expected type: ${expectedTypeStr(op)}. Found: $providedTypesStr."
                }
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    errMessage
                )
            }

            ErrorType.INVALID_OPERATOR -> {
                val op = (error as InvalidOperator).operator
                val errMessage = if (op.binary) "Expected two operands." else "Expected one operand."
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    errMessage
                )
            }

            ErrorType.OUTSIDE_GLOBAL_SCOPE_CALLABLE_DECLARATION -> {
                return UnderlinedHighlighter(
                    startOffset,
                    endOffset,
                    false,
                    "Function or procedure must be declared in global scope."
                )
            }

            else -> throw IllegalStateException("Unhandled error type.")
        }
    }

    private fun tokenToName(token: Token): String {
        val objTokenToName = mapOf(
            LeftParenToken to "(",
            RightParenToken to ")",
            LeftBraceToken to "{",
            RightBraceToken to "}",
            SemicolonToken to ";",
            CommaToken to ",",
            ColonToken to ":",
            AssignToken to "=",
            VarKeywordToken to "var",
            IfKeywordToken to "if",
            FuncKeywordToken to "func",
            ProcKeywordToken to "proc",
            ElseKeywordToken to "else",
            WhileKeywordToken to "while",
            PrintKeywordToken to "print",
            ReturnKeywordToken to "return",
            UnrecognizedToken to "unrecognized token"
        )

        return when (token) {
            is IdentifierToken -> "name"
            is ConstantToken -> "number"
            is StringLiteralToken -> "string"
            is BoolToken -> "bool"
            is OpToken -> token.operatorSymbol
            is TypeToken -> "type"
            else -> objTokenToName[token] ?: ""
        }
    }

    private val typeToName = mapOf(
        Type.NUMBER to "number",
        Type.STRING to "string",
        Type.BOOL to "bool",
        Type.UNKNOWN to "unknown"
    )

    private fun createFunctionSignature(funcName: String, args: List<Type>): String {
        val argsAsString = args.joinToString(separator = ", ") { type -> typeToName[type]!! }
        return "$funcName($argsAsString)"
    }

    private fun expectedTypeStr(op: OpToken): String {
        return when (op) {
            is ArithmeticOpToken -> typeToName[Type.NUMBER]!!
            is StringOpToken -> typeToName[Type.STRING]!!
            is BoolOpToken -> typeToName[Type.BOOL]!!
            is NumericRelationOpToken -> typeToName[Type.NUMBER]!!
            else -> throw IllegalStateException("Unhandled operator.")
        }
    }
}