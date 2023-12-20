# Editor for IDELang

*team: Kseniia Simanova and Mikhail Rodionychev*

## How to build and run

TODO

## Technical decisions

Data structure used for text - gap buffer.

Editor supports lexical and semantic highlighting (errors). Highlightings are not incremental, and
we are using just sorted list for their display.

Lexical analysis is implemented by finite automaton, syntax with recursive descent parser that builds Abstract Syntax
Tree
and semantic analysis with type inference is implemented with visitor pattern.
[Grammar rules for IDELang](./src/main/kotlin/language/Grammar.txt)

### Technical description

The editor utilizes files as `Documents` -- in-memory objects that contain the in-editor representation of a text file.
This includes text stored as a text buffer (at the moment, we support trivial `SimpleArrayTextBuffer` and a bit more
effective `GapBuffer` implementations) and a caret state. It also provides an interface to interact with the text.

On the backend, there is a `VirtualFileSystem` -- abstraction aiming to ease working with various filesystems.
Unfortunately, it is not integrated in the project as much as we would like, but still it provides an interface to work
with various types of files defined by `VirtualFile` interface: it creates, moves, copies, deletes, loads, and updates
them. It also provides a `WatchService` to watch changes coming from the OS to notify user about them.

All events and some of the calculations happen in the background -- events are dispatched to the background thread
using `UiEventProcessor`. Concurrency is implemented in a very schematic way: there is no correct usage of scopes yet
and, unfortunately, exceptions coming from the background can blow something up.

The editor itself provides some of the common functionalities: file opening, file creation, editing, highlighting,
several windows (controlled by `OPENED_DOCUMENTS_LIMIT` parameter in `Constants`). It also features lazy file explorer
that updates when new files are added.

### What can be easily added

Unfortunately, we did not add all the functionality we would like in time, but there is a skeleton that supports (
questionably) easy extension, at least for some features:

* File management (create, delete, move, rename, ...): all operations are supported in the `VFS`, but not linked to the
  interface. As an example, there is a `Create File` option in the top menu.
* Storing project info: it is quite easy to add some Project model functionality, at least to maintain, for example,
  state of editors, opened files, etc.
* Performance: proper viewport, background processing improvements, advanced data structures can be added relatively
  simple, as the overall structure was built with this in mind.
* IDE language features: with minor adjustments to the language services, we can add more advanced features, such as
  go-to definitions and refactoring.

## IDELang specification

[IDELang-specification.md](./IDELang-specification.md)

## List of supported errors that may occur in code on IDELang

[Supported-errors.md](./Supported-errors.md)