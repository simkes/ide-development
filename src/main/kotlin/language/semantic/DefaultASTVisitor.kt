package language.semantic

import dataStructures.SpaghettiStack
import language.*
import language.lexer.*
import language.parser.*


class DefaultASTVisitor : Visitor {

    private val semanticErrors = mutableListOf<SemanticError>()
    override fun getErrors(): List<SemanticError> = semanticErrors
    override fun visit(node: Stmt.VarDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val exprType = node.expr.accept(this, symbolTables) as Type

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        val prevStWithDeclaration = symbolTables.lookUpInParentChain { st -> st.containsVariable(identifierName) }
        if (prevStWithDeclaration != null) {
            semanticErrors.add(ConflictingDeclaration(symbol))
            return Type.UNKNOWN
        }

        symbolTables.currentValue()!!.defineVariable(TypedSymbol(identifierName, exprType))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.Assignment, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val exprType = node.expr.accept(this, symbolTables) as Type

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        val stWithDeclaration = symbolTables.lookUpInParentChain { st -> st.containsVariable(identifierName) }
        if (stWithDeclaration == null) {
            semanticErrors.add(UnresolvedSymbol(symbol))
        } else {
            val varType = (stWithDeclaration.resolveVariable(identifierName)!!).type
            if (varType != Type.UNKNOWN && exprType != Type.UNKNOWN && varType != exprType) {
                semanticErrors.add(AssignmentTypeMismatch(node, varType, exprType))
            }
        }

        return Type.UNKNOWN;
    }

    override fun visit(node: Stmt.IfStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val condType = node.condition.accept(this, symbolTables) as Type

        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            semanticErrors.add(ConditionTypeMismatch(node.condition, condType))
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
            semanticErrors.add(ConditionTypeMismatch(node.condition, condType))
        }

        node.block.accept(this, symbolTables)

        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.Block, symbolTables: SpaghettiStack<SymbolTable>): Pair<Type, Boolean> {
        val scope = if (symbolTables.currentValue() == null) Scope.GLOBAL_SCOPE else Scope.BLOCK_SCOPE
        symbolTables.addNode(SymbolTable(scope))

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
        if (symbolTables.currentValue()!!.scope != Scope.GLOBAL_SCOPE) {
            semanticErrors.add(OutsideGlobalScopeCallableDeclaration(node))
            return Type.UNKNOWN
        }

        val parameterTypes = node.parameters.map { param -> param.accept(this, symbolTables) as Type }

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        var conflictingDeclaration = false

        if (symbolTables.lookUpInParentChain { st -> st.containsOverload(identifierName, parameterTypes) } != null) {
            semanticErrors.add(ConflictingDeclaration(symbol))
            conflictingDeclaration = true
        }

        symbolTables.addNode(SymbolTable(Scope.FUNC_SCOPE))
        node.parameters.forEach { param ->
            val paramSymbol = param.symbol
            val prevStWithDeclaration =
                symbolTables.lookUpInParentChain { st -> st.containsVariable(paramSymbol.identifier.name) }
            if (prevStWithDeclaration != null) {
                semanticErrors.add(ConflictingDeclaration(paramSymbol))
            } else {
                symbolTables.currentValue()!!.defineVariable(TypedSymbol(paramSymbol.identifier.name, param.type))
            }
        }

        val (returnType, returnStmt) = node.block.accept(this, symbolTables) as Pair<Type, Boolean>

        symbolTables.exitNode()

        if (!returnStmt) {
            semanticErrors.add(MissingReturnStatement(node))
        }

        val parameters =
            node.parameters.zip(parameterTypes) { param, type -> TypedSymbol(param.symbol.identifier.name, type) }
        if (!conflictingDeclaration)
            symbolTables.currentValue()!!.defineOverload(FuncSymbol(identifierName, parameters, returnType))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.ProcDeclaration, symbolTables: SpaghettiStack<SymbolTable>): Type {
        if (symbolTables.currentValue()!!.scope != Scope.GLOBAL_SCOPE) {
            semanticErrors.add(OutsideGlobalScopeCallableDeclaration(node))
            return Type.UNKNOWN
        }

        val parameterTypes = node.parameters.map { param -> param.accept(this, symbolTables) as Type }

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        var conflictingDeclaration = false

        if (symbolTables.lookUpInParentChain { st -> st.containsOverload(identifierName, parameterTypes) } != null) {
            semanticErrors.add(ConflictingDeclaration(symbol))
            conflictingDeclaration = true
        }

        symbolTables.addNode(SymbolTable(Scope.PROC_SCOPE))
        node.parameters.forEach { param ->
            val paramSymbol = param.symbol
            val prevStWithDeclaration =
                symbolTables.lookUpInParentChain { st -> st.containsVariable(paramSymbol.identifier.name) }
            if (prevStWithDeclaration != null) {
                semanticErrors.add(ConflictingDeclaration(paramSymbol))
            } else {
                symbolTables.currentValue()!!.defineVariable(TypedSymbol(paramSymbol.identifier.name, param.type))
            }
        }
        node.block.accept(this, symbolTables)
        symbolTables.exitNode()

        val parameters =
            node.parameters.zip(parameterTypes) { param, type -> TypedSymbol(param.symbol.identifier.name, type) }

        if (!conflictingDeclaration)
            symbolTables.currentValue()!!.defineOverload(ProcSymbol(identifierName, parameters))
        return Type.UNKNOWN
    }

    override fun visit(node: Stmt.ReturnStatement, symbolTables: SpaghettiStack<SymbolTable>): Type {
        if (symbolTables.lookUpInParentChain { st -> st.scope == Scope.FUNC_SCOPE } == null) {
            semanticErrors.add(UnexpectedReturnStatement(node))
        }
        return node.expr.accept(this, symbolTables) as Type
    }

    override fun visit(node: Stmt.ProcCall, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val argTypes = node.arguments.map { arg -> arg.accept(this, symbolTables) as Type }
        if (argTypes.any { it == Type.UNKNOWN }) {
            return Type.UNKNOWN
        }

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        val stWithDecl = symbolTables.lookUpInParentChain { it.containsFunction(identifierName) }
        if (stWithDecl == null) {
            semanticErrors.add(UnresolvedSymbol(symbol))
            return Type.UNKNOWN
        }

        val st = symbolTables.lookUpInParentChain { it.containsOverload(identifierName, argTypes) }
        if (st == null) {
            semanticErrors.add(UnresolvedOverload(node, identifierName, argTypes))
            return Type.UNKNOWN
        }

        if (st.resolveOverload(identifierName, argTypes) is FuncSymbol) {
            semanticErrors.add(IgnoredReturnValue(node))
        }
        return Type.UNKNOWN
    }

    override fun visit(node: Parameter, symbolTables: SpaghettiStack<SymbolTable>): Type {
        return node.type
    }

    override fun visit(node: Expr.BinaryOp, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val leftType = node.leftExpr.accept(this, symbolTables) as Type
        val rightType = node.rightExpr.accept(this, symbolTables) as Type

        if (leftType == Type.UNKNOWN || rightType == Type.UNKNOWN)
            return Type.UNKNOWN

        if (!node.operation.binary) {
            semanticErrors.add(InvalidOperator(node, node.operation))
        }

        return when {
            node.operation is ArithmeticOpToken && leftType == Type.NUMBER && rightType == Type.NUMBER -> Type.NUMBER
            node.operation is BoolOpToken && leftType == Type.BOOL && rightType == Type.BOOL -> Type.BOOL
            node.operation is StringOpToken && leftType == Type.STRING && rightType == Type.STRING -> Type.STRING
            node.operation is EqOpToken || node.operation is NotEqOpToken && leftType == rightType -> Type.BOOL
            node.operation is NumericRelationOpToken && leftType == Type.NUMBER && rightType == Type.NUMBER -> Type.BOOL
            else -> {
                semanticErrors.add(OperandTypeMismatch(node, node.operation, listOf(leftType, rightType)))
                Type.UNKNOWN
            }
        }
    }

    override fun visit(node: Expr.UnaryOp, symbolTables: SpaghettiStack<SymbolTable>): Type {
        if (!node.operation.unary) {
            semanticErrors.add(InvalidOperator(node, node.operation))
            return Type.UNKNOWN
        }

        val exprType = node.expr.accept(this, symbolTables) as Type
        if (exprType == Type.UNKNOWN)
            return exprType

        if (node.operation is ArithmeticOpToken && exprType != Type.NUMBER ||
            node.operation is BoolOpToken && exprType != Type.BOOL
        ) {
            semanticErrors.add(OperandTypeMismatch(node, node.operation, listOf(exprType)))
            return Type.UNKNOWN
        }

        return exprType
    }

    override fun visit(node: Expr.FuncCall, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val argTypes = node.arguments.map { arg -> arg.accept(this, symbolTables) as Type }
        if (argTypes.any { it == Type.UNKNOWN }) {
            return Type.UNKNOWN
        }

        val symbol = node.symbol
        val identifierName = symbol.identifier.name

        val stWithDecl = symbolTables.lookUpInParentChain { it.containsFunction(identifierName) }
        if (stWithDecl == null) {
            semanticErrors.add(UnresolvedSymbol(symbol))
            return Type.UNKNOWN
        }

        val st = symbolTables.lookUpInParentChain { it.containsOverload(identifierName, argTypes) }
        if (st == null) {
            semanticErrors.add(UnresolvedOverload(node, identifierName, argTypes))
            return Type.UNKNOWN
        }

        if (st.resolveOverload(identifierName, argTypes) is ProcSymbol) {
            semanticErrors.add(NoReturnValue(node))
            return Type.UNKNOWN
        }

        return (st.resolveOverload(identifierName, argTypes)!! as FuncSymbol).returnType
    }

    override fun visit(node: Expr.SymbolName, symbolTables: SpaghettiStack<SymbolTable>): Type {
        val name = node.identifier.name

        val st = symbolTables.lookUpInParentChain { st -> st.containsVariable(name) }
        if (st == null) {
            semanticErrors.add(UnresolvedSymbol(node))
            return Type.UNKNOWN
        }

        return st.resolveVariable(name)!!.type
    }

    override fun visit(node: Expr.BoolValue, symbolTables: SpaghettiStack<SymbolTable>) = Type.BOOL
    override fun visit(node: Expr.StringLiteral, symbolTables: SpaghettiStack<SymbolTable>) = Type.STRING
    override fun visit(node: Expr.Constant, symbolTables: SpaghettiStack<SymbolTable>) = Type.NUMBER
}