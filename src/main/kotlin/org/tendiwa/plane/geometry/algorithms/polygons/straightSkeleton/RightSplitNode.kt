package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton


import org.tendiwa.plane.geometry.points.Point

internal class RightSplitNode(
    point: Point,
    previousEdgeStart: OriginalEdgeStart,
    currentEdgeStart: OriginalEdgeStart) : SplitNode(point, previousEdgeStart, currentEdgeStart) {

    internal override val isLeft: Boolean
        get() = false
}
