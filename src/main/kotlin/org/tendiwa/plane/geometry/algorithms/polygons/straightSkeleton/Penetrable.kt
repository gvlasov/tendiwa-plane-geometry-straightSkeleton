package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.segments.Segment

internal interface Penetrable {
    fun add(poll: Point, poll1: Point)

    fun obtainIntersectionPoint(
        intersected: Segment,
        intersecting: Segment
    ): Point?

    val depth: Double
}
