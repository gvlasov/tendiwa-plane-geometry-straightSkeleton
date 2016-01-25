package org.tendiwa.plane.geometry.algorithms.polygons.shrinking

import org.junit.Test
import org.tendiwa.plane.geometry.rectangles.Rectangle
import kotlin.test.assertEquals

class PolygonShrinkingKtTest {
    @Test
    fun `shrinks polygon`() {
        Rectangle(0.0, 0.0, 10.0, 10.0)
            .shrink(1.0)
            .apply { assertEquals(1, size) }
    }
}
