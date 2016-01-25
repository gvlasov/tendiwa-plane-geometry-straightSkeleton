package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point

internal abstract class SplitNode(
    point: Point,
    previousEdgeStart: OriginalEdgeStart,
    currentEdgeStart: OriginalEdgeStart
) : Node(point, previousEdgeStart, currentEdgeStart) {
    /**
     * Node at the same [Point] created as a result of
     * [SplitEvent]. Its [Node.pair] points to this node.
     * This field is not final just because nodes have to be added mutually, and
     * for that both nodes must already be constructed.
     */
    // TODO: Rename this to "neighbor" or something like this. Pair has
    // TODO: different meaning in Kotlin
    override var pair: SplitNode? = null
        set(pair) {
            assert(pair!!.vertex == vertex)
            field = pair
        }

    internal override fun hasPair(): Boolean =
        true

    override fun isPair(node: Node): Boolean =
        pair === node

    internal abstract val isLeft: Boolean
}
