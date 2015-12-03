package org.tendiwa.plane.geometry.straightSkeleton

import org.junit.Test
import org.tendiwa.geometry.rectangles.Rectangle
import org.tendiwa.geometry.trails.Trail
import org.tendiwa.geometry.trails.polygon
import kotlin.test.assertEquals

class StraightSkeletonTest {
    @Test fun skeletonizesTriangle() {
        assertEquals(
            3,
            StraightSkeleton(
                Trail(0.0, 0.0).apply {
                    move(10.0, 0.0)
                    move(10.0, 10.0)
                }.polygon
            ).graph().edgeSet().size
        )
    }

    @Test fun skeletonizesRectangle() {
        // 4 obvious edges from corners to the center + 1 additional really
        // short edge because of perturbation.
        assertEquals(
            5,
            StraightSkeleton(
                Rectangle(1.2, 3.4, 5.6, 7.8)
            ).graph().edgeSet().size
        )
    }
}
