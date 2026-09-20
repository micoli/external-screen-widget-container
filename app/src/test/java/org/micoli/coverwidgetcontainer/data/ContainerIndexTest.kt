package org.micoli.coverwidgetcontainer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContainerIndexTest {
    @Test
    fun `parses index from provider class name`() {
        assertEquals(7, ContainerIndex.fromClassName("org.micoli.coverwidgetcontainer.cover.Container07Provider"))
        assertEquals(20, ContainerIndex.fromClassName("Container20Provider"))
    }

    @Test
    fun `rejects unknown or out of range names`() {
        assertNull(ContainerIndex.fromClassName("Container21Provider"))
        assertNull(ContainerIndex.fromClassName("Container00Provider"))
        assertNull(ContainerIndex.fromClassName("SomethingElse"))
    }

    @Test
    fun `builds provider class name`() {
        assertEquals("a.b.cover.Container03Provider", ContainerIndex.providerClassName("a.b", 3))
    }
}
