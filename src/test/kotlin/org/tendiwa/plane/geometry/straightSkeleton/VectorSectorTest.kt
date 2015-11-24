package org.tendiwa.plane.geometry.straightSkeleton

import org.junit.Test
import org.tendiwa.geometry.vectors.HorizontalVector
import org.tendiwa.geometry.vectors.VerticalVector
import org.tendiwa.geometry.vectors.plus
import org.tendiwa.geometry.vectors.unaryMinus
import kotlin.test.assertEquals

class VectorSectorTest {
    @Test fun bisector() {
        val cw = VerticalVector(10.0)
        val ccw = HorizontalVector(10.0)
        assertEquals(
            VectorSector(cw, ccw).bisector,
            ccw + cw
        )
    }

    @Test fun sumVector() {
        val cw = HorizontalVector(10.0)
        val ccw = VerticalVector(10.0)
        assertEquals(
            VectorSector(cw, ccw).sumVector,
            -(cw + ccw)
        )
    }
}
