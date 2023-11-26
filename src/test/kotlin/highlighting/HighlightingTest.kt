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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dataStructures.SpaghettiStack
import language.lexer.Lexer
import language.parser.RecursiveDescentParser
import language.semantic.DefaultASTVisitor
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class HighlightingTest {
    private fun getHighlighters(text: String): List<Highlighter> {
        val lexer = Lexer(text)
        val tokensWithOffset = lexer.tokenize()
        val parser = RecursiveDescentParser(tokensWithOffset.map { tokenWithOffset -> tokenWithOffset.token })
        val (AST, syntaxErrors) = parser.parse()
        val visitor = DefaultASTVisitor()
        visitor.visit(AST, SpaghettiStack())
        val lexicalHighlighters = tokensWithOffset.map { tokenWithOffset -> createHighlighter(tokenWithOffset) }
        val syntaxErrorsHighlighters =
            syntaxErrors.map { invalidStatement -> createHighlighter(invalidStatement, tokensWithOffset) }
        val semanticErrorsHighlighters = visitor.getErrors().map {error -> createHighlighter(error, tokensWithOffset) }
        val lexicalAndSyntaxMerged = mergeHighlighters(lexicalHighlighters, syntaxErrorsHighlighters)
        // TODO: merge lexicalHighlighters & syntaxErrorsHighlighters by creating separate highlighter in intersections (taking color
        // TODO: from lexical and underline from syntax, error message from lexical but if empty - from syntax, sort by startOffset
        // TODO: merge to them semantic errors highlighting win underlining
        return mergeHighlighters(lexicalAndSyntaxMerged, semanticErrorsHighlighters)
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

//    @Test
//    @DisplayName("tmp.")
//    fun testTmp() {
//        val programText = """
//{
//  print("Aboba);
//}
//"""
//        runTestApp(programText)
//    }
}