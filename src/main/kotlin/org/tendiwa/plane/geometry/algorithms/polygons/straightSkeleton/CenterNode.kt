package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton


import org.tendiwa.plane.geometry.points.Point

internal class CenterNode(point: Point) : Node(point) {

    internal override fun hasPair(): Boolean {
        return false
    }
}
