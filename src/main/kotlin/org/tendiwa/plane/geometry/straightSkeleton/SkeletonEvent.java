package org.tendiwa.plane.geometry.straightSkeleton;

import com.sun.istack.internal.NotNull;
import org.tendiwa.geometry.algorithms.distances.DistancesKt;
import org.tendiwa.geometry.points.Point;

abstract class SkeletonEvent implements Comparable<SkeletonEvent> {
    final double distanceToOriginalEdge;
    final Point point;

    /**
     * <i>v<sub>a</sub></i> in [Obdrzalek 1998]
     */

    protected SkeletonEvent(Point point, Node parent) {
        this.point = point;
        this.distanceToOriginalEdge = DistancesKt.distanceToLine(
            point,
            parent.currentEdge
        );
    }

    @Override
    public int compareTo(@NotNull SkeletonEvent o) {
        if (distanceToOriginalEdge > o.distanceToOriginalEdge) {
            return 1;
        } else if (distanceToOriginalEdge < o.distanceToOriginalEdge) {
            return -1;
        }
        return 0;
    }

    abstract void handle(StraightSkeleton skeleton);

    protected void eliminate2NodeLav(
        Node node1,
        StraightSkeleton skeleton
    ) {
//		assert node1.next() == node2 && node2.next() == node1;
        skeleton.outputArc(node1.vertex, node1.next().vertex);
        skeleton.debug.draw2NodeLavArc(node1, node1.next());
        node1.growAdjacentFaces(node1.next());
        node1.setProcessed();
        node1.next().setProcessed();
    }
}
