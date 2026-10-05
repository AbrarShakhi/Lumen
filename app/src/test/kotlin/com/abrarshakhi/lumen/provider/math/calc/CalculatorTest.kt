package com.abrarshakhi.lumen.provider.math.calc

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CalculatorTest {

    private fun eval(input: String): Double =
        assertNotNull(Calculator.evaluate(input), "expected '$input' to evaluate").value

    private fun assertEval(expected: Double, input: String, tolerance: Double = 1e-9) =
        assertEquals(expected, eval(input), tolerance, "for '$input'")

    // --- Arithmetic and precedence ---------------------------------------------------------

    @Test
    fun `basic arithmetic`() {
        assertEval(4.0, "2 + 2")
        assertEval(6.0, "8 - 2")
        assertEval(12.0, "3 * 4")
        assertEval(2.5, "5 / 2")
    }

    @Test
    fun `multiplication binds tighter than addition`() {
        assertEval(14.0, "2 + 3 * 4")
        assertEval(20.0, "(2 + 3) * 4")
    }

    @Test
    fun `exponentiation binds tighter than multiplication and is right associative`() {
        assertEval(18.0, "2 * 3 ^ 2")
        // 2^(3^2) = 2^9 = 512, not (2^3)^2 = 64.
        assertEval(512.0, "2 ^ 3 ^ 2")
    }

    @Test
    fun `subtraction is left associative`() {
        assertEval(1.0, "10 - 6 - 3")
    }

    @Test
    fun `unary minus works in every position`() {
        assertEval(-1.0, "3 + -4")
        assertEval(8.0, "-(-8)")
        assertEval(-8.0, "-2 ^ 3")
    }

    @Test
    fun `nested parentheses`() {
        assertEval(30.0, "((2 + 3) * (4 + 2))")
    }

    // --- Input people actually type ---------------------------------------------------------

    @Test
    fun `alternative operator symbols are accepted`() {
        assertEval(12.0, "3 × 4")
        assertEval(4.0, "8 ÷ 2")
        assertEval(12.0, "3 x 4")
    }

    @Test
    fun `thousands separators inside numbers are ignored`() {
        assertEval(2_000_000.0, "1,000,000 + 1,000,000")
    }

    @Test
    fun `implicit multiplication`() {
        assertEval(14.0, "2(3 + 4)")
        assertEval(2 * PI, "2pi")
        assertEval(24.0, "(2)(3)(4)")
    }

    @Test
    fun `whitespace is irrelevant`() {
        assertEval(4.0, "2+2")
        assertEval(4.0, "  2   +   2  ")
    }

    // --- Percent ----------------------------------------------------------------------------

    @Test
    fun `postfix percent divides by a hundred`() {
        assertEval(0.15, "15%")
        assertEval(12.0, "15% * 80")
    }

    @Test
    fun `of reads as multiplication`() {
        assertEval(12.0, "15% of 80")
        assertEval(50.0, "50% of 100")
    }

    // --- Functions and constants -------------------------------------------------------------

    @Test
    fun `functions work with and without parentheses`() {
        assertEval(4.0, "sqrt(16)")
        assertEval(4.0, "sqrt 16")
        assertEval(2.0, "log(100)")
        assertEval(3.0, "abs(-3)")
    }

    @Test
    fun `constants are available`() {
        assertEval(PI, "pi")
        assertEval(2 * PI, "tau")
        // `e` is declined on its own (see below) but works wherever intent is unambiguous.
        assertEval(1.0, "ln(e)")
        assertEval(Math.E * 2, "e * 2")
    }

    @Test
    fun `functions compose with arithmetic`() {
        assertEval(10.0, "sqrt(16) + sqrt(36)")
        assertEval(5.0, "sqrt(9 + 16)")
    }

    // --- Non-expressions and errors ----------------------------------------------------------

    @Test
    fun `a bare number is not a calculation`() {
        // Echoing "42 = 42" back at the user is noise, not an answer.
        assertNull(Calculator.evaluate("42"))
        assertNull(Calculator.evaluate("3.14"))
        // A signed literal is still just a number.
        assertNull(Calculator.evaluate("-5"))
    }

    @Test
    fun `a lone single letter is not a constant lookup`() {
        // "e" while typing an app name must not turn into 2.718.
        assertNull(Calculator.evaluate("e"))
        assertEval(kotlin.math.PI, "pi")
    }

    @Test
    fun `plain text is not a calculation`() {
        assertNull(Calculator.evaluate("chrome"))
        assertNull(Calculator.evaluate("call mum"))
        assertNull(Calculator.evaluate(""))
        assertNull(Calculator.evaluate("   "))
    }

    @Test
    fun `division by zero is an error, not infinity`() {
        // Returning ∞ would look like the calculation had succeeded.
        assertNull(Calculator.evaluate("1 / 0"))
        assertNull(Calculator.evaluate("5 / (3 - 3)"))
    }

    @Test
    fun `malformed expressions yield nothing`() {
        assertNull(Calculator.evaluate("2 +"))
        assertNull(Calculator.evaluate("(2 + 3"))
        assertNull(Calculator.evaluate("2 + 3)"))
        assertNull(Calculator.evaluate("* 5"))
        assertNull(Calculator.evaluate("2 $ 3"))
    }

    @Test
    fun `unknown names yield nothing`() {
        assertNull(Calculator.evaluate("frobnicate(3)"))
    }

    @Test
    fun `an app name that looks vaguely mathematical is not hijacked`() {
        // The calculator must not claim queries meant for other providers.
        assertNull(Calculator.evaluate("x"))
        assertNull(Calculator.evaluate("notes"))
    }
}
