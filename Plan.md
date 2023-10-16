#Plan:

---
##Step 1:
- Type text
- Render text
- Store text in simples form (char[])

##Step 2:
- Caret movement, left/right/backspace/delete/insert
- Implement interface for data structure and basic text operations

##Step 3:
- Detect line feed special character and render multiple lines of text
- Caret movement up/down

##Step 4:
- [Maybe] Update our data structure

##Step 5:
- Open and read file
- Save file on disk

##Step 6:
- Viewport and scrolling
- Scrollbar/ file minimap

##Step 7:
- Document markup model (simplest impl: sorted list of highlighters)
- Update rendering

##Step 8:
- Text selection
- Copy/Cut/Paste support

##Step 9:
- Language services
- Lexical analyser and lexical syntax highlighter

##Step 10:
- Project model and [maybe] multiple text editor tabs
- Simple tree-like project view using default controls from library

##Step 11:
- Introduce background processing and move lexical analysis and project mode processing to background
- Some way to manage concurrency

##Step 12:
- Syntax analyser
- Show syntax errors

##Step 13:
- VFS and Caches

##Step 14:
- Build symbol tables and perform semantic analysis (related to searching, auto-completion, go-to-declaration, etc.)
- Type inference and type checking
- Show semantic errors

##Step 15:
- Simple auto-completion
- Simple "Go-to-definition"

##Step 16:
- Code modification and transactions
- Some refactoring

##Step 17:
- Refine, optimize, rewrite basic data structures if needed