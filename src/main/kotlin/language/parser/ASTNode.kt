package language.parser

import dataStructures.SpaghettiStack
import language.lexer.*
import language.semantic.SymbolTable
import language.semantic.Visitor

sealed class ASTNode(open val start: Int, open val end: Int) {
    abstract fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any
}

sealed class Stmt(override val start: Int, override val end: Int) : ASTNode(start, end) {
    data class VarDeclaration(
        val identifier: IdentifierToken,
        val expr: Expr,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Assignment(
        val identifier: IdentifierToken, val expr: Expr,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class IfStatement(
        val condition: Expr, val thenBlock: Block, val elseBlock: Block?,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class WhileStatement(
        val condition: Expr, val block: Block,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Block(
        val statements: List<Stmt>,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class PrintStatement(
        val expr: Expr,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class FuncDeclaration(
        val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ReturnStatement(
        val expr: Expr,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ProcDeclaration(
        val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ProcCall(
        val identifier: IdentifierToken, val arguments: List<Expr>,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class InvalidStatement(
        val errorMessage: String,
        override val start: Int = -1,
        override val end: Int = -1
    ) : Stmt(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }
}

data class Parameter(
    val identifier: IdentifierToken, val type: TypeToken, override val start: Int = -1,
    override val end: Int = -1
) : ASTNode(start, end) {
    override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
        return visitor.visit(this, symbolTables)
    }
}

sealed class Expr(
    override val start: Int,
    override val end: Int
) : ASTNode(start, end) {
    data class BinaryOp(
        val operation: OpToken, val leftExpr: Expr, val rightExpr: Expr, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class UnaryOp(
        val operation: OpToken, val expr: Expr, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class FuncCall(
        val identifier: IdentifierToken, val arguments: List<Expr>, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Variable(
        val name: IdentifierToken, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class BoolValue(
        val value: BoolToken, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class StringLiteral(
        val value: StringLiteralToken, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Constant(
        val value: ConstantToken, override val start: Int = -1,
        override val end: Int = -1
    ) : Expr(start, end) {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }
}
