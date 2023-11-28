@file:OptIn(ExperimentalTextApi::class)

package highlighting

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import language.AnalysisError
import language.CodeAnalyzer
import language.Level
import language.lexer.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class HighlightingTest {

    private fun createHighlighter(tokenWithOffset: TokenWithOffset): Highlighter {
        val startOffset = tokenWithOffset.startOffset
        val endOffset = tokenWithOffset.endOffset
        return when (tokenWithOffset.token) {
            is IdentifierToken -> Highlighter(startOffset, endOffset, highlighting.Color.PURPLE)
            is ConstantToken -> Highlighter(startOffset, endOffset, highlighting.Color.BLUE)
            is StringLiteralToken -> Highlighter(startOffset, endOffset, highlighting.Color.GREEN)
            is BoolToken, is KeywordToken -> Highlighter(startOffset, endOffset, highlighting.Color.ORANGE)
            is UnrecognizedToken -> Highlighter(
                startOffset,
                endOffset,
                highlighting.Color.RED,
                false,
                (tokenWithOffset.token as UnrecognizedToken).errorMessage
            )

            else -> Highlighter(startOffset, endOffset)
        }
    }

    private fun createHighlighter(analysisError: AnalysisError, tokens: List<TokenWithOffset>): Highlighter {
        val startOffset = tokens[analysisError.node.start].startOffset
        val endOffset = tokens[analysisError.node.end].endOffset
        return Highlighter(startOffset, endOffset, highlighting.Color.BLACK, true, analysisError.errorMessage)
    }
    private fun getHighlighters(text: String): List<Highlighter> {
        val (tokens, errors) = CodeAnalyzer.analyze(text, Level.SEMANTIC)
        val lexicalHighlighters = tokens.map { tk -> createHighlighter(tk) }
        val errorHighlighters =
            errors.map { error -> createHighlighter(error, tokens) }.sortedBy { highlighter -> highlighter.startOffset }
        return mergeHighlighters(lexicalHighlighters, errorHighlighters)
    }

    private fun mergeHighlighters(primary: List<Highlighter>, secondary: List<Highlighter>): List<Highlighter> {
        val merged = mutableListOf<Highlighter>()
        var secondaryIndex = 0

        for (prim in primary) {
            while (secondaryIndex < secondary.size && secondary[secondaryIndex].endOffset < prim.startOffset) {
                secondaryIndex++
            }

            if (secondaryIndex < secondary.size && secondary[secondaryIndex].startOffset <= prim.endOffset) {
                val sec = secondary[secondaryIndex]

                // Merge properties: color, underline, and error message from the secondary if present, otherwise from the primary
                val color = if (sec.color != highlighting.Color.BLACK) sec.color else prim.color
                val underlined = sec.underlined || prim.underlined
                val errorMessage = if (sec.errorMessage.isNotEmpty()) sec.errorMessage else prim.errorMessage

                merged.add(
                    Highlighter(
                        startOffset = maxOf(prim.startOffset, sec.startOffset),
                        endOffset = minOf(prim.endOffset, sec.endOffset),
                        color = color,
                        underlined = underlined,
                        errorMessage = errorMessage
                    )
                )
            } else {
                // No overlapping secondary highlighter, add primary as is
                merged.add(prim)
            }
        }

        // Add remaining secondary highlighters that don't overlap with any primary highlighters
        merged.addAll(secondary.filterNot { sec -> merged.any { prim -> prim.startOffset <= sec.endOffset && prim.endOffset >= sec.startOffset } })

        return merged.sortedBy { it.startOffset }
    }

    private fun createAnnotatedString(text: String): AnnotatedString {
        val colorMap = mapOf(
            highlighting.Color.RED to Color(0xFFBF2308),
            highlighting.Color.ORANGE to Color(0xFFae7313),
            highlighting.Color.PURPLE to Color(0xFF9d6c7c),
            highlighting.Color.GREEN to Color(0xFF7d9726),
            highlighting.Color.BLUE to Color(0xFF5f9182),
            highlighting.Color.BLACK to Color(0xFF878573),
        )
        val annotatedString = AnnotatedString.Builder()
        val highlighters = getHighlighters(text)
        var lastIndex = 0
        highlighters.forEach { interval ->
            if (interval.startOffset > lastIndex) {
                annotatedString.append(text.substring(lastIndex, interval.startOffset))
            }

            val textDecoration = if (interval.underlined) {
                TextDecoration.Underline
            } else {
                TextDecoration.None
            }

            annotatedString.withStyle(style = SpanStyle(color = colorMap[interval.color]!!, textDecoration = textDecoration)) {
                append(text.substring(interval.startOffset, interval.endOffset))
            }

            lastIndex = interval.endOffset
        }

        if (lastIndex < text.length) {
            annotatedString.append(text.substring(lastIndex))
        }

        return annotatedString.toAnnotatedString()
    }

    @OptIn(ExperimentalTextApi::class)
    @Composable
    @Preview
    private fun drawHighlightedText(text: String) {
        val textMeasurer = rememberTextMeasurer()
        MaterialTheme {
            Box() {
                Canvas(modifier = Modifier.fillMaxSize().background(color = Color(0xFF22221b))) {
                    text.let {
                        val measuredText = textMeasurer.measure(
                            createAnnotatedString(it),
                            style = TextStyle(fontSize = 15.sp)
                        )

                        translate(10f, 10f) {
                            drawText(measuredText)
                        }
                    }
                }
            }
        }
    }


    private fun runTestApp(text: String) {
        application {
            Window(onCloseRequest = ::exitApplication) {
                drawHighlightedText(text)
            }
        }
    }

    @Test
    @DisplayName("Simple program in IDELang.")
    fun testSimpleProgram() {
        val programText = """
func factorial(n: number) {
    var result = 1;
    while (n > 1) {
        result = result * n;
        n = n - 1;
    }
    return result;
}

proc main() {
  var input = 5;
  var result = factorial(input);
	
  print(result);
  print("Aboba");
}
"""
        runTestApp(programText)
    }

    @Test
    @DisplayName("Program in IDELang with error.")
    fun testProgramWithError() {
        val programText = """
func factorial(n: number) {
    var result = 1;
    while (n > 1) {
        result = result & n;
        n = n - 1;
    }
    return result;
}

proc main() {
  var input = 5;
  var result = factorial(input);
	
  print(result);
  print("Aboba);
}
"""
        runTestApp(programText)
    }

    @Test
    @DisplayName("Big program in IDELang.")
    fun testBigProgram() {
        val programText = """
func calculateScore(choices: number) {
    return choices * 100;
}

func makeChoice(message: string) {
    print(message);
    var choice = 0;
    while (choice < 1 || choice > 2) {
        print("Enter 1 or 2: ");
    }
    return choice;
}

proc main() {
    var score = 0;
    var playerChoice = 0;
    
    print("Welcome to the Adventure Game!");
    

    playerChoice = makeChoice("You see a fork in the road. Do you go left (1) or right (2)?");
    if (playerChoice == 1) {
        score = score + 1;
        print("You encounter a friendly traveler.");
    } else {
        score = score + 2;
        print("You find a treasure map!");
    }

    playerChoice = makeChoice("Do you camp for the night (1) or continue (2)?");
    if (playerChoice == 1) {
        score = score + 1;
        print("You have a peaceful night's rest.");
    } else {
        score = score + 2;
        print("You encounter nocturnal creatures.");
    }

    var finalScore = calculateScore(score);
    var resultMessage = "Your adventure ends with a score of: ";
    print(resultMessage);
    print(finalScore);
}
"""
        runTestApp(programText)
    }

    @Test
    @DisplayName("Unresolved function overload.")
    fun testUnresolvedFunction() {
        val programText = """
func addNumbers(a: number, b: number) {
    var sum = a + b;
    return sum;
}

var result = addNumbers(5, "10");

"""
        runTestApp(programText)
    }

    @Test
    @DisplayName("Type mismatch.")
    fun testTypeMismatch() {
        val programText = """
func concatStrings(str1: string, str2: number) {
    return str1 % str2;
}

var message = concatStrings("Hello", "World");

"""
        runTestApp(programText)
    }

    @Test
    @DisplayName("Function type mismatch.")
    fun testFunctionTypeMismatch() {
        val programText = """
    func circleArea(radius: string) {
        var area = 3 * radius * radius;
        return area;
    }

    var result = circleArea(10);
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Unresolved symbol.")
    fun testUnresolvedSymbol() {
        val programText = """
    proc increment(num: number) {
        num = num + 1;
        print(num);
    }

    increment(x);
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Missing semicolon in return statement.")
    fun testMissingSemicolonInReturn() {
        val programText = """
    func power(base: number, exponent: number) {
        var result = 1;
        while (exponent > 0) {
            result = result * base;
            exponent = exponent - 1;
        }
        return result
    }

    var powResult = power(2, 3);
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Conflicting variable declaration.")
    fun testConflictingVariableDeclaration() {
        val programText = """
    var x = 10;
    var x = "Hello";
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Unresolved variable in assignment.")
    fun testUnresolvedVariableInAssignment() {
        val programText = """
    y = 5;
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Type mismatch in assignment.")
    fun testTypeMismatchInAssignment() {
        val programText = """
    var x = 10;
    x = "Hello";
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Incorrect condition type in if statement.")
    fun testIncorrectConditionTypeInIfStatement() {
        val programText = """
    if ("not a boolean") {
    
    }
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Missing return statement in function.")
    fun testMissingReturnStatementInFunction() {
        val programText = """
    func myFunction() {
        var i = 4;
    }
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Unexpected return statement in procedure.")
    fun testUnexpectedReturnInProcedure() {
        val programText = """
    proc myProcedure() {
        return 5;
    }
    
    var x = myProcedure();
    """
        runTestApp(programText)
    }


    @Test
    @DisplayName("Conflicting function declaration.")
    fun testConflictingFunctionDeclaration() {
        val programText = """
    func add(a: number, b: number) {
        return a + b;
    }
    func add(a: number, b: number) {
        return a - b;
    }
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Invalid binary operation.")
    fun testInvalidBinaryOperation() {
        val programText = """
    var x = 10 + "test";
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Invalid unary operation.")
    fun testInvalidUnaryOperation() {
        val programText = """
    var x = 10;
    var y = !x;
    """
        runTestApp(programText)
    }

    @Test
    @DisplayName("Unresolved function call.")
    fun testUnresolvedFunctionCall() {
        val programText = """
    var result = myFunc(10, 20);
    """
        runTestApp(programText)
    }

}