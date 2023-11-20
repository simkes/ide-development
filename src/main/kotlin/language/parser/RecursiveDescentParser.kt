package language.parser

import language.lexer.*
import kotlin.IllegalStateException
import kotlin.reflect.KClass

class RecursiveDescentParser(private val tokens: List<Token>) {

    private var currentTokenIndex = 0
    var parsedWithError = false
        private set

    fun parse(): Program {
        val statements = mutableListOf<Stmt>()
        var statement = statement()
        while (statement != null) {
            statements.add(statement)
            statement = statement()
        }
        expectEndOfInput()
        return Program(statements)
    }

    private fun statement(): Stmt? {
        if (currentTokenIndex == tokens.size) return null
        return try {
            when (peekToken()) {
                is VarKeywordToken -> varDeclaration()
                is IdentifierToken -> assignmentOrProcCall()
                is IfKeywordToken -> ifStatement()
                is WhileKeywordToken -> whileStatement()
                is LeftBraceToken -> block()
                is PrintKeywordToken -> printStatement()
                is FuncKeywordToken -> funcDeclaration()
                is ReturnKeywordToken -> returnStatement()
                is ProcKeywordToken -> procDeclaration()
                else -> throw IllegalArgumentException("Unexpected token ${tokenToErrorName(peekToken())} at position $currentTokenIndex.")
            }
        } catch (e: IllegalArgumentException) {
            parsedWithError = true
            currentTokenIndex++
            return Stmt.InvalidStatement(e.message ?: "");
        }

    }

    private fun varDeclaration(): Stmt.VarDeclaration {
        consumeToken(VarKeywordToken)
        val identifierToken = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(AssignToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.VarDeclaration(identifierToken, expression)
    }

    private fun assignmentOrProcCall(): Stmt {
        val identifierToken = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        return if (checkToken(AssignToken)) {
            assignment(identifierToken)
        } else {
            procCall(identifierToken)
        }
    }

    private fun assignment(identifierToken: IdentifierToken): Stmt.Assignment {
        consumeToken(AssignToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.Assignment(identifierToken, expression)
    }

    private fun procCall(identifierToken: IdentifierToken): Stmt.ProcCall {
        consumeToken(LeftParenToken)
        val arguments = arguments()
        consumeToken(RightParenToken)
        consumeToken(SemicolonToken)
        return Stmt.ProcCall(identifierToken, arguments)
    }

    private fun ifStatement(): Stmt.IfStatement {
        consumeToken(IfKeywordToken)
        consumeToken(LeftParenToken)
        val condition = expression()
        consumeToken(RightParenToken)
        val block = block()
        var elseBlock: Stmt.Block? = null
        if (checkToken(ElseKeywordToken)) {
            consumeToken(ElseKeywordToken)
            elseBlock = block()
        }
        return Stmt.IfStatement(
            condition,
            block,
            elseBlock
        )
    }

    private fun whileStatement(): Stmt.WhileStatement {
        consumeToken(WhileKeywordToken)
        consumeToken(LeftParenToken)
        val condition = expression()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.WhileStatement(condition, block)
    }

    private fun block(): Stmt.Block {
        consumeToken(LeftBraceToken)
        val statements = mutableListOf<Stmt>()
        while (!checkToken(RightBraceToken)) {
            statements.add(statement() ?: break)
        }
        consumeToken(RightBraceToken)
        return Stmt.Block(statements)
    }

    private fun printStatement(): Stmt.PrintStatement {
        consumeToken(PrintKeywordToken)
        consumeToken(LeftParenToken)
        val expression = expression()
        consumeToken(RightParenToken)
        consumeToken(SemicolonToken)
        return Stmt.PrintStatement(expression)
    }

    private fun funcDeclaration(): Stmt.FuncDeclaration {
        consumeToken(FuncKeywordToken)
        val name = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(LeftParenToken)
        val parameters = parameters()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.FuncDeclaration(name, parameters, block)
    }

    private fun returnStatement(): Stmt.ReturnStatement {
        consumeToken(ReturnKeywordToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.ReturnStatement(expression)
    }

    private fun procDeclaration(): Stmt.ProcDeclaration {
        consumeToken(ProcKeywordToken)
        val name = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(LeftParenToken)
        val parameters = parameters()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.ProcDeclaration(name, parameters, block)
    }

    private fun arguments(): List<Expr> {
        val args = mutableListOf<Expr>()
        if (!checkToken(RightParenToken)) {
            do {
                args.add(expression())
            } while (checkToken(CommaToken).also { if (it) consumeToken(CommaToken) })
        }
        return args
    }

    private fun parameters(): List<Parameter> {
        val params = mutableListOf<Parameter>()
        if (!checkToken(RightParenToken)) {
            do {
                val paramName = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
                consumeToken(ColonToken)
                val paramType = type()
                params.add(Parameter(paramName, paramType))
            } while (checkToken(CommaToken).also { if (it) consumeToken(CommaToken) })
        }
        return params
    }

    private fun type(): TypeToken {
        return consumeTokenOfType(TypeToken::class) as TypeToken
    }

    private fun factor(): Expr {
        return when (val token = nextToken()) {
            is BoolToken -> Expr.BoolValue(token)
            is StringLiteralToken -> Expr.StringLiteral(token)
            is ConstantToken -> Expr.Constant(token)
            is IdentifierToken -> identifierOrFuncCall(token)
            is LeftParenToken -> {
                val expr = expression()
                consumeToken(RightParenToken)
                expr
            }

            is OpToken -> Expr.UnaryOp(token, factor())
            else -> throw IllegalStateException("Empty expression factor?")
        }
    }

    private fun identifierOrFuncCall(identifierToken: IdentifierToken): Expr {
        return if (checkToken(LeftParenToken)) {
            funcCall(identifierToken)
        } else {
            Expr.Variable(identifierToken)
        }
    }

    private fun funcCall(identifierToken: IdentifierToken): Expr.FuncCall {
        consumeToken(LeftParenToken)
        val arguments = arguments()
        consumeToken(RightParenToken)
        return Expr.FuncCall(identifierToken, arguments)
    }

    private fun expression(priority: Int = EXPR_PRIORITY): Expr {
        if (priority == FACTOR_PRIORITY)
            return factor()
        var curExpr = expression(priority + 1)
        while (getPriority(peekOp()) == priority) {
            val op = nextOp()
            val nextExpr = expression(priority + 1)
            curExpr = Expr.BinaryOp(op, curExpr, nextExpr)
        }
        return curExpr
    }

    private fun peekToken(): Token = tokens[currentTokenIndex]
    private fun nextToken(): Token = tokens[currentTokenIndex++]
    private fun peekOp(): OpToken? = (peekToken() as? OpToken)
    private fun nextOp(): OpToken = (nextToken() as OpToken)

    private fun checkToken(expected: Token): Boolean = peekToken() == expected

    private fun consumeToken(expected: Token) {
        if (peekToken() == expected) {
            currentTokenIndex++
        } else {
            throw IllegalArgumentException(
                "Expected token: ${tokenToErrorName(expected)}, but found: ${
                    tokenToErrorName(
                        peekToken()
                    )
                }."
            )
        }
    }

    private fun consumeTokenOfType(expected: KClass<out Token>): Token {
        val currentToken = peekToken()
        if (expected.isInstance(currentToken)) {
            currentTokenIndex++
            return currentToken
        } else {
            throw IllegalArgumentException("Expected token of type: ${expected.simpleName}, but found: ${currentToken::class.simpleName}.")
        }
    }

    private fun expectEndOfInput() {
        // throwing an exception as it is unreachable state
        if (currentTokenIndex != tokens.size) {
            throw IllegalStateException(
                "Unexpected token at position $currentTokenIndex: ${
                    tokenToErrorName(
                        peekToken()
                    )
                }"
            )
        }
    }

    companion object {
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

// for errors printing
fun tokenToErrorName(token: Token?): String {
    return when (token) {
        is IdentifierToken -> "name"
        is ConstantToken -> "number"
        is StringLiteralToken -> "string literal"
        is BoolToken -> "boolean"
        is OpToken -> "operation"
        is LeftParenToken -> "("
        is RightParenToken -> ")"
        is LeftBraceToken -> "{"
        is RightBraceToken -> "}"
        is SemicolonToken -> ";"
        is CommaToken -> ","
        is ColonToken -> ":"
        is AssignToken -> "="
        is VarKeywordToken -> "var"
        is IfKeywordToken -> "if"
        is ElseKeywordToken -> "else"
        is WhileKeywordToken -> "while"
        is FuncKeywordToken -> "func"
        is ProcKeywordToken -> "proc"
        is ReturnKeywordToken -> "return"
        is PrintKeywordToken -> "print"
        is TypeToken -> "type"
        else -> throw IllegalStateException("Unrecognized token.")
    }
}