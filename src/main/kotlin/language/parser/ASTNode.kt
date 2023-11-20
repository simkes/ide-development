package language.parser

import dataStructures.SpaghettiStack
import language.lexer.*
import language.semantic.SymbolTable
import language.semantic.Visitor

sealed class ASTNode {
    abstract fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any
}

data class Program(val statements: List<Stmt>) : ASTNode() {
    override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
        return visitor.visit(this, symbolTables)
    }
}

sealed class Stmt : ASTNode() {
    data class VarDeclaration(val identifier: IdentifierToken, val expr: Expr) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Assignment(val identifier: IdentifierToken, val expr: Expr) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class IfStatement(val condition: Expr, val thenBlock: Block, val elseBlock: Block?) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class WhileStatement(val condition: Expr, val block: Block) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Block(val statements: List<Stmt>) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class PrintStatement(val expr: Expr) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class FuncDeclaration(val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block) :
        Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ReturnStatement(val expr: Expr) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ProcDeclaration(val identifier: IdentifierToken, val parameters: List<Parameter>, val block: Block) :
        Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class ProcCall(val identifier: IdentifierToken, val arguments: List<Expr>) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class InvalidStatement(val errorMessage: String) : Stmt() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }
}

data class Parameter(val identifier: IdentifierToken, val type: TypeToken) : ASTNode() {
    override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
        return visitor.visit(this, symbolTables)
    }
}

sealed class Expr : ASTNode() {
    data class BinaryOp(val operation: OpToken, val leftExpr: Expr, val rightExpr: Expr) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class UnaryOp(val operation: OpToken, val expr: Expr) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class FuncCall(val identifier: IdentifierToken, val arguments: List<Expr>) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Variable(val name: IdentifierToken) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class BoolValue(val value: BoolToken) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class StringLiteral(val value: StringLiteralToken) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }

    data class Constant(val value: ConstantToken) : Expr() {
        override fun accept(visitor: Visitor, symbolTables: SpaghettiStack<SymbolTable>): Any {
            return visitor.visit(this, symbolTables)
        }
    }
}
