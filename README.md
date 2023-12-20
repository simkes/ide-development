# Editor for IDELang

*team: Kseniia Simanova and Mikhail Rodionychev*

## How to build and run
 TODO

## Technical decisions
Data structure used for text - gap buffer.

Editor supports lexical and semantic highlighting (errors). Highlightings are not incremental and
we are using just sorted list for their display.
Lexical analysis is implemented by finite automaton, syntax with recursive descent parser that builds Abstract Syntax Tree
and semantic analysis with type inference is implemented with visitor pattern.
[Grammar rules for IDELang](./src/main/kotlin/language/Grammar.txt)

## IDELang specification
[IDELang-specification.md](./IDELang-specification.md)

## List of supported errors that may occur in code on IDELang
[Supported-errors.md](./Supported-errors.md)