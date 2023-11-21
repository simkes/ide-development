package language

import language.lexer.*
import language.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SyntaxAnalysisTest {

    private fun runTest(input: String, expected: Stmt.Block, parsedWithError: Boolean = false) {
        // assuming that lexer works fine
        val lexer = Lexer(input)
        val tokens = lexer.tokenize()
        val parser = RecursiveDescentParser(tokens)
        val programResult = parser.parse()
        assertTrue(
            compare(programResult, expected),
            "Actual does not match expected. Expected: $expected, actual: $programResult."
        )
        assertEquals(parsedWithError, parser.parsedWithError)
    }

    @Test
    @DisplayName("Test parsing of var declaration with expression.")
    fun testComplexExpressionToken() {
        val varDeclaration = Stmt.VarDeclaration(
            identifier = IdentifierToken("result"),
            expr = Expr.BinaryOp(
                operation = PlusOpToken,
                leftExpr = Expr.Constant(ConstantToken(3)),
                rightExpr = Expr.BinaryOp(
                    operation = MulOpToken,
                    leftExpr = Expr.Constant(ConstantToken(2)),
                    rightExpr = Expr.Variable(IdentifierToken("x"))
                )
            )
        )
        runTest(
            "var result = 3 + (2 * x);",
            Stmt.Block(listOf(varDeclaration))
        )
    }

    @Test
    @DisplayName("Test parsing of function declaration.")
    fun testFunctionDeclaration() {
        val funcDeclaration = Stmt.FuncDeclaration(
            identifier = IdentifierToken("add"),
            parameters = listOf(
                Parameter(IdentifierToken("a"), NumberTypeToken),
                Parameter(IdentifierToken("b"), NumberTypeToken)
            ),
            block = Stmt.Block(
                listOf(
                    Stmt.ReturnStatement(
                        Expr.BinaryOp(
                            operation = PlusOpToken,
                            leftExpr = Expr.Variable(IdentifierToken("a")),
                            rightExpr = Expr.Variable(IdentifierToken("b"))
                        )
                    )
                )
            )
        )
        runTest(
            "func add(a: number, b: number) { return a + b; }",
            Stmt.Block(listOf(funcDeclaration))
        )
    }

    @Test
    @DisplayName("Test parsing of while loop with variable declaration.")
    fun testWhileLoop() {
        val varDeclaration = Stmt.VarDeclaration(
            identifier = IdentifierToken("i"),
            expr = Expr.Constant(ConstantToken(0))
        )
        val whileStatement = Stmt.WhileStatement(
            condition = Expr.BinaryOp(
                operation = NumericRelationOpToken("<"),
                leftExpr = Expr.Variable(IdentifierToken("i")),
                rightExpr = Expr.Constant(ConstantToken(5))
            ),
            block = Stmt.Block(
                listOf(
                    Stmt.Assignment(
                        identifier = IdentifierToken("i"),
                        expr = Expr.BinaryOp(
                            operation = PlusOpToken,
                            leftExpr = Expr.Variable(IdentifierToken("i")),
                            rightExpr = Expr.Constant(ConstantToken(1))
                        )
                    )
                )
            )
        )
        runTest(
            "var i = 0; while (i < 5) { i = i + 1; }",
            Stmt.Block(listOf(varDeclaration, whileStatement))
        )
    }

    @Test
    @DisplayName("Test parsing of if-else statement with variable declaration.")
    fun testIfElseStatement() {
        val varXDeclaration = Stmt.VarDeclaration(
            identifier = IdentifierToken("x"),
            expr = Expr.BoolValue(BoolToken(true))
        )
        val varYDeclaration = Stmt.VarDeclaration(
            identifier = IdentifierToken("y"),
            expr = Expr.StringLiteral(StringLiteralToken(""))
        )
        val ifElseStatement = Stmt.IfStatement(
            condition = Expr.Variable(IdentifierToken("x")),
            thenBlock = Stmt.Block(
                listOf(
                    Stmt.Assignment(
                        identifier = IdentifierToken("y"),
                        expr = Expr.StringLiteral(StringLiteralToken("True"))
                    )
                )
            ),
            elseBlock = Stmt.Block(
                listOf(
                    Stmt.Assignment(
                        identifier = IdentifierToken("y"),
                        expr = Expr.StringLiteral(StringLiteralToken("False"))
                    )
                )
            )
        )
        runTest(
            "var x = true; var y = \"\"; if (x) { y = \"True\"; } else { y = \"False\"; }",
            Stmt.Block(listOf(varXDeclaration, varYDeclaration, ifElseStatement))
        )
    }

    @Test
    @DisplayName("Test parsing of print statement.")
    fun testPrintStatement() {
        val printStatement = Stmt.PrintStatement(
            expr = Expr.StringLiteral(StringLiteralToken("Hello, World!"))
        )
        runTest(
            "print(\"Hello, World!\");",
            Stmt.Block(listOf(printStatement))
        )
    }

    @Test
    @DisplayName("Test parsing of complex arithmetic expression.")
    fun testComplexArithmeticExpression() {
        val complexExpression = Stmt.VarDeclaration(
            identifier = IdentifierToken("result"),
            expr = Expr.BinaryOp(
                operation = MinusOpToken,
                leftExpr = Expr.BinaryOp(
                    operation = PlusOpToken,
                    leftExpr = Expr.BinaryOp(
                        operation = MulOpToken,
                        leftExpr = Expr.Constant(ConstantToken(2)),
                        rightExpr = Expr.Variable(IdentifierToken("x"))
                    ),
                    rightExpr = Expr.Constant(ConstantToken(10))
                ),
                rightExpr = Expr.BinaryOp(
                    operation = DivOpToken,
                    leftExpr = Expr.Constant(ConstantToken(4)),
                    rightExpr = Expr.Variable(IdentifierToken("y"))
                )
            )
        )
        runTest(
            "var result = 2 * x + 10 - 4 / y;",
            Stmt.Block(listOf(complexExpression))
        )
    }

    @Test
    @DisplayName("Test parsing of boolean expressions.")
    fun testBooleanExpressions() {
        val boolExpression = Stmt.VarDeclaration(
            identifier = IdentifierToken("boolResult"),
            expr = Expr.BinaryOp(
                operation = OrOpToken,
                leftExpr = Expr.BoolValue(BoolToken(true)),
                rightExpr = Expr.BinaryOp(
                    operation = AndOpToken,
                    leftExpr = Expr.Variable(IdentifierToken("x")),
                    rightExpr = Expr.UnaryOp(
                        operation = NotOpToken,
                        expr = Expr.BoolValue(BoolToken(false))
                    )
                )
            )
        )
        runTest(
            "var boolResult = true || x && !false;",
            Stmt.Block(listOf(boolExpression))
        )
    }

    @Test
    @DisplayName("Test parsing of relational expressions.")
    fun testRelationalExpressions() {
        val relationalExpression = Stmt.VarDeclaration(
            identifier = IdentifierToken("relResult"),
            expr = Expr.BinaryOp(
                operation = EqOpToken,
                leftExpr = Expr.BinaryOp(
                    operation = NumericRelationOpToken("<"),
                    leftExpr = Expr.Constant(ConstantToken(5)),
                    rightExpr = Expr.Variable(IdentifierToken("y"))
                ),
                rightExpr = Expr.BinaryOp(
                    operation = NumericRelationOpToken(">"),
                    leftExpr = Expr.Variable(IdentifierToken("z")),
                    rightExpr = Expr.Constant(ConstantToken(3))
                )
            )
        )
        runTest(
            "var relResult = (5 < y) == (z > 3);",
            Stmt.Block(listOf(relationalExpression))
        )
    }

    @Test
    @DisplayName("Test parsing of complex relational and boolean expressions.")
    fun testComplexRelationalAndBooleanExpressions() {
        val complexExpression = Stmt.VarDeclaration(
            identifier = IdentifierToken("complexResult"),
            expr = Expr.BinaryOp(
                operation = OrOpToken,
                leftExpr = Expr.BinaryOp(
                    operation = AndOpToken,
                    leftExpr = Expr.BinaryOp(
                        operation = EqOpToken,
                        leftExpr = Expr.Constant(ConstantToken(10)),
                        rightExpr = Expr.BinaryOp(
                            operation = MulOpToken,
                            leftExpr = Expr.Constant(ConstantToken(2)),
                            rightExpr = Expr.Variable(IdentifierToken("x"))
                        )
                    ),
                    rightExpr = Expr.BinaryOp(
                        operation = NotEqOpToken,
                        leftExpr = Expr.Variable(IdentifierToken("y")),
                        rightExpr = Expr.Constant(ConstantToken(5))
                    )
                ),
                rightExpr = Expr.BinaryOp(
                    operation = OrOpToken,
                    leftExpr = Expr.BinaryOp(
                        operation = NumericRelationOpToken(">"),
                        leftExpr = Expr.Variable(IdentifierToken("z")),
                        rightExpr = Expr.Constant(ConstantToken(3))
                    ),
                    rightExpr = Expr.UnaryOp(
                        operation = NotOpToken,
                        expr = Expr.BoolValue(BoolToken(true))
                    )
                )
            )
        )
        runTest(
            "var complexResult = (10 == 2 * x && y != 5) || (z > 3 || !true);",
            Stmt.Block(listOf(complexExpression))
        )
    }

    @Test
    @DisplayName("Test parsing of print statement with error.")
    fun testPrintStatementError() {
        val printStatement = Stmt.InvalidStatement("Expected token: ), but found: ;.")
        runTest(
            "print(\"Hello, World!\";",
            Stmt.Block(listOf(printStatement)),
            true
        )
    }

    @Test
    @DisplayName("Test parsing of var declaration with error.")
    fun testDeclarationError() {
        runTest(
            "var = 3 + x;",
            Stmt.Block(listOf(
                Stmt.InvalidStatement("Expected token of type: IdentifierToken, but found: AssignToken."),
                Stmt.InvalidStatement("Unexpected token number at position 2."),
                Stmt.InvalidStatement("Unexpected token operation at position 3."),
                Stmt.InvalidStatement("Expected token: (, but found: ;.")
            )),
            true
        )
    }

}