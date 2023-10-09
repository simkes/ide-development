package editor

interface TextEditor {
    fun add(c: Char, pos: Int)

    fun delete(pos: Int)

    fun getText() : String
}