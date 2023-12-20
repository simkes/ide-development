package language

import language.lexer.*
import language.parser.Expr
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AnalysisErrorTest {

    private fun getCodeErrors(code: String): List<Error> {
        val (_, errors) = CodeAnalyzer.analyze(code, Level.SEMANTIC)
        return errors
    }

    private fun runTest(code: String, expected: List<Error>) {
        val actual = getCodeErrors(code)
        assertEquals(expected.size, actual.size, "Size mismatch.")
        expected.zip(actual).forEachIndexed { index, (expectedErr, actualErr) ->
            assertTrue(
                compare(expectedErr, actualErr),
                "Actual $actualErr does not match expected $expectedErr at position $index."
            )
        }
    }

    @Test
    fun unexpectedSymbol() {
        val code = "var a = $ 5;"
        val errors = listOf(
            LexicalError(TokenWithOffset(UnrecognizedToken, -1, -1), ErrorType.UNEXPECTED_SYMBOL),
            ExpectedExpression(2)
        )
        runTest(code, errors)
    }

    @Test
    fun missingQuote() {
        val code = "var a = \"aboba"
        val errors = listOf(
            LexicalError(TokenWithOffset(StringLiteralToken("aboba"), -1, -1), ErrorType.EXPECTED_QUOTE),
            ExpectedToken(3, UnrecognizedToken, UnrecognizedToken)
        )
        runTest(code, errors)
    }

    @Test
    fun expectedExpression() {
        val code = "var a = ;"
        val errors = listOf(
            ExpectedExpression(2)
        )
        runTest(code, errors)
    }

    @Test
    fun conflictingDeclaration() {
        val code = """
            proc f (n: bool) {
            }
            func f (b: bool) {
                return 4;
            }
        """
        val errors = listOf(
            ConflictingDeclaration(Expr.SymbolName( identifier = IdentifierToken("f")))
        )
        runTest(code, errors)
    }

    @Test
    fun unresolvedSymbol() {
        val code = "var a = b;"
        val errors = listOf(
            UnresolvedSymbol(Expr.SymbolName( identifier = IdentifierToken("b")))
        )
        runTest(code, errors)
    }

    @Test
    fun typeMismatch() {
        val code = "var a = 45 % \"Hello\";"
        val expr = Expr.BinaryOp(StringOpToken, Expr.Constant(ConstantToken(45)), Expr.StringLiteral(StringLiteralToken("Hello")))
        val errors = listOf(
            OperandTypeMismatch(expr, StringOpToken, emptyList())
        )
        runTest(code, errors)
    }
}