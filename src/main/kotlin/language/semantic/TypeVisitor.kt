package language.semantic

import dataStructures.SpaghettiStack
import language.parser.*

// TODO - saved as draft version. Implement after completing lexical and syntax analysis. Need a way to bind AST nodes with errors.


//data class Error(val errorMessage: String)
//class TypeVisitor : Visitor {
//    override fun visit(node: Stmt.VarDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        val result = node.expr.accept(this, symbolTables)
//        if (result is Error) return result
//        val type = result as Type
//        val name = node.identifier.name
//        if (symbolTables.lookUpInParentChain { st -> st.contains(name) } != null) {
//            return Error("Conflicting declarations of variable $name.")
//        }
//        symbolTables.currentValue()!!.define(TypedSymbol(name, type))
//        return Unit
//    }
//
//    override fun visit(node: Stmt.Assignment, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        val result = node.expr.accept(this, symbolTables)
//        if (result is Error) return result
//        val expressionType = result as Type
//        val name = node.identifier.name
//        val st = symbolTables.lookUpInParentChain { st -> st.contains(name) }
//            ?: return Error("Unresolved variable $name.")
//        val varType = (st.resolve(name)!! as TypedSymbol).type
//        return if (varType != expressionType) {
//            Error("Type mismatch: expected $varType, found $expressionType.")
//        } else {
//            Unit
//        }
//    }
//
//    override fun visit(node: Stmt.IfStatement, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        val condResult = node.condition.accept(this, symbolTables)
//        if (condResult is Error) return condResult
//        val type = condResult as Type
//        if (type != Type.BOOL) {
//            return Error("Type mismatch: expected ${Type.BOOL}, found $type.")
//        }
//        val thenBlockRes = node.thenBlock.accept(this, symbolTables)
//        if (thenBlockRes is Error) return thenBlockRes
//        if (node.elseBlock != null) {
//            return node.elseBlock.accept(this, symbolTables)
//        }
//        return Unit
//    }
//
//    override fun visit(node: Stmt.WhileStatement, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        val condResult = node.condition.accept(this, symbolTables)
//        if (condResult is Error) return condResult
//        val type = condResult as Type
//        if (type != Type.BOOL) {
//            return Error("Type mismatch: expected ${Type.BOOL}, found $type.")
//        }
//        return node.block.accept(this, symbolTables)
//    }
//
//    override fun visit(node: Stmt.Block, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Stmt.PrintStatement, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        val result = node.expr.accept(this, symbolTables)
//        return if (result is Error) {
//            result
//        } else {
//            Unit
//        }
//    }
//
//    override fun visit(node: Stmt.FuncDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Stmt.ReturnStatement, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Stmt.ProcDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Stmt.ProcCall, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Parameter, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        return tokenToType.getValue(node.type)
//    }
//
//    override fun visit(node: Expr.BinaryOp, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.UnaryOp, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.FuncCall, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.Variable, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.BoolValue, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.StringLiteral, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Expr.Constant, symbolTables: SpaghettiStack<SymbolTable>): Any {
//        TODO("Not yet implemented")
//    }
//
//
//}