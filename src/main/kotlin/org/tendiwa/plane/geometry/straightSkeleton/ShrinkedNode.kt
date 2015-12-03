package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.points.Point

internal class ShrinkedNode(
    point: Point,
    previousEdgeStart: OriginalEdgeStart,
    currentEdgeStart: OriginalEdgeStart) : Node(point, previousEdgeStart, currentEdgeStart) {

    internal override fun hasPair(): Boolean {
        return false
    }
}
