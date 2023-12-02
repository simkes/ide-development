package language

sealed class Token

data class IdentifierToken(val name: String) : Token()
data class ConstantToken(val value: Int) : Token()
data class StringLiteralToken(val value: String) : Token()

object BoolTrueToken : Token()
object BoolFalseToken : Token()

const val QUOTE = '\"'
const val TRUE = "true"
const val FALSE = "false"

sealed class OperatorToken(open val symbol: String) : Token()

data class ArithmeticOperatorToken(override val symbol: String) : OperatorToken(symbol)
data class StringOperatorToken(override val symbol: String) : OperatorToken(symbol)
data class BooleanOperatorToken(override val symbol: String) : OperatorToken(symbol)
data class RelationalOperatorToken(override val symbol: String) : OperatorToken(symbol)

object LeftParenToken : Token()
object RightParenToken : Token()
object LeftBraceToken : Token()
object RightBraceToken : Token()
object SemicolonToken : Token()
object CommaToken : Token()
object ColonToken : Token()
object AssignToken : Token()

val specialSymbolToToken = mapOf(
    "+" to ArithmeticOperatorToken("+"),
    "-" to ArithmeticOperatorToken("-"),
    "*" to ArithmeticOperatorToken("*"),
    "/" to ArithmeticOperatorToken("/"),
    "%" to StringOperatorToken("%"),
    "!" to BooleanOperatorToken("!"),
    "&&" to BooleanOperatorToken("&&"),
    "||" to BooleanOperatorToken("||"),
    ">" to RelationalOperatorToken(">"),
    "<" to RelationalOperatorToken("<"),
    "==" to RelationalOperatorToken("=="),
    "!=" to RelationalOperatorToken("!="),
    "=>" to RelationalOperatorToken("=>"),
    "<=" to RelationalOperatorToken("<="),
    "(" to LeftParenToken,
    ")" to RightParenToken,
    "{" to LeftBraceToken,
    "}" to RightBraceToken,
    ";" to SemicolonToken,
    "," to CommaToken,
    ":" to ColonToken,
    "=" to AssignToken
)

fun isSpecialSymbol(char: Char): Boolean {
    return specialSymbolToToken.containsKey(char.toString()) || char == '&' || char == '|'
}

object VarKeywordToken : Token()
object IfKeywordToken : Token()
object ElseKeywordToken : Token()
object WhileKeywordToken : Token()
object FuncKeywordToken : Token()
object ProcKeywordToken : Token()
object ReturnKeywordToken : Token()
object PrintKeywordToken : Token()

object NumberTypeToken : Token()
object StringTypeToken : Token()
object BoolTypeToken : Token()

val keywordToToken = mapOf(
    "var" to VarKeywordToken,
    "func" to FuncKeywordToken,
    "proc" to ProcKeywordToken,
    "if" to IfKeywordToken,
    "else" to ElseKeywordToken,
    "while" to WhileKeywordToken,
    "print" to PrintKeywordToken,
    "return" to ReturnKeywordToken,
    "number" to NumberTypeToken,
    "string" to StringTypeToken,
    "bool" to BoolTypeToken
)

