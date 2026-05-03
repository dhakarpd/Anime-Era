package com.github.dhakarpd.animeera.data.local.typeConvertor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StringListConverterTest {

    private lateinit var converter: StringListConverter

    @Before
    fun setUp() {
        converter = StringListConverter()
    }

    @Test
    fun `fromList returns comma separated string when list is provided`() {
        val list = listOf("Action", "Adventure", "Comedy")
        val result = converter.fromList(list)
        assertEquals("Action,Adventure,Comedy", result)
    }

    @Test
    fun `fromList returns empty string when list is empty`() {
        val list = emptyList<String>()
        val result = converter.fromList(list)
        assertEquals("", result)
    }

    @Test
    fun `fromList returns empty string when list is null`() {
        val result = converter.fromList(null)
        assertEquals("", result)
    }

    @Test
    fun `toList returns list of strings when comma separated string is provided`() {
        val value = "Action,Adventure,Comedy"
        val result = converter.toList(value)
        assertEquals(3, result.size)
        assertEquals("Action", result[0])
        assertEquals("Adventure", result[1])
        assertEquals("Comedy", result[2])
    }

    @Test
    fun `toList returns single item list when string has no comma`() {
        val value = "Action"
        val result = converter.toList(value)
        assertEquals(1, result.size)
        assertEquals("Action", result[0])
    }

    @Test
    fun `toList returns list of strings when string even has comma separated string in between`() {
        val value = "Action,,Adventure"
        val result = converter.toList(value)
        assertEquals(3, result.size)
        assertEquals("Action", result[0])
    }

    @Test
    fun `toList returns list of strings when string even has only comma separated string`() {
        val value = ","
        val result = converter.toList(value)
        assertEquals(2, result.size)
        assertEquals("", result[0])
    }

    @Test
    fun `toList returns empty list when string is empty`() {
        val value = ""
        val result = converter.toList(value)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toList returns empty list when string is blank`() {
        val value = "   "
        val result = converter.toList(value)
        assertTrue(result.isEmpty())
    }
}
