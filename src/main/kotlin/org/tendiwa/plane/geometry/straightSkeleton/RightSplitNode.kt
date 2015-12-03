package org.tendiwa.plane.geometry.straightSkeleton


import org.tendiwa.geometry.points.Point

internal class RightSplitNode(
    point: Point,
    previousEdgeStart: OriginalEdgeStart,
    currentEdgeStart: OriginalEdgeStart) : SplitNode(point, previousEdgeStart, currentEdgeStart) {

    internal override val isLeft: Boolean
        get() = false
}
