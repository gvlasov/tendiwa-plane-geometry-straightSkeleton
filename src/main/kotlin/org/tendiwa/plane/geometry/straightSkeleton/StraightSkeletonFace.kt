package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.points.Point
import org.tendiwa.geometry.polygons.Polygon
import org.tendiwa.geometry.polygons.lastSegment

class StraightSkeletonFace(points: List<Point>) {
    val polygon = Polygon(points)
    val front = polygon.lastSegment
}
