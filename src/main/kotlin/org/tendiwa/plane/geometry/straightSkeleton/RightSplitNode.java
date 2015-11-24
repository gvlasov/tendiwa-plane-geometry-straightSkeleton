package org.tendiwa.plane.geometry.straightSkeleton;


import org.tendiwa.geometry.points.Point;

final class RightSplitNode extends SplitNode {
    RightSplitNode(
        Point point,
        OriginalEdgeStart previousEdgeStart,
        OriginalEdgeStart currentEdgeStart
    ) {
        super(point, previousEdgeStart, currentEdgeStart);
    }

    @Override
    boolean isLeft() {
        return false;
    }
}
