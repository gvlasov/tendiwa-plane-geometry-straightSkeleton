package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton


import org.tendiwa.plane.geometry.points.isLeftOf
import org.tendiwa.plane.geometry.rays.RayIntersection
import org.tendiwa.plane.geometry.segments.Segment
import org.tenidwa.collections.utils.SuccessiveTuples
import java.util.*

/**
 * Apart from being a [Node], this class acts as an access point to an
 * original edge of a polygon emanating from this node.
 */
internal class OriginalEdgeStart(edge: Segment) : Node(edge.start) {
    /**
     * Face of the straight skeleton that has [Node.currentEdge] as one of
     * its segments. This field is variable because
     */
    private var mutableFace: MutableFace? = null

    init {
        currentEdge = edge
        currentEdgeStart = this
    }

    fun setPreviousInitial(node: OriginalEdgeStart) {
        previousEdgeStart = node
    }

    fun initFace() {
        this.mutableFace = IncompleteMutableFace(
            currentEdgeStart!!,
            currentEdgeStart!!.next() as OriginalEdgeStart)
    }


    internal override fun hasPair(): Boolean {
        return false
    }

    fun face(): MutableFace {
        return mutableFace!!
    }

    fun integrateSplitNodes(
        parent: Node,
        leftNode: LeftSplitNode,
        rightNode: RightSplitNode) {
        val leftLavNextNode: Node
        val rightLavPreviousNode: Node
        assert(!mutableFace!!.isClosed)
        leftLavNextNode = mutableFace!!.getNodeFromLeft(leftNode)
        rightLavPreviousNode = mutableFace!!.getNodeFromRight(rightNode)

        leftNode.setPreviousInLav(parent.previous())
        leftLavNextNode.setPreviousInLav(leftNode)

        rightNode.setPreviousInLav(rightLavPreviousNode)
        parent.next().setPreviousInLav(rightNode)

        parent.setProcessed()

        parent.growRightFace(rightNode)
        parent.growLeftFace(leftNode)
        mutableFace!!.addLink(leftNode, rightNode)
    }

    fun findAnotherOppositeEdgeStart(parent: Node): OriginalEdgeStart {
        var leftLavNextNode: Node
        var rightLavPreviousNode: Node
        val oppositeInClosed = findClosestIntersectedSegment(parent.bisector!!)
        var oneNode: Node? = null
        var anotherNode: Node? = null
        for (node in mutableFace!!) {
            if (node.vertex == oppositeInClosed.start || node.vertex == oppositeInClosed.end) {
                if (oneNode == null) {
                    oneNode = node
                } else {
                    assert(anotherNode == null)
                    anotherNode = node
                }
            }
        }
        assert(oneNode != null && anotherNode != null)
        if (oneNode!!.vertex.isLeftOf(parent.bisector!!)) {
            leftLavNextNode = oneNode
            rightLavPreviousNode = anotherNode!!
        } else {
            leftLavNextNode = anotherNode!!
            rightLavPreviousNode = oneNode
        }
        while (leftLavNextNode.isProcessed) {
            leftLavNextNode = leftLavNextNode.next()
        }
        while (rightLavPreviousNode.isProcessed) {
            rightLavPreviousNode = rightLavPreviousNode.previous()
        }
        return leftLavNextNode.previousEdgeStart!!
    }

    // TODO: This method is defined at a wrong level of abstraction
    private fun asSegmentStream(mutableFace: MutableFace): List<Segment> {
        val segments = ArrayList<Segment>()
        SuccessiveTuples.forEachLooped(mutableFace) { a, b ->
            if (a.vertex !== b.vertex) {
                segments.add(Segment(a.vertex, b.vertex))
            }
        }
        return segments
    }

    private fun findClosestIntersectedSegment(ray: Segment): Segment {
        assert(mutableFace!!.isClosed)
        return asSegmentStream(mutableFace!!)
            .filter {
                s ->
                val r = RayIntersection(s, ray).r
                r < 1.0 && r > 0.0
            }
            .minBy { segment -> RayIntersection(ray, segment).r }
            // TODO: Bad code style, but just !! at the end of the previous
            // line would be not as readable
            .run { this@run!! }
    }
}
