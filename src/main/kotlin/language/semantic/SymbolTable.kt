package language.semantic

import language.Type

enum class Scope {
    GLOBAL_SCOPE,
    FUNC_SCOPE,
    PROC_SCOPE,
    BLOCK_SCOPE
}

class SymbolTable(val scope: Scope) {

    private val variables = mutableMapOf<String, TypedSymbol>()
    private val functions = mutableMapOf<String, MutableList<CallableSymbol>>()

    fun defineVariable(variable: TypedSymbol) {
        val key = variable.name
        if (variables.containsKey(key))
            throw IllegalArgumentException("Redefinition of symbol $key is not allowed.")
        variables[key] = variable
    }

    fun defineOverload(function: CallableSymbol) {
        if (!functions.containsKey(function.name)) {
            functions[function.name] = mutableListOf()
        } else {
            if (containsOverload(function.name, function.parameters.map { typedSymbol -> typedSymbol.type }))
                throw IllegalArgumentException("Redefinition of symbol ${function.name} is not allowed.")
        }
        functions[function.name]!!.add(function)
    }

    fun resolveVariable(name: String): TypedSymbol? = variables[name]
    fun resolveOverload(name: String, parameterTypes: List<Type>): CallableSymbol? {
        if (!containsFunction(name)) return null
        for (overload in functions[name]!!) {
            val typesList = overload.parameters.map { typedSymbol -> typedSymbol.type }
            if (typesList == parameterTypes)
                return overload
        }
        return null
    }

    fun containsVariable(name: String): Boolean = resolveVariable(name) != null
    fun containsOverload(name: String, parameterTypes: List<Type>): Boolean = resolveOverload(name,parameterTypes) != null
    fun containsFunction(name: String): Boolean = functions.containsKey(name)
}