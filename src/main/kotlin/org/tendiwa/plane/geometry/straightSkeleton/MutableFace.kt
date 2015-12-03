package org.tendiwa.plane.geometry.straightSkeleton


internal interface MutableFace : Iterable<Node> {
    fun addLink(one: Node, another: Node)

    val isClosed: Boolean

    fun toPolygon(): StraightSkeletonFace

    fun getNodeFromLeft(leftNode: LeftSplitNode): Node

    fun getNodeFromRight(rightNode: RightSplitNode): Node
}
