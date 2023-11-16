package language.parser

import language.lexer.*

sealed class ASTNode
data class Program(val statements: List<Stmt>): ASTNode()
sealed class Stmt : ASTNode() {
    data class VarDeclaration(val identifier: IdentifierToken, val expr: Expr) : Stmt()
    data class Assignment(val identifier: IdentifierToken, val expr: Expr) : Stmt()
    data class IfStatement(val condition: Expr, val thenBlock: Block, val elseBlock: Block?) : Stmt()
    data class WhileStatement(val condition: Expr, val block: Block) : Stmt()
    data class Block(val statements: List<Stmt>) : Stmt()
    data class PrintStatement(val expr: Expr) : Stmt()
    data class FuncDeclaration(val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block) : Stmt()
    data class ReturnStatement(val expr: Expr) : Stmt()
    data class ProcDeclaration(val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block) : Stmt()
    data class ProcCall(val identifier: IdentifierToken, val arguments: List<Expr>) : Stmt()
}
data class Parameter(val identifier: IdentifierToken, val type: TypeToken) : ASTNode()
sealed class Expr : ASTNode() {
    data class BinaryOp(val operation: OpToken, val leftExpr: Expr, val rightExpr: Expr) : Expr()
    data class UnaryOp(val operation: OpToken, val expr: Expr) : Expr()
    data class FuncCall(val identifier: IdentifierToken, val arguments: List<Expr>) : Expr()
    data class Variable(val name: IdentifierToken) : Expr()
    data class BoolValue(val value: BoolToken) : Expr()
    data class StringLiteral(val value: StringLiteralToken) : Expr()
    data class Constant(val value: ConstantToken) : Expr()
}
