package com.github.jcornaz.kwik.evaluator

import com.github.jcornaz.kwik.generator.api.Generator
import com.github.jcornaz.kwik.generator.api.withSamples
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CheckForAll3Test : AbstractRunnerTest() {

    private val testGenerator1 = Generator.create { it.nextInt() }
    private val testGenerator2 = Generator.create { it.nextDouble() }
    private val testGenerator3 = Generator.create { it.nextLong() }

    override fun evaluate(iterations: Int, seed: Long, invocation: PropertyEvaluationContext.() -> Boolean) {
        checkForAll(
            testGenerator1,
            testGenerator2,
            testGenerator3,
            iterations,
            seed
        ) { _, _, _ -> assertTrue(invocation()) }
    }

    @Test
    fun evaluateSamples() {
        val ints = mutableSetOf<Int>()
        val doubles = mutableSetOf<Double>()
        val longs = mutableSetOf<Long>()

        val gen1 = Generator.create { it.nextInt(0, 10) }.withSamples(42, 100)
        val gen2 = Generator.create { it.nextDouble(0.0, 10.0) }.withSamples(123.0, 678.0)
        val gen3 = Generator.create { it.nextLong(0L, 10L) }.withSamples(-42L)

        checkForAll(gen1, gen2, gen3) { i, d, l ->
            ints += i
            doubles += d
            longs += l
        }

        assertTrue(42 in ints)
        assertTrue(100 in ints)
        assertTrue(123.0 in doubles)
        assertTrue(123.0 in doubles)
        assertTrue(-42L in longs)
    }

    @Test
    fun errorDisplayHelpfulMessage() {
        val exception = assertFailsWith<FalsifiedPropertyError> {
            var i = 0
            checkForAll(
                Generator.create { 42 },
                Generator.create { -4.1 },
                Generator.create { 100L },
                iterations = 123, seed = 78
            ) { _, _, _ ->
                if (++i >= 12 ) error("failed")
            }
        }

        assertEquals(
            """
                Property falsified after 12 tests (out of 123)
                Argument 1: 42
                Argument 2: -4.1
                Argument 3: 100
                Generation seed: 78
            """.trimIndent(),
            exception.message
        )
    }

    @Test
    fun evaluateForRandomValues() {
        val valuesA = mutableSetOf<Int>()
        val valuesB = mutableSetOf<Double>()
        val valuesC = mutableSetOf<Long>()

        checkForAll(
            testGenerator1,
            testGenerator2,
            testGenerator3,
            seed = 0L
        ) { a, b, c ->
            valuesA += a
            valuesB += b
            valuesC += c
        }

        assertTrue(valuesA.size > 190)
    }

    @Test
    fun isPredictable() {
        val pass1 = mutableListOf<Triple<Int, Double, Long>>()
        val pass2 = mutableListOf<Triple<Int, Double, Long>>()

        val seed = 123564L

        checkForAll(
            testGenerator1,
            testGenerator2,
            testGenerator3,
            seed = seed
        ) { a, b, c ->
            pass1 += Triple(a, b, c)
        }

        checkForAll(
            testGenerator1,
            testGenerator2,
            testGenerator3,
            seed = seed
        ) { a, b, c ->
            pass2 += Triple(a, b, c)
        }

        assertEquals(pass1, pass2)
    }

    @Test
    @Suppress("USELESS_IS_CHECK")
    fun canBeCalledWithoutExplicitGenerator() {
        checkForAll { a: Int, b: Long, c: Double ->
            assertTrue(a is Int && b is Long && c is Double)
        }
    }
}
