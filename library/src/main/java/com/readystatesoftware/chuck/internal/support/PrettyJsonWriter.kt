package com.readystatesoftware.chuck.internal.support

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.text.color
import androidx.core.text.italic
import com.google.gson.JsonElement

/**
 * Pretty print JSON data with syntax highlighting.  This class started life as
 * the JsonWriter from the GSON project.  It was first converted to Kotlin and
 * then amended to write to a SpannableStringBuilder rather than a Java Writer.
 * Syntax coloring was then added.  Big thanks to the original GSON authors!
 */
class PrettyJsonWriter(
        @ColorInt private val nameColor: Int,
        @ColorInt private val stringColor: Int,
        @ColorInt private val numberColor: Int = stringColor,
        @ColorInt private val booleanColor: Int = stringColor
) {
    private var stack = IntArray(32)
    private var stackSize = 0
    private var deferredName: String? = null

    val output = SpannableStringBuilder()

    init {
        push(EMPTY_DOCUMENT)
    }

    /**
     * Begins encoding a new array. Each call to this method must be paired with
     * a call to [.endArray].
     *
     * @return this writer.
     */
    fun beginArray(): PrettyJsonWriter {
        writeDeferredName()
        return open(EMPTY_ARRAY, "[")
    }

    /**
     * Ends encoding the current array.
     *
     * @return this writer.
     */
    fun endArray(): PrettyJsonWriter {
        return close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]")
    }

    /**
     * Begins encoding a new object. Each call to this method must be paired
     * with a call to [.endObject].
     *
     * @return this writer.
     */
    fun beginObject(): PrettyJsonWriter {
        writeDeferredName()
        return open(EMPTY_OBJECT, "{")
    }

    /**
     * Ends encoding the current object.
     *
     * @return this writer.
     */
    fun endObject(): PrettyJsonWriter {
        return close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}")
    }

    /**
     * Enters a new scope by appending any necessary whitespace and the given
     * bracket.
     */
    private fun open(empty: Int, openBracket: String): PrettyJsonWriter {
        beforeValue()
        push(empty)
        output.append(openBracket)
        return this
    }

    /**
     * Closes the current scope by appending any necessary whitespace and the
     * given bracket.
     */
    private fun close(empty: Int, nonempty: Int, closeBracket: String): PrettyJsonWriter {
        val context = peek()
        if (context != nonempty && context != empty) {
            throw IllegalStateException("Nesting problem.")
        }
        if (deferredName != null) {
            throw IllegalStateException("Dangling name: " + deferredName!!)
        }

        stackSize--
        if (context == nonempty) {
            newline()
        }
        output.append(closeBracket)
        return this
    }

    private fun push(newTop: Int) {
        if (stackSize == stack.size) {
            val newStack = IntArray(stackSize * 2)
            System.arraycopy(stack, 0, newStack, 0, stackSize)
            stack = newStack
        }
        stack[stackSize++] = newTop
    }

    /**
     * Returns the value on the top of the stack.
     */
    private fun peek(): Int {
        if (stackSize == 0) {
            throw IllegalStateException("PrettyJsonWriter is closed.")
        }
        return stack[stackSize - 1]
    }

    /**
     * Replace the value on the top of the stack with the given value.
     */
    private fun replaceTop(topOfStack: Int) {
        stack[stackSize - 1] = topOfStack
    }

    /**
     * Encodes the property name.
     *
     * @param name the name of the forthcoming value. May not be null.
     * @return this writer.
     */
    fun name(name: String?): PrettyJsonWriter {
        if (name == null) {
            throw NullPointerException("name == null")
        }
        if (deferredName != null) {
            throw IllegalStateException()
        }
        if (stackSize == 0) {
            throw IllegalStateException("PrettyJsonWriter is closed.")
        }
        deferredName = name
        return this
    }

    private fun writeDeferredName() {
        if (deferredName != null) {
            beforeName()
            output.color(nameColor) { literalString(deferredName!!) }
            deferredName = null
        }
    }

    /**
     * Encodes `value`.
     *
     * @param value the literal string value, or null to encode a null literal.
     * @return this writer.
     */
    fun value(value: String?): PrettyJsonWriter {
        if (value == null) {
            return nullValue()
        }
        writeDeferredName()
        beforeValue()
        string(value)
        return this
    }

    /**
     * Encodes `null`.
     *
     * @return this writer.
     */
    fun nullValue(): PrettyJsonWriter {
        if (deferredName != null) {
            writeDeferredName()
        }
        beforeValue()
        output.italic { append("null") }
        return this
    }

    /**
     * Encodes `value`.
     *
     * @return this writer.
     */
    fun value(value: Boolean): PrettyJsonWriter {
        writeDeferredName()
        beforeValue()
        output.color(booleanColor) { append(if (value) "true" else "false") }
        return this
    }

    /**
     * Encodes `value`.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this writer.
     */
    fun value(value: Double): PrettyJsonWriter {
        writeDeferredName()
        beforeValue()
        output.color(numberColor) { append(java.lang.Double.toString(value)) }
        return this
    }

    /**
     * Encodes `value`.
     *
     * @param value a finite value. May not be [NaNs][Double.isNaN] or
     * [infinities][Double.isInfinite].
     * @return this writer.
     */
    fun value(value: Number?): PrettyJsonWriter {
        if (value == null) {
            return nullValue()
        }

        writeDeferredName()
        val string = value.toString()
        beforeValue()
        output.color(numberColor) { append(string) }
        return this
    }

    private fun string(value: String) {
        output.color(stringColor) { literalString(value) }
    }

    private fun SpannableStringBuilder.literalString(value: String) {
        output.append("\"")
        var last = 0
        val length = value.length
        for (i in 0 until length) {
            val c = value[i]
            val replacement: String?
            if (c.toInt() < 128) {
                replacement = REPLACEMENT_CHARS[c.toInt()]
                if (replacement == null) {
                    continue
                }
            } else if (c == '\u2028') {
                replacement = "\\u2028"
            } else if (c == '\u2029') {
                replacement = "\\u2029"
            } else {
                continue
            }
            if (last < i) {
                append(value, last, i+1)
            }
            append(replacement)
            last = i + 1
        }
        if (last < length) {
            append(value, last, length)
        }
        append("\"")
    }

    private fun newline() {
        output.append("\n")
        var i = 1
        val size = stackSize
        while (i < size) {
            output.append("  ")
            i++
        }
    }

    /**
     * Inserts any necessary separators and whitespace before a name. Also
     * adjusts the stack to expect the name's value.
     */
    private fun beforeName() {
        val context = peek()
        if (context == NONEMPTY_OBJECT) { // first in object
            output.append(",")
        } else if (context != EMPTY_OBJECT) { // not in an object!
            throw IllegalStateException("Nesting problem.")
        }
        newline()
        replaceTop(DANGLING_NAME)
    }

    /**
     * Inserts any necessary separators and whitespace before a literal value,
     * inline array, or inline object. Also adjusts the stack to expect either a
     * closing bracket or another element.
     */
    private fun beforeValue() {
        when (peek()) {
            NONEMPTY_DOCUMENT ->
                replaceTop(NONEMPTY_DOCUMENT)

            // fall-through
            // first in document
            EMPTY_DOCUMENT ->
                replaceTop(NONEMPTY_DOCUMENT)

            // first in array
            EMPTY_ARRAY -> {
                replaceTop(NONEMPTY_ARRAY)
                newline()
            }

            // another in array
            NONEMPTY_ARRAY -> {
                output.append(',')
                newline()
            }

            // value for name
            DANGLING_NAME -> {
                output.append(": ")
                replaceTop(NONEMPTY_OBJECT)
            }

            else -> throw IllegalStateException("Nesting problem.")
        }
    }

    fun write(value: JsonElement?) {
        when {
            value == null || value.isJsonNull -> {
                nullValue()
            }
            value.isJsonPrimitive -> {
                val primitive = value.asJsonPrimitive
                when {
                    primitive.isNumber -> value(primitive.asNumber)
                    primitive.isBoolean -> value(primitive.asBoolean)
                    else -> value(primitive.asString)
                }
            }
            value.isJsonArray -> {
                beginArray()
                value.asJsonArray.forEach { write(it) }
                endArray()

            }
            value.isJsonObject -> {
                beginObject()
                value.asJsonObject.entrySet().forEach { (k, v) ->
                    name(k)
                    write(v)
                }
                endObject()
            }
            else -> throw IllegalArgumentException("Couldn't write ${value.javaClass}")
        }
    }


    companion object {
        /**
         * An array with no elements requires no separators or newlines before
         * it is closed.
         */
        private val EMPTY_ARRAY = 1

        /**
         * A array with at least one value requires a comma and newline before
         * the next element.
         */
        private val NONEMPTY_ARRAY = 2

        /**
         * An object with no name/value pairs requires no separators or newlines
         * before it is closed.
         */
        private val EMPTY_OBJECT = 3

        /**
         * An object whose most recent element is a key. The next element must
         * be a value.
         */
        private val DANGLING_NAME = 4

        /**
         * An object with at least one name/value pair requires a comma and
         * newline before the next element.
         */
        private val NONEMPTY_OBJECT = 5

        /**
         * No object or array has been started.
         */
        private val EMPTY_DOCUMENT = 6

        /**
         * A document with at an array or object.
         */
        private val NONEMPTY_DOCUMENT = 7

        /**
         * A document that's been closed and cannot be accessed.
         */
        private val CLOSED = 8

        /*
         * From RFC 7159, "All Unicode characters may be placed within the
         * quotation marks except for the characters that must be escaped:
         * quotation mark, reverse solidus, and the control characters
         * (U+0000 through U+001F)."
         *
         * We also escape '\u2028' and '\u2029', which JavaScript interprets as
         * newline characters. This prevents eval() from failing with a syntax
         * error. http://code.google.com/p/google-gson/issues/detail?id=341
         */
        private val REPLACEMENT_CHARS: Array<String?> = arrayOfNulls(128)

        init {
            for (i in 0..0x1f) {
                REPLACEMENT_CHARS[i] = String.format("\\u%04x", i)
            }
            REPLACEMENT_CHARS['"'.toInt()] = "\\\""
            REPLACEMENT_CHARS['\\'.toInt()] = "\\\\"
            REPLACEMENT_CHARS['\t'.toInt()] = "\\t"
            REPLACEMENT_CHARS['\b'.toInt()] = "\\b"
            REPLACEMENT_CHARS['\n'.toInt()] = "\\n"
            REPLACEMENT_CHARS['\r'.toInt()] = "\\r"
        }
    }
}
