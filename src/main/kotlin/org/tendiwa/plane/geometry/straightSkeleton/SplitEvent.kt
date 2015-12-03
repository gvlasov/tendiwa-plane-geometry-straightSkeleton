package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.algorithms.intersections.RayIntersection
import org.tendiwa.geometry.points.Point

/**
 * Note: this class has natural ordering that is inconsistent with
 * [Object.equals].
 */
internal class SplitEvent(
    point: Point,
    private val parent: Node,
    private val oppositeEdgeStart: OriginalEdgeStart) : SkeletonEvent(point, parent) {

    fun parent(): Node {
        return parent
    }

    internal override fun handle(skeleton: StraightSkeleton) {
        if (parent().isProcessed) {
            return
        }
        if (parent().previous().previous().previous() === parent()) {
            // Non-convex 2c
            //				connectLast3SegmentsOfLav(point);
            assert(false)
        } else if (parent().isInLavOf2Nodes) {
            eliminate2NodeLav(parent(), skeleton)
        } else if (oppositeEdgeStart.face().isClosed) {
            replaceWithEventOverClosedFace(skeleton)
        } else {
            splitEdge(skeleton)
        }
    }

    private fun wedgesIntoOppositeFace(
        left: LeftSplitNode,
        right: RightSplitNode): Boolean {
        // TODO: Extract these two expressions into methods
        val leftBisectorFromLeft =
            RayIntersection(
                left.bisector!!,
                oppositeEdgeStart.face().getNodeFromLeft(left).bisector!!
            ).r > 0
        val rightBisectorFromRight =
            RayIntersection(
                right.bisector!!,
                oppositeEdgeStart.face().getNodeFromRight(right).bisector!!
            ).r > 0
        return rightBisectorFromRight && leftBisectorFromLeft
    }

    private fun replaceWithEventOverClosedFace(skeleton: StraightSkeleton) {
        skeleton.queueEvent(
            SplitEvent(
                point,
                parent,
                oppositeEdgeStart.findAnotherOppositeEdgeStart(parent)))
    }

    private fun splitEdge(skeleton: StraightSkeleton) {
        // Non-convex 2e

        // Split event produces two nodes at the same point, and those two nodes have distinct LAVs.
        val leftNode = LeftSplitNode(
            point,
            parent().previousEdgeStart!!,
            oppositeEdgeStart
        )
        val rightNode = RightSplitNode(
            point,
            oppositeEdgeStart,
            parent().currentEdgeStart!!
        )
        leftNode.pair = rightNode
        rightNode.pair = leftNode
        leftNode.computeReflexAndBisector()
        rightNode.computeReflexAndBisector()
        if (!wedgesIntoOppositeFace(leftNode, rightNode)) {
            return
        }
        // Non-convex 2d
        skeleton.outputArc(parent().vertex, point)
        skeleton.debug.drawSplitEventArc(this)

        oppositeEdgeStart.integrateSplitNodes(parent(), leftNode, rightNode)

        // Non-convex 2
        integrateNewSplitNode(leftNode, skeleton)
        integrateNewSplitNode(rightNode, skeleton)
    }

    private fun integrateNewSplitNode(node: Node, skeleton: StraightSkeleton) {
        if (node.isInLavOf2Nodes) {
            // Such lavs can form after a split event
            eliminate2NodeLav(node, skeleton)
        } else {
            //			node.computeReflexAndBisector();
            skeleton.queueEventFromNode(node)
        }
    }
}
