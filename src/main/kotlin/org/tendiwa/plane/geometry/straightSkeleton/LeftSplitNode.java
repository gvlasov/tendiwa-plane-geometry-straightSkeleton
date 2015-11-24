package org.tendiwa.plane.geometry.straightSkeleton;


import org.tendiwa.geometry.points.Point;

final class LeftSplitNode extends SplitNode {

	LeftSplitNode(Point point, OriginalEdgeStart previousEdgeStart, OriginalEdgeStart currentEdgeStart) {
		super(point, previousEdgeStart, currentEdgeStart);
	}

	@Override
	boolean isLeft() {
		return true;
	}

}
