package language.parser

import language.*
import language.lexer.*

class RecursiveDescentParser(private val tokens: List<Token>) {

    private var currentTokenIndex = 0

    private val errors = mutableListOf<SyntaxError>()

    // ignores all further errors if already faced one. Updates when entering new correct statement
    private var insideInvalidStmt = false

    fun parse(): Pair<Stmt.Block, MutableList<SyntaxError>> {
        val program = block(false) as Stmt.Block
        expectEndOfInput()
        return Pair(program, errors)
    }

    private fun unexpectedTokenError(tokenIndex: Int) {
        insideInvalidStmt = true
        errors.add(UnexpectedToken(tokenIndex, tokens[tokenIndex]))
    }

    private fun expectedTokenError(tokenIndex: Int, expectedToken: Token) {
        insideInvalidStmt = true
        val actualToken = if (tokenIndex < tokens.size) tokens[tokenIndex] else null
        errors.add(ExpectedToken(tokenIndex - 1, expectedToken, actualToken))
    }

    private fun expectedExpressionError(tokenIndex: Int) {
        insideInvalidStmt = true
        errors.add(ExpectedExpression(tokenIndex - 1))
    }

    private fun block(withBraces: Boolean = true): Stmt? {
        val startToken = currentTokenIndex

        if (withBraces) {
            consumeToken(LeftBraceToken)
        }
        if (insideInvalidStmt) return null

        val statements = mutableListOf<Stmt>()

        while (currentTokenIndex < tokens.size) {
            if (withBraces && checkToken(RightBraceToken))
                break
            statement()?.let { statements.add(it) }
        }

        if (withBraces) {
            consumeToken(RightBraceToken)
        }
        // not returning null if right brace is missing, it will be handled through error

        return Stmt.Block(statements, startToken, currentTokenIndex - 1)
    }

    private fun isStartOfStatement(token: Token?): Boolean {
        return token?.let { it::class in startStatementTokens } ?: false
    }

    private fun statement(): Stmt? {
        val token: Token = peekToken()!!

        if (isStartOfStatement(token)) {
            insideInvalidStmt = false
            return when (token) {
                is VarKeywordToken -> varDeclaration()
                is IdentifierToken -> assignmentOrProcCall()
                is IfKeywordToken -> ifStatement()
                is WhileKeywordToken -> whileStatement()
                is LeftBraceToken -> block()
                is PrintKeywordToken -> printStatement()
                is FuncKeywordToken -> funcDeclaration()
                is ReturnKeywordToken -> returnStatement()
                is ProcKeywordToken -> procDeclaration()
                else -> throw IllegalStateException()
            }
        }

        if (insideInvalidStmt) {
            nextToken()
            return null
        }


        unexpectedTokenError(currentTokenIndex)
        nextToken()
        return null
    }

    private fun varDeclaration(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(VarKeywordToken)
        if (insideInvalidStmt) return null

        if (currentTokenIndex >= tokens.size || peekToken() !is IdentifierToken) {
            expectedTokenError(currentTokenIndex, IdentifierToken(""))
            return null
        }
        val symbolName = symbolName()

        consumeToken(AssignToken)
        if (insideInvalidStmt) return null

        val expression = expression() ?: return null

        consumeToken(SemicolonToken)
        // not returning null if semicolon is missing

        return Stmt.VarDeclaration(symbolName, expression, startToken, currentTokenIndex - 1)
    }

    private fun symbolName(): Expr.SymbolName {
        val identifierToken = nextToken() as IdentifierToken
        return Expr.SymbolName(identifierToken, currentTokenIndex - 1, currentTokenIndex - 1)
    }

    private fun assignmentOrProcCall(): Stmt? {
        if (currentTokenIndex >= tokens.size || peekToken() !is IdentifierToken) {
            expectedTokenError(currentTokenIndex, IdentifierToken(""))
            return null
        }

        val symbolName = symbolName()

        return if (checkToken(AssignToken)) {
            assignment(symbolName)
        } else {
            procCall(symbolName)
        }
    }

    private fun assignment(symbolName: Expr.SymbolName): Stmt? {
        val startToken = currentTokenIndex - 1

        consumeToken(AssignToken)
        if (insideInvalidStmt) return null

        val expression = expression() ?: return null

        consumeToken(SemicolonToken)

        return Stmt.Assignment(symbolName, expression, startToken, currentTokenIndex - 1)
    }

    private fun procCall(symbolName: Expr.SymbolName): Stmt? {
        val startToken = currentTokenIndex - 1

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val arguments = arguments() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        consumeToken(SemicolonToken)

        return Stmt.ProcCall(symbolName, arguments, startToken, currentTokenIndex - 1)
    }

    private fun ifStatement(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(IfKeywordToken)
        if (insideInvalidStmt) return null

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val condition = expression() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        val block = block() ?: return null

        var elseBlock: Stmt? = null
        if (checkToken(ElseKeywordToken)) {
            consumeToken(ElseKeywordToken)
            elseBlock = block()
        }

        return Stmt.IfStatement(
            condition,
            block as Stmt.Block,
            elseBlock as Stmt.Block, // TODO: check null casts to Block
            startToken,
            currentTokenIndex - 1
        )
    }

    private fun whileStatement(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(WhileKeywordToken)
        if (insideInvalidStmt) return null

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val condition = expression() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        val block = block() ?: return null

        return Stmt.WhileStatement(condition, block as Stmt.Block, startToken, currentTokenIndex - 1)
    }


    private fun printStatement(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(PrintKeywordToken)
        if (insideInvalidStmt) return null

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val expression = expression() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        consumeToken(SemicolonToken)

        return Stmt.PrintStatement(expression, startToken, currentTokenIndex - 1)
    }

    private fun funcDeclaration(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(FuncKeywordToken)
        if (insideInvalidStmt) return null

        if (currentTokenIndex >= tokens.size || peekToken() !is IdentifierToken) {
            expectedTokenError(currentTokenIndex, IdentifierToken(""))
            return null
        }

        val symbolName = symbolName()

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val parameters = parameters() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        val block = block() ?: return null

        return Stmt.FuncDeclaration(symbolName, parameters, block as Stmt.Block, startToken, currentTokenIndex - 1)
    }

    private fun returnStatement(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(ReturnKeywordToken)
        if (insideInvalidStmt) return null

        val expression = expression() ?: return null

        consumeToken(SemicolonToken)

        return Stmt.ReturnStatement(expression, startToken, currentTokenIndex - 1)
    }

    private fun procDeclaration(): Stmt? {
        val startToken = currentTokenIndex

        consumeToken(ProcKeywordToken)
        if (insideInvalidStmt) return null

        if (currentTokenIndex >= tokens.size || peekToken() !is IdentifierToken) {
            expectedTokenError(currentTokenIndex, IdentifierToken(""))
            return null
        }

        val symbolName = symbolName()

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val parameters = parameters() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        val block = block() ?: return null

        return Stmt.ProcDeclaration(symbolName, parameters, block as Stmt.Block, startToken, currentTokenIndex - 1)
    }

    private fun arguments(): List<Expr>? {
        val args = mutableListOf<Expr>()
        if (!checkToken(RightParenToken)) {
            do {
                args.add(expression() ?: return null)
            } while (checkToken(CommaToken).also { if (it) consumeToken(CommaToken) })
        }
        return args
    }

    private fun parameters(): List<Parameter>? {
        val params = mutableListOf<Parameter>()
        if (!checkToken(RightParenToken)) {
            do {
                val startToken = currentTokenIndex

                if (currentTokenIndex >= tokens.size || peekToken() !is IdentifierToken) {
                    expectedTokenError(currentTokenIndex, IdentifierToken(""))
                    return null
                }

                val symbolName = symbolName()

                consumeToken(ColonToken)
                if (insideInvalidStmt) return null

                if (currentTokenIndex >= tokens.size || peekToken() !is TypeToken) {
                    expectedTokenError(currentTokenIndex, TypeToken())
                    return null
                }
                val paramType = tokenToType[nextToken() as TypeToken]!!

                params.add(Parameter(symbolName, paramType, startToken, currentTokenIndex - 1))
            } while (checkToken(CommaToken).also { if (it) consumeToken(CommaToken) })
        }
        return params
    }

    private fun factor(): Expr? {
        val startToken = currentTokenIndex

        if (currentTokenIndex == tokens.size) {
            expectedExpressionError(startToken)
            return null
        }

        return when (val token = nextToken()) {
            is BoolToken -> Expr.BoolValue(token, startToken, startToken)
            is StringLiteralToken -> Expr.StringLiteral(token, startToken, startToken)
            is ConstantToken -> Expr.Constant(token, startToken, startToken)
            is IdentifierToken -> identifierOrFuncCall(token)
            is LeftParenToken -> {
                val expr = expression()
                consumeToken(RightParenToken)
                expr
            }

            is OpToken -> {
                val factor = factor() ?: return null
                Expr.UnaryOp(token, factor, startToken, currentTokenIndex - 1)
            }

            else -> {
                expectedExpressionError(startToken)
                return null
            }
        }
    }

    private fun identifierOrFuncCall(identifierToken: IdentifierToken): Expr? {
        val symbolName = Expr.SymbolName(identifierToken, currentTokenIndex - 1, currentTokenIndex - 1)
        return if (checkToken(LeftParenToken)) {
            funcCall(symbolName)
        } else {
            symbolName
        }
    }

    private fun funcCall(symbolName: Expr.SymbolName): Expr.FuncCall? {
        val startToken = currentTokenIndex - 1

        consumeToken(LeftParenToken)
        if (insideInvalidStmt) return null

        val arguments = arguments() ?: return null

        consumeToken(RightParenToken)
        if (insideInvalidStmt) return null

        return Expr.FuncCall(symbolName, arguments, startToken, currentTokenIndex - 1)
    }

    private fun expression(priority: Int = EXPR_PRIORITY): Expr? {
        if (priority == FACTOR_PRIORITY)
            return factor()

        val startToken = currentTokenIndex

        var curExpr = expression(priority + 1) ?: return null

        while (currentTokenIndex < tokens.size && getPriority(peekOp()) == priority) {
            val op = nextOp()
            val nextExpr = expression(priority + 1) ?: return null
            curExpr = Expr.BinaryOp(op, curExpr, nextExpr, startToken, currentTokenIndex - 1)
        }
        return curExpr
    }

    private fun peekToken(): Token? = if (currentTokenIndex < tokens.size) tokens[currentTokenIndex] else null
    private fun nextToken(): Token? = if (currentTokenIndex < tokens.size) tokens[currentTokenIndex++] else null
    private fun peekOp(): OpToken? = (peekToken() as? OpToken)
    private fun nextOp(): OpToken = (nextToken() as OpToken)

    private fun checkToken(expected: Token): Boolean {
        if (currentTokenIndex >= tokens.size)
            return false
        return peekToken() == expected
    }

    private fun consumeToken(expected: Token) {
        if (checkToken(expected)) {
            nextToken()
        } else {
            expectedTokenError(currentTokenIndex, expected)
        }
    }

    private fun expectEndOfInput() {
        // throwing an exception as it is unreachable state
        if (currentTokenIndex != tokens.size) {
            throw IllegalStateException("Expected end of input.")
        }
    }

    companion object {

        private val startStatementTokens = setOf(
            VarKeywordToken::class,
            IdentifierToken::class,
            IfKeywordToken::class,
            WhileKeywordToken::class,
            LeftBraceToken::class,
            PrintKeywordToken::class,
            FuncKeywordToken::class,
            ReturnKeywordToken::class,
            ProcKeywordToken::class
        )

        private const val EXPR_PRIORITY = 0
        private const val FACTOR_PRIORITY = 5
        private fun getPriority(token: Token?): Int {
            return when (token) {
                is OrOpToken -> 0
                is AndOpToken -> 1
                is RelationalOpToken -> 2
                is PlusOpToken -> 3
                is MinusOpToken -> 3
                is StringOpToken -> 3
                is MulOpToken -> 4
                is DivOpToken -> 4
                else -> -1
            }
        }
    }
}