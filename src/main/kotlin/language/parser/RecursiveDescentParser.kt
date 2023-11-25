package language.parser

import language.lexer.*
import kotlin.IllegalStateException
import kotlin.reflect.KClass

class RecursiveDescentParser(private val tokens: List<Token>) {

    private var currentTokenIndex = 0

    private val errors = mutableListOf<Stmt.InvalidStatement>()

    fun parse(): Pair<Stmt.Block, List<Stmt.InvalidStatement>> {
        val program = block(false)
        expectEndOfInput()
        return Pair(program, errors)
    }

    private fun statement(): Stmt? {
        val startToken = currentTokenIndex
        if(currentTokenIndex == tokens.size)
            return null
        try {
            return when (val token = peekToken()) {
                is VarKeywordToken -> varDeclaration()
                is IdentifierToken -> assignmentOrProcCall()
                is IfKeywordToken -> ifStatement()
                is WhileKeywordToken -> whileStatement()
                is LeftBraceToken -> block()
                is PrintKeywordToken -> printStatement()
                is FuncKeywordToken -> funcDeclaration()
                is ReturnKeywordToken -> returnStatement()
                is ProcKeywordToken -> procDeclaration()
                else -> throw IllegalArgumentException("Unexpected ${tokenToErrorName(token)}.")
            }
        } catch (e: IllegalArgumentException) {
            if(currentTokenIndex == tokens.size)
                currentTokenIndex -= 1
            val message = if (e.message == null || peekToken() is UnrecognizedToken) {
                ""
            } else {
                e.message!!
            }
            val invalidStmt = Stmt.InvalidStatement(message, startToken, currentTokenIndex)
            currentTokenIndex++
            return invalidStmt
        }
    }

    private fun varDeclaration(): Stmt.VarDeclaration {
        val startToken = currentTokenIndex
        consumeToken(VarKeywordToken)
        val identifierToken = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(AssignToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.VarDeclaration(identifierToken, expression, startToken, currentTokenIndex - 1)
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
        val startToken = currentTokenIndex - 1
        consumeToken(AssignToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.Assignment(identifierToken, expression, startToken, currentTokenIndex - 1)
    }

    private fun procCall(identifierToken: IdentifierToken): Stmt.ProcCall {
        val startToken = currentTokenIndex - 1
        consumeToken(LeftParenToken)
        val arguments = arguments()
        consumeToken(RightParenToken)
        consumeToken(SemicolonToken)
        return Stmt.ProcCall(identifierToken, arguments, startToken, currentTokenIndex - 1)
    }

    private fun ifStatement(): Stmt.IfStatement {
        val startToken = currentTokenIndex
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
            elseBlock,
            startToken,
            currentTokenIndex - 1
        )
    }

    private fun whileStatement(): Stmt.WhileStatement {
        val startToken = currentTokenIndex
        consumeToken(WhileKeywordToken)
        consumeToken(LeftParenToken)
        val condition = expression()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.WhileStatement(condition, block, startToken, currentTokenIndex - 1)
    }

    private fun collectInvalidStatementsInBlock(statements: List<Stmt>): MutableList<Stmt> {
        val newStatements = mutableListOf<Stmt>()
        var invalidStatementEncountered = false
        var lastInvalidStatementEnd = -1

        for (statement in statements) {
            if (statement is Stmt.InvalidStatement) {
                if (!invalidStatementEncountered) {
                    newStatements.add(statement)
                    invalidStatementEncountered = true
                }
                lastInvalidStatementEnd = statement.end
            } else {
                if (invalidStatementEncountered) {
                    addInvalidStatement(newStatements, lastInvalidStatementEnd)
                    invalidStatementEncountered = false
                }
                newStatements.add(statement)
            }
        }

        if (invalidStatementEncountered) {
            addInvalidStatement(newStatements, lastInvalidStatementEnd)
        }

        return newStatements
    }
    private fun addInvalidStatement(newStatements: MutableList<Stmt>, newEnd: Int) {
        val firstInvalidStatement = newStatements.last() as Stmt.InvalidStatement
        newStatements[newStatements.size - 1] = firstInvalidStatement.copy(end = newEnd)
        errors.add(newStatements.last() as Stmt.InvalidStatement)
    }

    private fun block(withBraces: Boolean = true): Stmt.Block {
        val startToken = currentTokenIndex
        if (withBraces)
            consumeToken(LeftBraceToken)
        var statements = mutableListOf<Stmt>()
        while (currentTokenIndex < tokens.size && !checkToken(RightBraceToken)) {
            statements.add(statement() ?: break)
        }
        statements = collectInvalidStatementsInBlock(statements)
        if (withBraces)
            consumeToken(RightBraceToken)
        return Stmt.Block(statements, startToken, currentTokenIndex - 1)
    }

    private fun printStatement(): Stmt.PrintStatement {
        val startToken = currentTokenIndex
        consumeToken(PrintKeywordToken)
        consumeToken(LeftParenToken)
        val expression = expression()
        consumeToken(RightParenToken)
        consumeToken(SemicolonToken)
        return Stmt.PrintStatement(expression, startToken, currentTokenIndex - 1)
    }

    private fun funcDeclaration(): Stmt.FuncDeclaration {
        val startToken = currentTokenIndex
        consumeToken(FuncKeywordToken)
        val name = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(LeftParenToken)
        val parameters = parameters()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.FuncDeclaration(name, parameters, block, startToken, currentTokenIndex - 1)
    }

    private fun returnStatement(): Stmt.ReturnStatement {
        val startToken = currentTokenIndex
        consumeToken(ReturnKeywordToken)
        val expression = expression()
        consumeToken(SemicolonToken)
        return Stmt.ReturnStatement(expression, startToken, currentTokenIndex - 1)
    }

    private fun procDeclaration(): Stmt.ProcDeclaration {
        val startToken = currentTokenIndex
        consumeToken(ProcKeywordToken)
        val name = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
        consumeToken(LeftParenToken)
        val parameters = parameters()
        consumeToken(RightParenToken)
        val block = block()
        return Stmt.ProcDeclaration(name, parameters, block, startToken, currentTokenIndex - 1)
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
                val startToken = currentTokenIndex
                val paramName = consumeTokenOfType(IdentifierToken::class) as IdentifierToken
                consumeToken(ColonToken)
                val paramType = type()
                params.add(Parameter(paramName, paramType, startToken, currentTokenIndex - 1))
            } while (checkToken(CommaToken).also { if (it) consumeToken(CommaToken) })
        }
        return params
    }

    private fun type(): TypeToken {
        return consumeTokenOfType(TypeToken::class) as TypeToken
    }

    private fun factor(): Expr {
        val startToken = currentTokenIndex
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
            is OpToken -> Expr.UnaryOp(token, factor(), startToken, currentTokenIndex - 1)
            else -> {
                currentTokenIndex-= 1
                throw IllegalArgumentException("Empty expression.")
            }
        }
    }

    private fun identifierOrFuncCall(identifierToken: IdentifierToken): Expr {
        return if (checkToken(LeftParenToken)) {
            funcCall(identifierToken)
        } else {
            Expr.Variable(identifierToken, currentTokenIndex - 1, currentTokenIndex - 1)
        }
    }

    private fun funcCall(identifierToken: IdentifierToken): Expr.FuncCall {
        val startToken = currentTokenIndex - 1
        consumeToken(LeftParenToken)
        val arguments = arguments()
        consumeToken(RightParenToken)
        return Expr.FuncCall(identifierToken, arguments, startToken, currentTokenIndex - 1)
    }

    private fun expression(priority: Int = EXPR_PRIORITY): Expr {
        if (priority == FACTOR_PRIORITY)
            return factor()
        val startToken = currentTokenIndex
        var curExpr = expression(priority + 1)
        while (getPriority(peekOp()) == priority) {
            val op = nextOp()
            val nextExpr = expression(priority + 1)
            curExpr = Expr.BinaryOp(op, curExpr, nextExpr, startToken, currentTokenIndex - 1)
        }
        return curExpr
    }

    private fun peekToken(): Token = tokens[currentTokenIndex]
    private fun nextToken(): Token = tokens[currentTokenIndex++]
    private fun peekOp(): OpToken? = (peekToken() as? OpToken)
    private fun nextOp(): OpToken = (nextToken() as OpToken)

    private fun checkToken(expected: Token): Boolean = peekToken() == expected

    private fun consumeToken(expected: Token) {
        if(currentTokenIndex == tokens.size)
            throw IllegalArgumentException("Expected: ${tokenToErrorName(expected)}.")
        if (peekToken() == expected) {
            currentTokenIndex++
        } else {
            throw IllegalArgumentException("Expected: ${tokenToErrorName(expected)}.")
        }
    }

    private fun consumeTokenOfType(expected: KClass<out Token>): Token {
        if(currentTokenIndex == tokens.size)
            throw IllegalArgumentException("Expected type: ${expected.simpleName}.")
        val currentToken = peekToken()
        if (expected.isInstance(currentToken)) {
            currentTokenIndex++
            return currentToken
        } else {
            throw IllegalArgumentException(
                "Expected type: ${expected.simpleName}, but found: ${
                    tokenToErrorName(
                        currentToken
                    )
                }."
            )
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
        is UnrecognizedToken -> "unrecognized token"
        else -> throw IllegalStateException("Null token.")
    }
}