package language.semantic

import dataStructures.SpaghettiStack
import language.parser.*

// TODO
class SemanticAnalyzer(private val AST: Program) {

    private val scopes = SpaghettiStack<SymbolTable>()

    // order is important!
    private val visitors: List<Visitor> = emptyList()

    // Called recursively for each block or scope statements. First invocation - global scope.
    fun traverse(statements: List<Stmt> = AST.statements) {
        scopes.addNode(SymbolTable())
        for (statement in statements) {
            if (statement is Stmt.Block) {
                traverse(statement.statements)
            } else {
                visitNode(statement)
            }
        }
        scopes.exitNode()
    }

    private fun visitNode(statement: Stmt) {
        for (visitor in visitors) {
            statement.accept(visitor, scopes)
        }
    }
}