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

object Calculator {

    fun evaluate(input: String): Result? {
        val normalized = input.trim()
        if (normalized.isEmpty()) return null
        if (normalized.toDoubleOrNull() != null) return null

        return try {
            val tokens = Tokenizer.tokenize(normalized)
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
                    token is Token.Identifier && token.name == "of" -> "*"
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
        "%" -> null
        else -> null
    }

    private fun isRightAssociative(operator: String): Boolean = operator == "^"
}
