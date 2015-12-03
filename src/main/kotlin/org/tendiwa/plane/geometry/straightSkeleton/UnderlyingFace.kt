package org.tendiwa.plane.geometry.straightSkeleton

internal interface UnderlyingFace {
    val startHalfface: Chain

    var lastAddedChain: Chain

    fun isHalfface(chain: Chain): Boolean

    fun increaseNumberOfSkeletonNodes(d: Int)

    fun addNewSortedEnd(end: Node)

    fun forgetNodeProjection(node: Node)
}
