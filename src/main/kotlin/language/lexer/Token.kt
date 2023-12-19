package language.lexer

sealed class Token

data class IdentifierToken(val name: String) : Token()
data class ConstantToken(val value: Int) : Token()
data class StringLiteralToken(val value: String) : Token()

const val QUOTE = '\"'

data class BoolToken(val value: Boolean) : Token()

const val TRUE = "true"
const val FALSE = "false"

sealed class OpToken(open val operatorSymbol: String, open val unary: Boolean, open val binary: Boolean) : Token()
sealed class ArithmeticOpToken(
    override val operatorSymbol: String,
    override val unary: Boolean,
    override val binary: Boolean
) : OpToken(operatorSymbol, unary, binary)

// technically can be unary, but not allowed in this implementation to simplify
// code readability and parsing
object PlusOpToken : ArithmeticOpToken("+", false, true)
object MinusOpToken : ArithmeticOpToken("-", true, true)
object MulOpToken : ArithmeticOpToken("*", false, true)
object DivOpToken : ArithmeticOpToken("/", false, true)

// only one operator for strings
object StringOpToken : OpToken("%", false, true)
sealed class BoolOpToken(
    override val operatorSymbol: String,
    override val unary: Boolean,
    override val binary: Boolean
) : OpToken(operatorSymbol, unary, binary)

object NotOpToken : BoolOpToken("!", true, false)
object AndOpToken : BoolOpToken("&&", false, true)
object OrOpToken : BoolOpToken("||", false, true)
sealed class RelationalOpToken(override val operatorSymbol: String) : OpToken(operatorSymbol, false, true)
object EqOpToken : RelationalOpToken("==")
object NotEqOpToken : RelationalOpToken("!=")
data class NumericRelationOpToken(override val operatorSymbol: String) : RelationalOpToken(operatorSymbol)

object LeftParenToken : Token()
object RightParenToken : Token()
object LeftBraceToken : Token()
object RightBraceToken : Token()
object SemicolonToken : Token()
object CommaToken : Token()
object ColonToken : Token()
object AssignToken : Token()

val specialSymbolToToken = mapOf(
    "+" to PlusOpToken,
    "-" to MinusOpToken,
    "*" to MulOpToken,
    "/" to DivOpToken,
    "%" to StringOpToken,
    "!" to NotOpToken,
    "&&" to AndOpToken,
    "||" to OrOpToken,
    ">" to NumericRelationOpToken(">"),
    "<" to NumericRelationOpToken("<"),
    ">=" to NumericRelationOpToken(">="),
    "<=" to NumericRelationOpToken("<="),
    "==" to EqOpToken,
    "!=" to NotEqOpToken,
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

sealed class KeywordToken() : Token()

object VarKeywordToken : KeywordToken()
object IfKeywordToken : KeywordToken()
object ElseKeywordToken : KeywordToken()
object WhileKeywordToken : KeywordToken()
object FuncKeywordToken : KeywordToken()
object ProcKeywordToken : KeywordToken()
object ReturnKeywordToken : KeywordToken()
object PrintKeywordToken : KeywordToken()

open class TypeToken : Token()
object NumberTypeToken : TypeToken()
object StringTypeToken : TypeToken()
object BoolTypeToken : TypeToken()

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

object UnrecognizedToken : Token()