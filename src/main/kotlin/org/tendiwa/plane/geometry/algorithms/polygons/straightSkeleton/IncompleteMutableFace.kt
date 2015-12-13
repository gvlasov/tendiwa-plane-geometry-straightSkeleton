package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point
import java.util.*

internal class IncompleteMutableFace(
    private val edgeStart: OriginalEdgeStart,
    private val edgeEnd: OriginalEdgeStart
) : MutableFace, UnderlyingFace {
    override val startHalfface: Chain = Chain(edgeStart, edgeStart, null)
    //@Nonnull
    val endHalfface: Chain = Chain(edgeEnd, edgeEnd, startHalfface)
    private val sortedLinkEnds: TreeSet<Node> = SortedFaceNodes(edgeStart.vertex, edgeEnd.vertex)
    override var lastAddedChain: Chain = endHalfface
    // Initially there are two skeleton nodes: startHalfface start and
    // endHalfface end.
    private var numberOfSkeletonNodes: Int = 2

    init {
        startHalfface.nextChain = endHalfface
        assert(edgeStart.next() === edgeEnd)
        assert(
            startHalfface.nextChain == endHalfface
                && endHalfface.previousChain == startHalfface
                && startHalfface.previousChain == null
                && endHalfface.nextChain == null
        )
    }

    override fun getNodeFromLeft(leftNode: LeftSplitNode): Node {
        var higher: Node? = sortedLinkEnds.higher(leftNode)
        if (higher == null) {
            assert(!endHalfface.lastFaceNode().payload.isProcessed)
            higher = endHalfface.lastFaceNode().payload
        }
        assert(!higher.isProcessed)
        return higher
    }

    override fun getNodeFromRight(rightNode: RightSplitNode): Node {
        var lower: Node? = sortedLinkEnds.lower(rightNode)
        if (lower == null) {
            assert(!startHalfface.lastFaceNode().payload.isProcessed)
            lower = startHalfface.lastFaceNode().payload
        }
        assert(!lower.isProcessed)
        return lower
    }

    /**
     * @param one Order doesn't matter.
     * @param another Order doesn't matter.
     */
    override fun addLink(one: Node, another: Node) {
        FaceConstructionStep(this, one, another).run()
    }

    override fun forgetNodeProjection(node: Node) {
        sortedLinkEnds.remove(node)
    }

    override fun addNewSortedEnd(end: Node) {
        sortedLinkEnds.add(end)
        assert(
            end.vertex != startHalfface.firstFaceNode().payload.vertex
                && end.vertex != endHalfface.firstFaceNode().payload.vertex
        )
    }

    override fun isHalfface(chain: Chain): Boolean {
        return chain == startHalfface || chain == endHalfface
    }

    override fun increaseNumberOfSkeletonNodes(d: Int) {
        assert(d > 0)
        numberOfSkeletonNodes += d
    }

    override fun toPolygon(): StraightSkeletonFace {
        val points = ArrayList<Point>(numberOfSkeletonNodes)
        val seed = startHalfface.firstFaceNode()
        assert(!seed.hasBothNeighbors())
        var previousPayload: Point? = null
        for (node in seed) {
            if (node.vertex === previousPayload) {
                // This happens at split event points and at starts of half-faces
                continue
            }
            if (!(points.size == 0 || node.vertex != points[points.size - 1])) {
                assert(false)
            }
            points.add(node.vertex)
            previousPayload = node.vertex
        }
        return StraightSkeletonFace(points)
    }

    override val isClosed: Boolean
        get() = startHalfface.lastFaceNode() == endHalfface.firstFaceNode() || endHalfface.lastFaceNode() == startHalfface.firstFaceNode()

    override fun iterator(): Iterator<Node> {
        assert(isClosed)
        return if (startHalfface.firstFaceNode() == startHalfface.lastFaceNode()) {
            startHalfface.firstFaceNode().iterator()
        } else {
            endHalfface.firstFaceNode().iterator()
        }
    }
}
