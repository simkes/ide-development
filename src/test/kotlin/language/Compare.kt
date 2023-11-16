package language

import language.lexer.*
import language.parser.*
import java.lang.IllegalStateException

fun compare(token1: Token, token2: Token): Boolean {
    if (token1::class != token2::class) return false
    return when (token1) {
        is IdentifierToken -> token1.name == (token2 as IdentifierToken).name
        is ConstantToken -> token1.value == (token2 as ConstantToken).value
        is StringLiteralToken -> token1.value == (token2 as StringLiteralToken).value
        is BoolToken -> token1.value == (token2 as BoolToken).value
        is NumericRelationOpToken -> token1.operatorSymbol == (token2 as NumericRelationOpToken).operatorSymbol
        else -> true
    }
}

fun compare(nodeList1: List<ASTNode>, nodeList2: List<ASTNode>) : Boolean {
    if (nodeList1.size != nodeList2.size) return false
    nodeList1.zip(nodeList2).forEach { (node1, node2) ->
        if (!compare(node1, node2))
           return false
    }
    return true
}

fun compare(node1: ASTNode?, node2: ASTNode?): Boolean {
    if (node1 == null || node2 == null) return node1 == null && node2 == null
    if (node1::class != node2::class) return false
    return when (node1) {
        is Program -> compareProgram(node1, node2 as Program)
        is Stmt.VarDeclaration -> compareVarDeclaration(node1, node2 as Stmt.VarDeclaration)
        is Stmt.Assignment -> compareAssignment(node1, node2 as Stmt.Assignment)
        is Stmt.IfStatement -> compareIfStatement(node1, node2 as Stmt.IfStatement)
        is Stmt.WhileStatement -> compareWhileStatement(node1, node2 as Stmt.WhileStatement)
        is Stmt.Block -> compareBlock(node1, node2 as Stmt.Block)
        is Stmt.PrintStatement -> comparePrintStatement(node1, node2 as Stmt.PrintStatement)
        is Stmt.FuncDeclaration -> compareFuncDeclaration(node1, node2 as Stmt.FuncDeclaration)
        is Stmt.ReturnStatement -> compareReturnStatement(node1, node2 as Stmt.ReturnStatement)
        is Stmt.ProcDeclaration -> compareProcDeclaration(node1, node2 as Stmt.ProcDeclaration)
        is Stmt.ProcCall -> compareProcCall(node1, node2 as Stmt.ProcCall)
        is Parameter -> compareParameter(node1, node2 as Parameter)
        is Expr.BinaryOp -> compareBinaryOp(node1, node2 as Expr.BinaryOp)
        is Expr.UnaryOp -> compareUnaryOp(node1, node2 as Expr.UnaryOp)
        is Expr.FuncCall -> compareFuncCall(node1, node2 as Expr.FuncCall)
        is Expr.Variable -> compareVariable(node1, node2 as Expr.Variable)
        is Expr.BoolValue -> compareBoolValue(node1, node2 as Expr.BoolValue)
        is Expr.StringLiteral -> compareStringLiteral(node1, node2 as Expr.StringLiteral)
        is Expr.Constant -> compareConstant(node1, node2 as Expr.Constant)
        else -> throw IllegalStateException("Unexpected ast node.")
    }
}

fun compareProgram(node1: Program, node2: Program): Boolean {
    return compare(node1.statements, node2.statements)
}

fun compareVarDeclaration(node1: Stmt.VarDeclaration, node2: Stmt.VarDeclaration): Boolean {
    return compare(node1.identifier, node2.identifier) && compare(node1.expr, node2.expr)
}

fun compareAssignment(node1: Stmt.Assignment, node2: Stmt.Assignment): Boolean {
    return compare(node1.identifier, node2.identifier) && compare(node1.expr, node2.expr)
}
fun compareIfStatement(node1: Stmt.IfStatement, node2: Stmt.IfStatement): Boolean {
    return compare(node1.condition, node2.condition) &&
            compare(node1.thenBlock, node2.thenBlock) &&
            compare(node1.elseBlock, node2.elseBlock)
}

fun compareWhileStatement(node1: Stmt.WhileStatement, node2: Stmt.WhileStatement): Boolean {
    return compare(node1.condition, node2.condition) && compare(node1.block, node2.block)
}

fun compareBlock(node1: Stmt.Block, node2: Stmt.Block): Boolean {
    return compare(node1.statements, node2.statements)
}

fun comparePrintStatement(node1: Stmt.PrintStatement, node2: Stmt.PrintStatement): Boolean {
    return compare(node1.expr, node2.expr)
}

fun compareFuncDeclaration(node1: Stmt.FuncDeclaration, node2: Stmt.FuncDeclaration): Boolean {
    return compare(node1.identifier, node2.identifier) &&
            compare(node1.parameters, node2.parameters) &&
            compare(node1.block, node2.block)
}

fun compareReturnStatement(node1: Stmt.ReturnStatement, node2: Stmt.ReturnStatement): Boolean {
    return compare(node1.expr, node2.expr)
}

fun compareProcDeclaration(node1: Stmt.ProcDeclaration, node2: Stmt.ProcDeclaration): Boolean {
    return compare(node1.identifier, node2.identifier) &&
            compare(node1.parameters, node2.parameters) &&
            compare(node1.block, node2.block)
}

fun compareProcCall(node1: Stmt.ProcCall, node2: Stmt.ProcCall): Boolean {
    return compare(node1.identifier, node2.identifier) && compare(node1.arguments, node2.arguments)
}

fun compareParameter(node1: Parameter, node2: Parameter): Boolean {
    return compare(node1.identifier, node2.identifier) && compare(node1.type, node2.type)
}

fun compareBinaryOp(node1: Expr.BinaryOp, node2: Expr.BinaryOp): Boolean {
    return compare(node1.operation, node2.operation) &&
            compare(node1.leftExpr, node2.leftExpr) &&
            compare(node1.rightExpr, node2.rightExpr)
}

fun compareUnaryOp(node1: Expr.UnaryOp, node2: Expr.UnaryOp): Boolean {
    return compare(node1.operation, node2.operation) && compare(node1.expr, node2.expr)
}

fun compareFuncCall(node1: Expr.FuncCall, node2: Expr.FuncCall): Boolean {
    return compare(node1.identifier, node2.identifier) && compare(node1.arguments, node2.arguments)
}

fun compareVariable(node1: Expr.Variable, node2: Expr.Variable): Boolean {
    return compare(node1.name, node2.name)
}

fun compareBoolValue(node1: Expr.BoolValue, node2: Expr.BoolValue): Boolean {
    return node1.value == node2.value
}

fun compareStringLiteral(node1: Expr.StringLiteral, node2: Expr.StringLiteral): Boolean {
    return node1.value == node2.value
}

fun compareConstant(node1: Expr.Constant, node2: Expr.Constant): Boolean {
    return node1.value == node2.value
}
