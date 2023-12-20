package language.semantic

import dataStructures.SpaghettiStack
import language.SemanticError
import language.parser.*
interface Visitor {
    fun getErrors(): List<SemanticError>
    fun visit(node: Stmt.VarDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.Assignment, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.IfStatement, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.WhileStatement, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.Block, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.PrintStatement, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.FuncDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.ReturnStatement, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.ProcDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Stmt.ProcCall, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Parameter, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.BinaryOp, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.UnaryOp, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.FuncCall, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.SymbolName, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.BoolValue, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.StringLiteral, symbolTables: SpaghettiStack<SymbolTable>): Any
    fun visit(node: Expr.Constant, symbolTables: SpaghettiStack<SymbolTable>): Any
}