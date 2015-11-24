package org.tendiwa.plane.geometry.straightSkeleton

import org.junit.Test
import org.tendiwa.geometry.dimensions.area
import org.tendiwa.geometry.dimensions.by
import org.tendiwa.geometry.points.AnyPoint
import org.tendiwa.geometry.rectangles.Rectangle
import org.tendiwa.geometry.rectangles.size
import org.tendiwa.geometry.trails.Polygon
import org.tendiwa.geometry.trails.moveTo
import org.tendiwa.plane.directions.CardinalDirection.E
import org.tendiwa.plane.directions.CardinalDirection.S
import org.tendiwa.plane.directions.OrdinalDirection.NE
import org.tendiwa.plane.directions.OrdinalDirection.SW
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EdgePerturbationsTest {
    @Test fun perturbsRectangle() {
        Rectangle(AnyPoint(), 4.0 by 4.0).apply {
            assertNotEquals(
                parallelAndPerpendicularEdgesDeflected.points,
                points
            )
        }
    }

    @Test fun perturbationDoesntChangeRectangleAreaMuch() {
        Rectangle(AnyPoint(), 5.0 by 5.0).apply {
            assert(
                parallelAndPerpendicularEdgesDeflected.hull.size.area < hull.size.area * 1.01
            )
        }
    }

    @Test fun doesntPerturbIrregularPolygon() {
        Polygon(AnyPoint(), { move(10.0, E); move(6.0, SW) })
            .apply {
                assertEquals(
                    parallelAndPerpendicularEdgesDeflected.points,
                    points
                )
            }
    }

    @Test fun perturbsPolygonWithPerpendicularButWithNoParallelSegments() {
        Polygon(AnyPoint(), { move(10.0, E); move(10.0, S) })
            .apply {
                assertNotEquals(
                    parallelAndPerpendicularEdgesDeflected.points,
                    points
                )
            }
    }

    @Test fun positiveAndNegativeInfilitySlopesTreatedAsEqual() {
        val start = AnyPoint()
        Polygon(start, {
            move(3.0, NE)
            move(8.0, S)
            moveTo(start.x, start.y + 4)
        }) // Has 2 vertical parallel segments
            .apply {
                assertNotEquals(
                    parallelAndPerpendicularEdgesDeflected.points,
                    points
                )
            }
    }
}
