package language.semantic

import language.lexer.BoolTypeToken
import language.lexer.NumberTypeToken
import language.lexer.StringTypeToken

enum class Type {
    NUMBER,
    STRING,
    BOOL
}

val tokenToType = mapOf(
    NumberTypeToken to Type.NUMBER,
    StringTypeToken to Type.STRING,
    BoolTypeToken to Type.BOOL
)

sealed class Symbol(val name: String)
class TypedSymbol(name: String, val type: Type) : Symbol(name)
abstract class CallableSymbol(name: String, val parameters: List<TypedSymbol>) : Symbol(name)
class ProcSymbol(name: String, parameters: List<TypedSymbol>) : CallableSymbol(name, parameters)
class FuncSymbol(name: String, parameters: List<TypedSymbol>, val returnType: Type) : CallableSymbol(name, parameters)