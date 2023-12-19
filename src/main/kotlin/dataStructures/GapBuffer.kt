package dataStructures

import editor.TextBuffer

class GapBuffer(initialText: String = "", private val initialGapSize: Int = 100) : TextBuffer {

    private var buffer: CharArray
    private var gapStart: Int
    private var gapEnd: Int

    init {
        if (initialGapSize <= 0) throw IllegalArgumentException("init: gap size $initialGapSize.")

        buffer = CharArray(initialText.length + initialGapSize)

        for (i in initialText.indices) {
            buffer[i] = initialText[i]
        }

        gapStart = initialText.length
        gapEnd = buffer.size - 1
    }

    override val size: Int
        get() = buffer.size - (gapEnd - gapStart + 1)
    override val lineCount: Int
        get() = buffer.count { it == '\n' }

    override fun add(c: Char, pos: Int) {
        if (pos < 0 || pos > size) throw IllegalArgumentException("add: pos $pos.")
        moveGapToPosition(pos)
        if (gapStart == gapEnd) {
            resize(initialGapSize, pos)
        }
        buffer[gapStart] = c
        gapStart++
    }

    override fun delete(pos: Int) {
        if (pos < 0 || pos >= size) throw IllegalArgumentException("delete: pos $pos.")
        moveGapToPosition(pos + 1)
        buffer[pos] = Char(0)
        gapStart--
    }

    override fun getText(): String = buffer.filter { it != Char(0) }.joinToString("")

    override fun getCharAtPos(pos: Int): Char {
        if (pos < 0 || pos >= size) throw IllegalArgumentException("get: pos $pos.")
        val position = translateTextIntoBufferCoordinates(pos)
        return buffer[position]
    }

    private fun translateTextIntoBufferCoordinates(textPos: Int): Int {
        if (textPos <= gapStart)
            return textPos
        return textPos + (gapEnd - gapStart + 1)
    }

    // after end of function we have gap start right at position
    private fun moveGapToPosition(position: Int) {
        while (position < gapStart) {
            moveLeft()
        }
        while (position > gapStart) {
            moveRight()
        }
    }

    private fun moveLeft() {
        if (gapStart > 0) {
            gapStart--
            gapEnd--
            buffer[gapEnd + 1] = buffer[gapStart]
            buffer[gapStart] = Char(0)
        }
    }

    private fun moveRight() {
        if (gapEnd + 1 < buffer.size) {
            gapStart++
            gapEnd++
            buffer[gapStart - 1] = buffer[gapEnd]
            buffer[gapEnd] = Char(0)
        }
    }

    private fun resize(gapSize: Int, position: Int) {
        val newBuffer = CharArray(size + gapSize)

        for (i in 0 until position) {
            newBuffer[i] = buffer[i]
        }

        for (i in position until buffer.size - 1) {
            newBuffer[i + gapSize] = buffer[i + 1]
        }

        buffer = newBuffer
        gapStart = position
        gapEnd = position + gapSize - 1
    }
}
