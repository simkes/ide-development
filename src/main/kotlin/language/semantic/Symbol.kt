package language.semantic

enum class Type {
    NUMBER,
    STRING,
    BOOL
}

sealed class Symbol(val name: String)
class TypedSymbol(name: String, val type: Type) : Symbol(name)
abstract class CallableSymbol(name: String, val parameters: List<TypedSymbol>) : Symbol(name)
class ProcSymbol(name: String, parameters: List<TypedSymbol>) : CallableSymbol(name, parameters)
class FuncSymbol(name: String, parameters: List<TypedSymbol>, val returnType: Type) : CallableSymbol(name, parameters)