package language

import language.lexer.OpToken
import language.lexer.Token
import language.lexer.TokenWithOffset
import language.parser.ASTNode

sealed class Error(open val type: ErrorType)

data class LexicalError(val token: TokenWithOffset, override val type: ErrorType) : Error(type)

abstract class SyntaxError(open val tokenIndex: Int, override val type: ErrorType) : Error(type)
data class UnexpectedToken(override val tokenIndex: Int, val token: Token): SyntaxError(tokenIndex, ErrorType.UNEXPECTED_TOKEN)
data class ExpectedToken(override val tokenIndex: Int, val expectedToken: Token, val actualToken: Token?): SyntaxError(tokenIndex, ErrorType.EXPECTED_TOKEN)
data class ExpectedExpression(override val tokenIndex: Int): SyntaxError(tokenIndex, ErrorType.EXPECTED_EXPRESSION)

abstract class SemanticError(open val node: ASTNode, override val type: ErrorType) : Error(type)
data class ConflictingDeclaration(override val node: ASTNode): SemanticError(node, ErrorType.CONFLICTING_DECLARATION)
data class UnresolvedSymbol(override val node: ASTNode): SemanticError(node, ErrorType.UNRESOLVED_SYMBOL)
data class AssignmentTypeMismatch(override val node: ASTNode, val expected: Type, val actual: Type): SemanticError(node, ErrorType.ASSIGNMENT_TYPE_MISMATCH)
data class ConditionTypeMismatch(override val node: ASTNode, val actual: Type): SemanticError(node, ErrorType.CONDITION_TYPE_MISMATCH)
data class MissingReturnStatement(override val node: ASTNode): SemanticError(node, ErrorType.MISSING_RETURN_STATEMENT)
data class UnexpectedReturnStatement(override val node: ASTNode): SemanticError(node, ErrorType.UNEXPECTED_RETURN_STATEMENT)
data class UnresolvedOverload(override val node: ASTNode, val name: String, val providedTypes: List<Type>): SemanticError(node, ErrorType.UNRESOLVED_OVERLOAD)
data class IgnoredReturnValue(override val node: ASTNode): SemanticError(node, ErrorType.IGNORED_RETURN_VALUE)
data class NoReturnValue(override val node: ASTNode): SemanticError(node, ErrorType. NO_RETURN_VALUE)
data class OperandTypeMismatch(override val node: ASTNode, val operator: OpToken, val providedTypes: List<Type>): SemanticError(node, ErrorType.OPERAND_TYPE_MISMATCH)
data class InvalidOperator(override val node: ASTNode, val operator: OpToken): SemanticError(node, ErrorType.INVALID_OPERATOR)
data class OutsideGlobalScopeCallableDeclaration(override val node: ASTNode): SemanticError(node, ErrorType.OUTSIDE_GLOBAL_SCOPE_CALLABLE_DECLARATION)

enum class ErrorType {
    // lexical
    UNEXPECTED_SYMBOL,
    EXPECTED_QUOTE,

    // syntax
    UNEXPECTED_TOKEN,
    EXPECTED_TOKEN,
    EXPECTED_EXPRESSION,

    // semantic
    CONFLICTING_DECLARATION, // variable (including parameters), function or procedure declaration
    UNRESOLVED_SYMBOL, // variable, function or procedure call
    ASSIGNMENT_TYPE_MISMATCH,
    CONDITION_TYPE_MISMATCH, // if, while
    MISSING_RETURN_STATEMENT,
    UNEXPECTED_RETURN_STATEMENT,
    UNRESOLVED_OVERLOAD, // or arg type / number mismatch
    IGNORED_RETURN_VALUE, // func call as proc
    NO_RETURN_VALUE, // proc call in expression
    OPERAND_TYPE_MISMATCH,
    INVALID_OPERATOR, // binary, unary operators
    OUTSIDE_GLOBAL_SCOPE_CALLABLE_DECLARATION
}