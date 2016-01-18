package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.collections.MutableDoublyLinkedNode


/**
 * Holds start and end of a subchain of a [IncompleteMutableFace]
 */
internal class Chain(oneEnd: Node, last: Node, var previousChain: Chain?) {
    var nextChain: Chain? = null
    private var first: MutableDoublyLinkedNode<Node>? = null
    private var last: MutableDoublyLinkedNode<Node>? = null

    init {
        if (oneEnd === last) {
            this.first = MutableDoublyLinkedNode(oneEnd)
            this.last = this.first
        } else {
            this.first = MutableDoublyLinkedNode(oneEnd)
            this.last = MutableDoublyLinkedNode(last)
            this.first!!.connectWithNext(this.last!!)
            this.last!!.connectWithPrevious(this.first!!)
        }
    }

    fun firstFaceNode(): MutableDoublyLinkedNode<Node> {
        return first!!
    }

    fun lastFaceNode(): MutableDoublyLinkedNode<Node> {
        return last!!
    }

    fun moveFirstFaceNode(newFirst: MutableDoublyLinkedNode<Node>) {
        first = newFirst
    }

    fun moveLastFaceNode(newLast: MutableDoublyLinkedNode<Node>) {
        last = newLast
    }

    fun removeFromFace() {
        if (this.nextChain != null) {
            nextChain!!.previousChain = this.previousChain
        }
        if (previousChain != null) {
            previousChain!!.nextChain = this.nextChain
        }
    }

    val isZeroLength: Boolean
        get() = firstFaceNode().payload === lastFaceNode().payload
}
