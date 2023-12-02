package language.semantic

class SymbolTable {

    private val symbols = mutableMapOf<String, Symbol>()

    // overloads handling
    companion object {
        fun generateCallableSignature(name: String, parameterTypes: List<Type>): String {
            return name + "(" + parameterTypes.joinToString(",") { it.toString() } + ")"
        }
    }

    fun define(symbol: Symbol) {
        var key = symbol.name

        if (symbol is CallableSymbol) {
            key = generateCallableSignature(symbol.name, symbol.parameters.map { it.type })
        }
        if (symbols.containsKey(key))
            throw IllegalArgumentException("Redefinition of symbol $key is not allowed.")

        symbols[key] = symbol
    }

    fun resolve(name: String, parameterTypes: List<Type>? = null): Symbol? {
        return if (parameterTypes != null) {
            symbols[generateCallableSignature(name, parameterTypes)]
        } else {
            symbols[name]
        }
    }

    fun contains(name: String, parameterTypes: List<Type>? = null): Boolean = resolve(name, parameterTypes) != null
}