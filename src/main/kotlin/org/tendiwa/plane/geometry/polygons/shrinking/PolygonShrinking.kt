package org.tendiwa.plane.geometry.polygons.shrinking

import org.tendiwa.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.straightSkeleton.StraightSkeleton

fun Polygon.shrink(amount: Double): Set<Polygon> =
    StraightSkeleton(this).cap(amount)
