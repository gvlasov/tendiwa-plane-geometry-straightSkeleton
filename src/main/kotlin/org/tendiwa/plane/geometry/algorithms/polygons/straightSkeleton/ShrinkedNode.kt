package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point

internal class ShrinkedNode(
    point: Point,
    previousEdgeStart: OriginalEdgeStart,
    currentEdgeStart: OriginalEdgeStart
) : Node(point, previousEdgeStart, currentEdgeStart) {

    internal override fun hasPair(): Boolean {
        return false
    }
}
