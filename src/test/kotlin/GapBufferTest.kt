import dataStructures.GapBuffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GapBufferTest {

    @Test
    fun `initializes with empty text and default gap size`() {
        val buffer = GapBuffer()
        assertEquals("", buffer.getText())
        assertEquals(0, buffer.size)
    }

    @Test
    fun `initializes with non-empty text and default gap size`() {
        val initialText = "Hello"
        val buffer = GapBuffer(initialText)
        assertEquals(initialText, buffer.getText())
        assertEquals(initialText.length, buffer.size)
    }

    @Test
    fun `initializes with custom gap size`() {
        val initialText = "Test"
        val customGapSize = 2
        val buffer = GapBuffer(initialText, customGapSize)
        assertEquals(initialText, buffer.getText())
        assertEquals(initialText.length, buffer.size)
    }

    @Test
    fun `throws IllegalArgumentException for zero or negative gap size`() {
        val initialText = "Example"
        val zeroGapSize = 0
        val negativeGapSize = -10

        assertThrows<IllegalArgumentException> {
            GapBuffer(initialText, zeroGapSize)
        }

        assertThrows<IllegalArgumentException> {
            GapBuffer(initialText, negativeGapSize)
        }
    }

    @Test
    fun `adds character to empty buffer`() {
        val buffer = GapBuffer()
        buffer.add('a', 0)
        assertEquals("a", buffer.getText())
    }

    @Test
    fun `adds character to non-empty buffer at various positions`() {
        val buffer = GapBuffer("abc", 2)
        buffer.add('x', 0)
        buffer.add('y', 2)
        buffer.add('z', 5)
        assertEquals("xaybcz", buffer.getText())
    }

    @Test
    fun `deletes character from various positions`() {
        val buffer = GapBuffer("abcde", 2)
        buffer.delete(0)
        buffer.delete(1)
        buffer.delete(1)
        assertEquals("be", buffer.getText())
    }

    @Test
    fun `adds character at buffer's size limit triggering resizing`() {
        val initialText = "abc"
        val buffer = GapBuffer(initialText, 2)
        buffer.add('d', 3)
        buffer.add('e', 4)
        buffer.add('g', 5)
        assertEquals("abcdeg", buffer.getText())
    }

    @Test
    fun `complex test`() {
        val initialText =
            "The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To achieve this, it would be necessary to have uniform grammar, pronunciation and more common words. If several languages coalesce, the grammar of the resulting language is more simple and regular than that of the individual languages. The new common language will be more simple and regular than the existing European languages. It will be as simple as Occidental; in fact, it will be Occidental. To an English person, it will seem like simplified English, as a skeptical Cambridge friend of mine told me what Occidental is. The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To achieve this, it would be necessary to have uniform grammar, pronunciation and more common words. If several languages coalesce, the grammar of the resulting language is more simple and regular than that of the individual languages. The new common language will be more simple and regular than the existing European languages. It will be as simple as Occidental; in fact, it will be Occidental. To an English person, it will seem like simplified English, as a skeptical Cambridge friend of mine told me what Occidental is. The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To achieve this, it would be necessary to have uniform grammar, pronunciation and more common words. If several languages coalesce, the grammar of the resulting language is more simple and regular than that of the individual languages. The new common language will be more simple and regular than the existing European languages. It will be as simple as Occidental; in fact, it will be Occidental. To an English person, it will seem like simplified English, as a skeptical Cambridge friend of mine told me what Occidental is.The European languages are members of the same family. Their separate existence is a myth. For science, music, sport, etc, Europe uses the same vocabulary. The languages only differ in their grammar, their pronunciation and their most common words. Everyone realizes why a new common language would be desirable: one could refuse to pay expensive translators. To achieve this, it would be necessary to have uniform grammar, pronunciation and more common\n"
        val gapBuffer = GapBuffer(initialText, 20)
        val stringBuilder = StringBuilder(initialText)
        val asciiStart = 32 // space symbol
        val asciiEnd = 127  // end of ASCII

        var curTextSize = initialText.length

        for (i in 1..3) {
            for (asciiCode in asciiStart..asciiEnd) {
                val char = asciiCode.toChar()
                val position = (asciiCode * asciiCode) % (curTextSize + 1)
                stringBuilder.insert(position, char)
                gapBuffer.add(char, position)
                curTextSize++
            }
        }

        assertEquals(stringBuilder.toString(), gapBuffer.getText())

        for (i in 1..3) {
            for (asciiCode in asciiStart..asciiEnd) {
                val position = (asciiCode * asciiCode) % curTextSize
                stringBuilder.delete(position, position + 1)
                gapBuffer.delete(position)
                curTextSize--
            }
        }

        assertEquals(stringBuilder.toString(), gapBuffer.getText())
    }
}