package language.semantic

import dataStructures.SpaghettiStack
import language.lexer.*
import language.parser.*


class DefaultASTVisitor : Visitor {

    private val semanticErrors = mutableListOf<SemanticError>()
    override fun getErrors(): List<SemanticError> = semanticErrors
    override fun visit(node: Stmt.VarDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val exprType = node.expr.accept(this, symbolTables) as Type
        val name = node.identifier.name
        if (symbolTables.lookUpInParentChain { st -> st.contains(name) } != null) {
            semanticErrors.add(SemanticError(node, "Conflicting declaration of $name."))
        }
        symbolTables.currentValue()!!.define(TypedSymbol(name, exprType))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.Assignment, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val name = node.identifier.name
        val st = symbolTables.lookUpInParentChain { st -> st.contains(name) }
        if (st == null) {
            semanticErrors.add(SemanticError(node, "Unresolved variable $name."))
            return Type.UNKNOWN
        }
        val varType = (st.resolve(name)!! as TypedSymbol).type
        val exprType = node.expr.accept(this, symbolTables) as Type
        if (varType != Type.UNKNOWN && exprType != Type.UNKNOWN && varType != exprType) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Type mismatch. Expected: ${typeToErrorName.getValue(varType)}, got: ${typeToErrorName[exprType]}"
                )
            )
        }
        return Type.UNKNOWN;
    }

    override fun visit(node: Stmt.IfStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val condType = node.condition.accept(this, symbolTables) as Type
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Expected type: ${typeToErrorName[Type.BOOL]}, got: ${typeToErrorName[condType]}."
                )
            )
        }
        node.thenBlock.accept(this, symbolTables)
        if (node.elseBlock != null) {
            node.elseBlock.accept(this, symbolTables)
        }
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.WhileStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val condType = node.condition.accept(this, symbolTables) as Type
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Expected type: ${typeToErrorName[Type.BOOL]}, got: ${typeToErrorName[condType]}."
                )
            )
        }
        node.block.accept(this, symbolTables)
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.Block, symbolTables: SpaghettiStack<SymbolTable>): Pair<Type, Boolean> {
        symbolTables.addNode(SymbolTable())
        var returnStatementEncountered = false
        var type = Type.UNKNOWN
        for (statement in node.statements) {
            if (statement is Stmt.ReturnStatement) {
                val returnType = statement.accept(this, symbolTables) as Type
                returnStatementEncountered = true
                type = returnType
                continue
            }
            statement.accept(this, symbolTables)
        }
        symbolTables.exitNode()
        return Pair(type, returnStatementEncountered)
    }

    override fun visit(node: Stmt.PrintStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        node.expr.accept(this, symbolTables)
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.FuncDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val parameterTypes = node.parameters.map { param -> param.accept(this, symbolTables) as Type }
        val parameters = node.parameters.zip(parameterTypes) { param, type -> TypedSymbol(param.identifier.name, type) }
        val name = node.identifier.name
        if (symbolTables.lookUpInParentChain { st -> st.contains(name, parameterTypes) } != null) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Conflicting declaration of function ${
                        SymbolTable.generateCallableSignature(name, parameterTypes)
                    }."
                )
            )
            return Type.UNKNOWN
        }
        symbolTables.addNode(SymbolTable())
        parameters.forEach { param -> symbolTables.currentValue()!!.define(param) }
        val (type, returnStmt) = node.block.accept(this, symbolTables) as Pair<Type, Boolean>
        symbolTables.exitNode()
        if (!returnStmt) {
            semanticErrors.add(SemanticError(node, "A 'return' expression required in a function body."))
        }
        symbolTables.currentValue()!!.define(FuncSymbol(name, parameters, type))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.ReturnStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        return node.expr.accept(this, symbolTables) as Type
    }

    override fun visit(node: Stmt.ProcDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val parameterTypes = node.parameters.map { param -> param.accept(this, symbolTables) as Type }
        val parameters = node.parameters.zip(parameterTypes) { param, type -> TypedSymbol(param.identifier.name, type) }
        val name = node.identifier.name
        if (symbolTables.lookUpInParentChain { st -> st.contains(name, parameterTypes) } != null) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Conflicting declaration of procedure ${
                        SymbolTable.generateCallableSignature(name, parameterTypes)
                    }."
                )
            )
            return Type.UNKNOWN
        }
        symbolTables.addNode(SymbolTable())
        parameters.forEach { param -> symbolTables.currentValue()!!.define(param) }
        val (_, returnStmt) = node.block.accept(this, symbolTables) as Pair<*, Boolean>
        symbolTables.exitNode()
        if (returnStmt) {
            semanticErrors.add(SemanticError(node, "Unexpected 'return' in a procedure body."))
        }
        symbolTables.currentValue()!!.define(ProcSymbol(name, parameters))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.ProcCall, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val name = node.identifier.name
        val argTypes = node.arguments.map { arg -> arg.accept(this, symbolTables) as Type }
        if (argTypes.any { it == Type.UNKNOWN }) {
            return Type.UNKNOWN
        }
        val st = symbolTables.lookUpInParentChain { it.contains(name, argTypes) }
        if (st == null) {
            semanticErrors.add(
                SemanticError(
                    node, "Unresolved overload of procedure ${
                        SymbolTable.generateCallableSignature(name, argTypes)
                    }."
                )
            )
        }
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.InvalidStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        return Type.UNKNOWN
    }

    override fun visit(node: Parameter, symbolTables: SpaghettiStack<SymbolTable>): Type {
        return tokenToType[node.type]!!
    }

    override fun visit(node: Expr.BinaryOp, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val leftType = node.leftExpr.accept(this, symbolTables) as Type
        val rightType = node.rightExpr.accept(this, symbolTables) as Type
        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN)
            return Type.UNKNOWN
        if (!node.operation.binary) {
            semanticErrors.add(SemanticError(node, "Expected binary operation."))
        }
        return when {
            node.operation is ArithmeticOpToken && leftType == Type.NUMBER && rightType == Type.NUMBER -> Type.NUMBER
            node.operation is BoolOpToken && leftType == Type.BOOL && rightType == Type.BOOL -> Type.BOOL
            node.operation is StringOpToken && leftType == Type.STRING && rightType == Type.STRING -> Type.STRING
            node.operation is EqOpToken || node.operation is NotEqOpToken && leftType == rightType -> Type.BOOL
            node.operation is NumericRelationOpToken && leftType == Type.NUMBER && rightType == Type.NUMBER -> Type.BOOL
            else -> {
                // TODO: type mismatch of args or operation with args
                semanticErrors.add(SemanticError(node, "Invalid binary operation."))
                Type.UNKNOWN
            }
        }
    }

    override fun visit(node: Expr.UnaryOp, symbolTables: SpaghettiStack<SymbolTable>): Type {
        if (!node.operation.unary) {
            semanticErrors.add(SemanticError(node, "Unexpected operation."))
            return Type.UNKNOWN
        }
        val exprType = node.expr.accept(this, symbolTables) as Type
        if (exprType == Type.UNKNOWN)
            return exprType
        if (node.operation is ArithmeticOpToken && exprType != Type.NUMBER) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Type mismatch. Expected: ${typeToErrorName[Type.NUMBER]}, got: ${typeToErrorName[exprType]}."
                )
            )
            return Type.UNKNOWN
        }
        if (node.operation is BoolOpToken && exprType != Type.BOOL) {
            semanticErrors.add(
                SemanticError(
                    node,
                    "Type mismatch. Expected: ${typeToErrorName[Type.BOOL]}, got: ${typeToErrorName[exprType]}."
                )
            )
            return Type.UNKNOWN
        }
        return exprType
    }

    override fun visit(node: Expr.FuncCall, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val name = node.identifier.name
        val argTypes = node.arguments.map { arg -> arg.accept(this, symbolTables) as Type }
        if (argTypes.any { it == Type.UNKNOWN }) {
            return Type.UNKNOWN
        }
        val st = symbolTables.lookUpInParentChain { it.contains(name, argTypes) }
        if (st == null) {
            semanticErrors.add(
                SemanticError(
                    node, "Unresolved overload of function ${
                        SymbolTable.generateCallableSignature(name, argTypes)
                    }."
                )
            )
            return Type.UNKNOWN
        }
        return (st.resolve(name, argTypes)!! as FuncSymbol).returnType
    }

    override fun visit(node: Expr.Variable, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val name = node.name.name
        val st = symbolTables.lookUpInParentChain { st -> st.contains(name) }
        if (st == null) {
            semanticErrors.add(SemanticError(node, "Unresolved variable $name."))
            return Type.UNKNOWN
        }
        return (st.resolve(name) as TypedSymbol).type
    }

    override fun visit(node: Expr.BoolValue, symbolTables: SpaghettiStack<SymbolTable>) = Type.BOOL
    override fun visit(node: Expr.StringLiteral, symbolTables: SpaghettiStack<SymbolTable>) = Type.STRING
    override fun visit(node: Expr.Constant, symbolTables: SpaghettiStack<SymbolTable>) = Type.NUMBER

    private val typeToErrorName = mapOf(
        Type.NUMBER to "number",
        Type.STRING to "string",
        Type.BOOL to "bool",
        Type.UNKNOWN to "unresolved"
    )
}