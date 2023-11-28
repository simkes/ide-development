package language

import dataStructures.SpaghettiStack
import language.lexer.Lexer
import language.lexer.TokenWithOffset
import language.parser.RecursiveDescentParser
import language.semantic.DefaultASTVisitor

// combines stages of code analysis together
object CodeAnalyzer {
    fun analyze(code: String, level: Level): Pair<List<TokenWithOffset>, List<AnalysisError>> {
        if(level == Level.NONE)
            return Pair(emptyList(), emptyList())

        val lexer = Lexer(code)
        val tokenWithOffset = lexer.tokenize()
        if(level == Level.LEXICAL)
            return Pair(tokenWithOffset, emptyList())

        val parser = RecursiveDescentParser(tokenWithOffset.map { tk -> tk.token })
        val (AST, analysisErrors) = parser.parse()
        if(level == Level.SYNTAX)
            return Pair(tokenWithOffset, analysisErrors)

        val visitor = DefaultASTVisitor()
        visitor.visit(AST, SpaghettiStack())
        analysisErrors.addAll(visitor.getErrors())
        return Pair(tokenWithOffset, analysisErrors)
    }
}