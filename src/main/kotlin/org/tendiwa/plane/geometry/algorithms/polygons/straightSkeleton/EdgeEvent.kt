package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton


import org.tendiwa.plane.geometry.algorithms.distances.distanceToLine
import org.tendiwa.plane.geometry.points.Point

internal class EdgeEvent(
    point: Point,
    private val leftParent: Node,
    /**
     * *vb* in [Obdrzalek 1998] `rightParent == null` means it is a split event,
     * otherwise it is an edge event
     */
    private val rightParent: Node?
) : SkeletonEvent(
    point,
    if (leftIsCloserThanRight(point, leftParent, rightParent!!)) leftParent
    else rightParent
) {


    fun rightParent(): Node {
        assert(rightParent != null)
        return rightParent!!
    }

    fun leftParent(): Node {
        return leftParent
    }

    internal override fun handle(skeleton: StraightSkeleton) {
        if (anyOfNodesIsProcessed()) {
            // Convex 2b
            tryQueueingNewNode(skeleton)
        } else if (lavOf3NodesLeft()) {
            // Convex 2c
            connectLast3SegmentsOfLav(skeleton)
        } else if (leftParent.isInLavOf2Nodes) {
            eliminate2NodeLav(leftParent, skeleton)
        } else {
            shrinkEdge(skeleton)
        }
    }

    private fun anyOfNodesIsProcessed(): Boolean {
        return leftParent.isProcessed || rightParent!!.isProcessed
    }

    private fun tryQueueingNewNode(skeleton: StraightSkeleton) {
        val leftProcessed = leftParent.isProcessed
        val rightProcessed = rightParent!!.isProcessed
        if (!(leftProcessed && rightProcessed)) {
            val unprocessed = if (leftProcessed) rightParent else leftParent
            skeleton.queueEventFromNode(unprocessed)
        }
    }

    private fun connectLast3SegmentsOfLav(skeleton: StraightSkeleton) {
        val centerNode = CenterNode(point)
        skeleton.outputArc(leftParent.vertex, point)
        skeleton.outputArc(rightParent!!.vertex, point)
        skeleton.outputArc(leftParent.previous().vertex, point)
        skeleton.debug.draw3NodeLavArcs(this)

        leftParent.growAdjacentFaces(centerNode)
        rightParent.growAdjacentFaces(centerNode)
        leftParent.previous().growAdjacentFaces(centerNode)

        leftParent.setProcessed()
        rightParent.setProcessed()
        leftParent.previous().setProcessed()

        assert(leftParent.previous() === rightParent.next())
    }

    private fun shrinkEdge(skeleton: StraightSkeleton) {
        // Convex 2d
        skeleton.outputArc(leftParent.vertex, point)
        skeleton.outputArc(rightParent!!.vertex, point)
        skeleton.debug.drawEdgeEventArcs(leftParent, rightParent, point)

        // Convex 2e
        val node = ShrinkedNode(
            point,
            leftParent.previousEdgeStart!!,
            rightParent.currentEdgeStart!!
        )

        leftParent.growAdjacentFaces(node)
        rightParent.growAdjacentFaces(node)

        node.setPreviousInLav(leftParent.previous())
        rightParent.next().setPreviousInLav(node)
        node.computeReflexAndBisector()

        leftParent.setProcessed()
        rightParent.setProcessed()

        // Convex 2f
        skeleton.queueEventFromNode(node)
    }

    private fun lavOf3NodesLeft(): Boolean {
        return leftParent.previous().previous() === rightParent
    }
}

private fun leftIsCloserThanRight(
    point: Point,
    leftParent: Node,
    rightParent: Node
): Boolean {
    return point.distanceToLine(leftParent.currentEdge()) < point
        .distanceToLine(rightParent.currentEdge())
}
