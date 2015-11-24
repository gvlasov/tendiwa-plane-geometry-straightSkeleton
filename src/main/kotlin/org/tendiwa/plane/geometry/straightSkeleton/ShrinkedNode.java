package org.tendiwa.plane.geometry.straightSkeleton;

import org.tendiwa.geometry.points.Point;

final class ShrinkedNode extends Node {
	ShrinkedNode(
			Point point,
			OriginalEdgeStart previousEdgeStart,
			OriginalEdgeStart currentEdgeStart
	) {
		super(point, previousEdgeStart, currentEdgeStart);
	}

	@Override
	boolean hasPair() {
		return false;
	}
}
