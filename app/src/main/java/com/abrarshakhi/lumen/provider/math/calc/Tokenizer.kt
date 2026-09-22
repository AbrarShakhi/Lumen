package com.abrarshakhi.lumen.provider.math.calc

/** A lexical unit of an arithmetic expression. */
internal sealed interface Token {
    data class Number(val value: Double) : Token
    data class Identifier(val name: String) : Token
    data class Operator(val symbol: String) : Token
    data object LeftParen : Token
    data object RightParen : Token
}

internal class TokenizeException(message: String) : Exception(message)

/**
 * Splits an expression into tokens.
 *
 * Accepts the punctuation people actually type rather than a strict grammar: `×` and `x`
 * for multiply, `÷` for divide, and thousands separators inside numbers — a launcher field
 * is not a programming language prompt.
 */
internal object Tokenizer {

    private val OPERATORS = setOf("+", "-", "*", "/", "^", "%")

    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0

        while (index < input.length) {
            val char = input[index]

            when {
                char.isWhitespace() -> index++

                char.isDigit() || char == '.' -> {
                    val (number, next) = readNumber(input, index)
                    tokens += Token.Number(number)
                    index = next
                }

                char.isLetter() -> {
                    val (name, next) = readIdentifier(input, index)
                    // `x` between operands is multiplication, not a variable — nothing in
                    // Lumen's calculator has variables, so the ambiguity is safe to resolve.
                    if (name == "x" && tokens.lastOperandLike()) {
                        tokens += Token.Operator("*")
                    } else {
                        tokens += Token.Identifier(name)
                    }
                    index = next
                }

                char == '(' -> { tokens += Token.LeftParen; index++ }
                char == ')' -> { tokens += Token.RightParen; index++ }

                char == '×' -> { tokens += Token.Operator("*"); index++ }
                char == '÷' -> { tokens += Token.Operator("/"); index++ }
                char == '−' -> { tokens += Token.Operator("-"); index++ }

                char.toString() in OPERATORS -> { tokens += Token.Operator(char.toString()); index++ }

                else -> throw TokenizeException("Unexpected character '$char'")
            }
        }

        return tokens
    }

    private fun readNumber(input: String, start: Int): Pair<Double, Int> {
        var index = start
        val builder = StringBuilder()
        var seenDot = false

        while (index < input.length) {
            val char = input[index]
            when {
                char.isDigit() -> builder.append(char)
                // A comma is a thousands separator here; it is never a decimal point,
                // because an argument list would need functions of several arguments and
                // this calculator has none.
                char == ',' && index + 1 < input.length && input[index + 1].isDigit() -> Unit
                char == '.' && !seenDot -> { seenDot = true; builder.append(char) }
                else -> break
            }
            index++
        }

        val text = builder.toString()
        val value = text.toDoubleOrNull() ?: throw TokenizeException("Invalid number '$text'")
        return value to index
    }

    private fun readIdentifier(input: String, start: Int): Pair<String, Int> {
        var index = start
        while (index < input.length && input[index].isLetter()) index++
        return input.substring(start, index).lowercase() to index
    }

    /** True when the previous token could end an operand, so `x` here means multiply. */
    private fun List<Token>.lastOperandLike(): Boolean = when (lastOrNull()) {
        is Token.Number, is Token.Identifier, Token.RightParen -> true
        else -> false
    }
}
