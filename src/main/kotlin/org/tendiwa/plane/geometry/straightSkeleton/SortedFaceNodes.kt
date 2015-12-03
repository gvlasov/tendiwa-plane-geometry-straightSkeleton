package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.points.Point
import java.util.*

internal fun SortedFaceNodes(edgeStart: Point, edgeEnd: Point): TreeSet<Node> =
    TreeSet(NodeComparator(edgeStart, edgeEnd))

private class NodeComparator(val edgeStart: Point, val edgeEnd: Point) :
    Comparator<Node> {
    override fun compare(o1: Node?, o2: Node?): Int {
        if (o1 == null || o2 == null) {
            throw NullPointerException()
        }
        if (o1 === o2) {
            return 0
        }
        if (o1.isPair(o2)) {
            val o1s = o1 as SplitNode
            val o2s = o2 as SplitNode
            assert(o1s.isLeft != o2s.isLeft)
            return if (o1s.isLeft) 1 else -1
        } else {
            val projection1 = projectionOnEdge(o1, edgeStart, edgeEnd)
            val projection2 = projectionOnEdge(o2, edgeStart, edgeEnd)
            assert(projection1 != projection2)
            return Math.signum(projection1 - projection2).toInt()
        }
    }

    private fun projectionOnEdge(
        node: Node,
        edgeStart: Point,
        edgeEnd: Point
    ): Double {
        //		return Vector2D
        //			.fromStartToEnd(edge.start, vertex)
        //			.dotProduct(edgeVector)
        //			/ edgeVector.magnitude()
        //			/ edgeVector.magnitude();
        val edx = edgeEnd.x - edgeStart.x
        val edy = edgeEnd.y - edgeStart.y
        // TODO: Extract this expression to a method
        return ((node.vertex.x - edgeStart.x) * edx + (node.vertex.y - edgeStart.y) * edy) / (edx * edx + edy * edy)
    }
}
