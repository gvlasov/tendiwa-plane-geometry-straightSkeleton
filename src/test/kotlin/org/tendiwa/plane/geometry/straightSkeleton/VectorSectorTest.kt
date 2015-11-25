package org.tendiwa.plane.geometry.straightSkeleton

import org.junit.Test
import org.tendiwa.geometry.vectors.*
import kotlin.test.assertEquals

class VectorSectorTest {
    @Test fun bisector() {
        val cw = VerticalVector(10.0)
        val ccw = HorizontalVector(10.0)
        assertEquals(
            VectorSector(cw, ccw).bisector,
            (ccw + cw).normalized * 10.0
        )
    }

    @Test fun sumVector() {
        val cw = HorizontalVector(10.0)
        val ccw = VerticalVector(10.0)
        assertEquals(
            VectorSector(cw, ccw).sumVector,
            (cw + ccw).normalized * 10.0
        )
    }
}
