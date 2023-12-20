# IDELang Supported Errors

It is list of code errors that will be detected by our editor.

1. Unexpected symbol.

    `var a = $ 5;`


2. Missing quote at the string literal end.


3. Unexpected token encountered.


4. Expected specific token, but was not found. 

    `func a ( n: ) // expecting type of parameter `


5. Expected expression.
 
    `var a = ;`


6. Conflicting declaration of variable, procedure (proc) or function (func). For functions and procedures overloads are possible.


7. Unresolved symbol - variable, function, or procedure call cannot be resolved by name. (Declaration was not found.)


8. Type mismatches occur in:
   - Variable assignments
   - Conditions for `if` or `while` statements
   - Expressions with various operators and types


9. Missing return statement inside function body.


10. Unexpected return statement encountered not inside function body scope.


11. Unresolved overload - none of the function/ procedure overloads is suitable for provided arguments types.


12. Ignored return value - calling function without using it return value.

    `func();`


13. No return value - when calling procedure inside expression.

    `a = 1 + proc();`


14. Invalid operator - using binary operator instead of unary.

    `a = %"someString"`


15. Outside global scope function or procedure declaration (for example, inside another function or block).
