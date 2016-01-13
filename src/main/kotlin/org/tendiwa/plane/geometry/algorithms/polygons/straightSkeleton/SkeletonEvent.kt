package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.points.distanceToLine

internal abstract class SkeletonEvent
/**
 * *va* in [Obdrzalek 1998]
 */
protected constructor(
    val point: Point,
    parent: Node
) : Comparable<SkeletonEvent> {
    val distanceToOriginalEdge: Double

    init {
        this.distanceToOriginalEdge = point.distanceToLine(parent.currentEdge())
    }

    override fun compareTo(o: SkeletonEvent): Int {
        if (distanceToOriginalEdge > o.distanceToOriginalEdge) {
            return 1
        } else if (distanceToOriginalEdge < o.distanceToOriginalEdge) {
            return -1
        }
        return 0
    }

    internal abstract fun handle(skeleton: StraightSkeleton)

    protected fun eliminate2NodeLav(
        node1: Node,
        skeleton: StraightSkeleton) {
        //		assert node1.next() == node2 && node2.next() == node1;
        skeleton.outputArc(node1.vertex, node1.next().vertex)
        skeleton.debug.draw2NodeLavArc(node1, node1.next())
        node1.growAdjacentFaces(node1.next())
        node1.setProcessed()
        node1.next().setProcessed()
    }
}
