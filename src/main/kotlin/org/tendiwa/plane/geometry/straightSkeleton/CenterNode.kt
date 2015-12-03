package org.tendiwa.plane.geometry.straightSkeleton


import org.tendiwa.geometry.points.Point

internal class CenterNode(point: Point) : Node(point) {

    internal override fun hasPair(): Boolean {
        return false
    }
}
