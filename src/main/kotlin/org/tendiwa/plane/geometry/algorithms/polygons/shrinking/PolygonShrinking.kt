package org.tendiwa.plane.geometry.algorithms.polygons.shrinking

import org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton.StraightSkeleton
import org.tendiwa.plane.geometry.polygons.Polygon

fun Polygon.shrink(amount: Double): Set<Polygon> =
    StraightSkeleton(this).cap(amount)
