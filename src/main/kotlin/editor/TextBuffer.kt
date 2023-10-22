package editor

interface TextBuffer {
    fun add(c: Char, pos: Int)

    fun delete(pos: Int)

    fun getText() : String
}