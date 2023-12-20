package language

import language.lexer.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LexerTest {
    private fun runTest(input: String, expectedTokens: List<Token>) {
        val lexer = Lexer(input)
        val actualTokens = lexer.tokenize().first.map { tokenWithOffset -> tokenWithOffset.token }
        assertEquals(expectedTokens.size, actualTokens.size, "Size mismatch.")
        expectedTokens.zip(actualTokens).forEachIndexed { index, (expected, actual) ->
            assertTrue(
                compare(expected, actual),
                "Actual $actual does not match expected $expected at position $index."
            )
        }
    }

    @Test
    @DisplayName("Test tokenization of an identifier")
    fun testIdentifierToken() {
        runTest("abc123", listOf(IdentifierToken("abc123")))
    }

    @Test
    @DisplayName("Test tokenization of a constant")
    fun testConstantToken() {
        runTest("12345", listOf(ConstantToken(12345)))
    }

    @Test
    @DisplayName("Test tokenization of a string literal")
    fun testStringLiteralToken() {
        runTest("\"hello world\"", listOf(StringLiteralToken("hello world")))
    }

    @Test
    @DisplayName("Test tokenization of a boolean true")
    fun testBoolTrueToken() {
        runTest("true", listOf(BoolToken(true)))
    }

    @Test
    @DisplayName("Test tokenization of a boolean false")
    fun testBoolFalseToken() {
        runTest("false", listOf(BoolToken(false)))
    }

    @Test
    @DisplayName("Test tokenization of an arithmetic operator")
    fun testArithmeticOperatorToken() {
        runTest("+", listOf(PlusOpToken))
    }

    @Test
    @DisplayName("Test tokenization of multiple tokens")
    fun testMultipleTokens() {
        runTest(
            "var x = 10;",
            listOf(
                VarKeywordToken,
                IdentifierToken("x"),
                AssignToken,
                ConstantToken(10),
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of special symbols")
    fun testSpecialSymbolsToken() {
        runTest(
            "(){},;",
            listOf(
                LeftParenToken,
                RightParenToken,
                LeftBraceToken,
                RightBraceToken,
                CommaToken,
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of a complex expression")
    fun testComplexExpressionToken() {
        runTest(
            "var result = 3 + (2 * x);",
            listOf(
                VarKeywordToken,
                IdentifierToken("result"),
                AssignToken,
                ConstantToken(3),
                PlusOpToken,
                LeftParenToken,
                ConstantToken(2),
                MulOpToken,
                IdentifierToken("x"),
                RightParenToken,
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of boolean operators")
    fun testBooleanOperatorToken() {
        runTest(
            "a && b || c",
            listOf(
                IdentifierToken("a"),
                AndOpToken,
                IdentifierToken("b"),
                OrOpToken,
                IdentifierToken("c")
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of relational operators")
    fun testRelationalOperatorToken() {
        runTest(
            "x <= 10",
            listOf(
                IdentifierToken("x"),
                NumericRelationOpToken("<="),
                ConstantToken(10)
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of integer variable assignment")
    fun testIntegerVariableAssignment() {
        runTest(
            input = "var num = 42;",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("num"),
                AssignToken,
                ConstantToken(42),
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of string variable assignment")
    fun testStringVariableAssignment() {
        runTest(
            input = """var greeting = "Hello, IDELang!";""",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("greeting"),
                AssignToken,
                StringLiteralToken("Hello, IDELang!"),
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of boolean variable assignment")
    fun testBooleanVariableAssignment() {
        runTest(
            input = "var isTrue = true;",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("isTrue"),
                AssignToken,
                BoolToken(true),
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of integer addition")
    fun testIntegerAddition() {
        runTest(
            input = "var sum = x + 10;",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("sum"),
                AssignToken,
                IdentifierToken("x"),
                PlusOpToken,
                ConstantToken(10),
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of boolean expression")
    fun testBooleanExpression() {
        runTest(
            input = "var bothTrue = (x > 4) && (z == true);",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("bothTrue"),
                AssignToken,
                LeftParenToken,
                IdentifierToken("x"),
                NumericRelationOpToken(">"),
                ConstantToken(4),
                RightParenToken,
                AndOpToken,
                LeftParenToken,
                IdentifierToken("z"),
                EqOpToken,
                BoolToken(true),
                RightParenToken,
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of if statement")
    fun testIfStatement() {
        runTest(
            input = "if (x > 10) { y = \"Greater\"; }",
            expectedTokens = listOf(
                IfKeywordToken,
                LeftParenToken,
                IdentifierToken("x"),
                NumericRelationOpToken(">"),
                ConstantToken(10),
                RightParenToken,
                LeftBraceToken,
                IdentifierToken("y"),
                AssignToken,
                StringLiteralToken("Greater"),
                SemicolonToken,
                RightBraceToken
            )
        )
    }


    @Test
    @DisplayName("Test tokenization of if-else statement")
    fun testIfElseStatement() {
        runTest(
            input = "if (z) { y = \"True\"; } else { y = \"False\"; }",
            expectedTokens = listOf(
                IfKeywordToken,
                LeftParenToken,
                IdentifierToken("z"),
                RightParenToken,
                LeftBraceToken,
                IdentifierToken("y"),
                AssignToken,
                StringLiteralToken("True"),
                SemicolonToken,
                RightBraceToken,
                ElseKeywordToken,
                LeftBraceToken,
                IdentifierToken("y"),
                AssignToken,
                StringLiteralToken("False"),
                SemicolonToken,
                RightBraceToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of while loop")
    fun testWhileLoop() {
        runTest(
            input = "var i = 0; while (i < 5) { i = i + 1; }",
            expectedTokens = listOf(
                VarKeywordToken,
                IdentifierToken("i"),
                AssignToken,
                ConstantToken(0),
                SemicolonToken,
                WhileKeywordToken,
                LeftParenToken,
                IdentifierToken("i"),
                NumericRelationOpToken("<"),
                ConstantToken(5),
                RightParenToken,
                LeftBraceToken,
                IdentifierToken("i"),
                AssignToken,
                IdentifierToken("i"),
                PlusOpToken,
                ConstantToken(1),
                SemicolonToken,
                RightBraceToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of function declaration")
    fun testFunctionDeclaration() {
        runTest(
            input = "func add(a: number, b: number) { return a + b; }",
            expectedTokens = listOf(
                FuncKeywordToken,
                IdentifierToken("add"),
                LeftParenToken,
                IdentifierToken("a"),
                ColonToken,
                NumberTypeToken,
                CommaToken,
                IdentifierToken("b"),
                ColonToken,
                NumberTypeToken,
                RightParenToken,
                LeftBraceToken,
                ReturnKeywordToken,
                IdentifierToken("a"),
                PlusOpToken,
                IdentifierToken("b"),
                SemicolonToken,
                RightBraceToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of procedure declaration")
    fun testProcedureDeclaration() {
        runTest(
            input = "proc display(message: string) {  }",
            expectedTokens = listOf(
                ProcKeywordToken,
                IdentifierToken("display"),
                LeftParenToken,
                IdentifierToken("message"),
                ColonToken,
                StringTypeToken,
                RightParenToken,
                LeftBraceToken,
                RightBraceToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of print operation")
    fun testPrintOperation() {
        runTest(
            input = "print(\"Hello, World!\"); print(x); print(y % \" there!\");",
            expectedTokens = listOf(
                PrintKeywordToken,
                LeftParenToken,
                StringLiteralToken("Hello, World!"),
                RightParenToken,
                SemicolonToken,
                PrintKeywordToken,
                LeftParenToken,
                IdentifierToken("x"),
                RightParenToken,
                SemicolonToken,
                PrintKeywordToken,
                LeftParenToken,
                IdentifierToken("y"),
                StringOpToken,
                StringLiteralToken(" there!"),
                RightParenToken,
                SemicolonToken
            )
        )
    }

    @Test
    @DisplayName("Test tokenization of function declaration with error")
    fun testFunctionBadDeclaration() {
        runTest(
            input = "func add(a: number, b: number) & return a _ b; }",
            expectedTokens = listOf(
                FuncKeywordToken,
                IdentifierToken("add"),
                LeftParenToken,
                IdentifierToken("a"),
                ColonToken,
                NumberTypeToken,
                CommaToken,
                IdentifierToken("b"),
                ColonToken,
                NumberTypeToken,
                RightParenToken,
                UnrecognizedToken,
                ReturnKeywordToken,
                IdentifierToken("a"),
                UnrecognizedToken,
                IdentifierToken("b"),
                SemicolonToken,
                RightBraceToken
            )
        )
    }
}
