# IDELang Specification

## Data Types
- **number, string, and bool.**
    - `number`: Support for integer arithmetic.  
      `var num = 42;`
    - `string`: Represent textual data.  
      `var greeting = "Hello, IDELang!";`
    - `bool`: Defined as `true` and `false`.  
      `var isTrue = true;`

## Variables
Variables are declared using the `var` keyword, with type being inferred from the assigned value.  
`var x = 5;`

## Arithmetic Operations
Supported operations: +, -, *, /  
`var sum = x + 10;`

## String Operations
- `%`: String concatenation  
  `var fullName = "Hello " % "World!";`

## Boolean Operations
Supported operations: &&, ||, !, ==, !=  
`var bothTrue = (x > 4) && (z == true);`

## Relational Operations
<, >, ==, !=, <=, >=  
Used with numbers, but `==` and `!=` can also be used for strings and bool.

## Conditional Structures
- `if(condition) {}`
- `if(condition) {} else {}`

Examples:  
`if (x > 10) { y = "Greater"; }`  
`if (z) { y = "True"; } else { y = "False"; }`

## Loops
While loops: `while(condition) {}`  
```c
var i = 0;
while (i < 5) { i = i + 1; }
```

## Functions
Functions are defined using the `func` keyword, and their return type is inferred based on the type of the returned value.  
```c
func add(a: number, b: number)
{
return a + b; // Inferred return type: number
}
```
There can be different functions with the same names but different sets of parameters.

## Procedures (Procs)
Procedures are introduced using the `proc` keyword and do not return a value.  
`proc display (message: string) { }`

## Print Operation
The `print` operation is used to display output.  
`print("Hello, World!");  // Displays: Hello, World!`  
`print(x);                // Displays the value of x`
