package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.polygons.lastSegment

class StraightSkeletonFace(points: List<Point>) {
    val polygon = Polygon(points)
    val front = polygon.lastSegment
}
