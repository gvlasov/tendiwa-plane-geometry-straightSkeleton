package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.junit.Test
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.rectangles.Rectangle
import kotlin.test.assertEquals

class StraightSkeletonTest {
    @Test fun skeletonizesTriangle() {
        assertEquals(
            3,
            StraightSkeleton(
                Polygon(
                    Point(0.0, 0.0),
                    {
                        move(10.0, 0.0)
                        move(10.0, 10.0)
                    }
                )
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
