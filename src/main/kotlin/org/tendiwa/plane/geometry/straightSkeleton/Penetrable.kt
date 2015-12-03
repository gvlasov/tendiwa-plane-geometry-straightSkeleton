package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.points.Point
import org.tendiwa.geometry.segments.Segment
import java.util.*

internal interface Penetrable {
    fun add(poll: Point, poll1: Point)

    fun obtainIntersectionPoint(
        intersected: Segment,
        intersecting: Segment): Optional<Point>

    fun depth(): Double
}
