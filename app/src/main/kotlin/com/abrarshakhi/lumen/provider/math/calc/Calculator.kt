package com.abrarshakhi.lumen.provider.math.calc

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Evaluates an arithmetic expression.
 *
 * A precedence-climbing parser rather than shunting-yard: the same single pass handles
 * right-associative `^`, prefix minus, postfix `%` and implicit multiplication without a
 * separate operator stack, and the recursion mirrors the grammar closely enough to read.
 *
 * Pure Kotlin with no Android dependency, so every rule below is unit-testable.
 */
object Calculator {

    /** Returns null when the input is not an expression at all, rather than throwing. */
    fun evaluate(input: String): Result? {
        val normalized = input.trim()
        if (normalized.isEmpty()) return null
        // Echoing "42 = 42" back at someone is noise, not an answer. This also covers
        // signed literals like "-5", which are numbers rather than calculations.
        if (normalized.toDoubleOrNull() != null) return null

        return try {
            val tokens = Tokenizer.tokenize(normalized)
            // A lone single letter is far likelier to be the start of an app name than a
            // request for Euler's number, so "e" is declined while "pi" and "tau" stand.
            val lone = tokens.singleOrNull()
            if (lone is Token.Identifier && lone.name.length < 2) return null

            val value = Parser(tokens).parse()
            if (value.isNaN()) return null
            Result(value)
        } catch (_: TokenizeException) {
            null
        } catch (_: CalculationException) {
            null
        }
    }

    data class Result(val value: Double)

    private val CONSTANTS = mapOf(
        "pi" to PI,
        "e" to Math.E,
        "tau" to 2 * PI,
    )

    /** Trigonometric functions take radians, as the standard library does. */
    private val FUNCTIONS: Map<String, (Double) -> Double> = mapOf(
        "sqrt" to ::sqrt,
        "cbrt" to ::cbrt,
        "abs" to ::abs,
        "round" to { value -> value.roundToLong().toDouble() },
        "floor" to ::floor,
        "ceil" to ::ceil,
        "ln" to ::ln,
        "log" to ::log10,
        "log2" to ::log2,
        "exp" to ::exp,
        "sin" to ::sin,
        "cos" to ::cos,
        "tan" to ::tan,
        "asin" to ::asin,
        "acos" to ::acos,
        "atan" to ::atan,
    )

    internal class CalculationException(message: String) : Exception(message)

    private class Parser(private val tokens: List<Token>) {

        private var position = 0

        fun parse(): Double {
            val value = parseExpression(MIN_PRECEDENCE)
            if (position < tokens.size) throw CalculationException("Unexpected trailing input")
            return value
        }

        private fun parseExpression(minPrecedence: Int): Double {
            var left = parseUnary()

            while (true) {
                val token = peek() ?: break

                val operator = when {
                    token is Token.Operator -> token.symbol
                    // "15% of 80" reads naturally and means multiplication.
                    token is Token.Identifier && token.name == "of" -> "*"
                    // "2(3+4)" and "2pi" are multiplication by juxtaposition.
                    token.startsOperand() -> IMPLICIT_MULTIPLY
                    else -> break
                }

                val precedence = precedenceOf(operator) ?: break
                if (precedence < minPrecedence) break

                if (operator != IMPLICIT_MULTIPLY) position++

                val nextMinimum = if (isRightAssociative(operator)) precedence else precedence + 1
                val right = parseExpression(nextMinimum)
                left = apply(if (operator == IMPLICIT_MULTIPLY) "*" else operator, left, right)
            }

            return left
        }

        private fun parseUnary(): Double {
            val token = peek()
            if (token is Token.Operator && (token.symbol == "-" || token.symbol == "+")) {
                position++
                val operand = parseExpression(UNARY_PRECEDENCE)
                return if (token.symbol == "-") -operand else operand
            }
            return parsePostfix()
        }

        /** Postfix `%` divides by a hundred, so `15% * 80` is 12. */
        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                val token = peek()
                if (token is Token.Operator && token.symbol == "%") {
                    position++
                    value /= 100.0
                } else {
                    break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = next() ?: throw CalculationException("Unexpected end of expression")

            return when (token) {
                is Token.Number -> token.value

                is Token.Identifier -> {
                    CONSTANTS[token.name]?.let { return it }
                    val function = FUNCTIONS[token.name]
                        ?: throw CalculationException("Unknown name '${token.name}'")
                    // Parentheses are optional: "sqrt 16" is as clear as "sqrt(16)".
                    val argument = parseExpression(UNARY_PRECEDENCE)
                    function(argument)
                }

                Token.LeftParen -> {
                    val value = parseExpression(MIN_PRECEDENCE)
                    if (next() != Token.RightParen) throw CalculationException("Missing ')'")
                    value
                }

                Token.RightParen -> throw CalculationException("Unexpected ')'")

                is Token.Operator -> throw CalculationException("Unexpected operator '${token.symbol}'")
            }
        }

        private fun apply(operator: String, left: Double, right: Double): Double = when (operator) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> {
                // Division by zero yields infinity in IEEE arithmetic; surfacing "∞" as an
                // answer would imply the calculation succeeded.
                if (right == 0.0) throw CalculationException("Division by zero")
                left / right
            }
            "^" -> left.pow(right)
            else -> throw CalculationException("Unknown operator '$operator'")
        }

        private fun peek(): Token? = tokens.getOrNull(position)

        private fun next(): Token? = tokens.getOrNull(position)?.also { position++ }

        private fun Token.startsOperand(): Boolean =
            this is Token.Number || this is Token.Identifier || this == Token.LeftParen
    }

    private const val MIN_PRECEDENCE = 0
    private const val UNARY_PRECEDENCE = 3
    private const val IMPLICIT_MULTIPLY = "implicit"

    private fun precedenceOf(operator: String): Int? = when (operator) {
        "+", "-" -> 1
        "*", "/" -> 2
        IMPLICIT_MULTIPLY -> 2
        "^" -> 4
        // Postfix, handled before binary operators are considered.
        "%" -> null
        else -> null
    }

    private fun isRightAssociative(operator: String): Boolean = operator == "^"
}
