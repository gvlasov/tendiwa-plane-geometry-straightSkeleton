package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.collections.DoublyLinkedNode


/**
 * Holds start and end of a subchain of a [IncompleteMutableFace]
 */
internal class Chain(oneEnd: Node, last: Node, var previousChain: Chain?) {
    var nextChain: Chain? = null
    private var first: DoublyLinkedNode<Node>? = null
    private var last: DoublyLinkedNode<Node>? = null

    init {
        if (oneEnd === last) {
            this.first = DoublyLinkedNode(oneEnd)
            this.last = this.first
        } else {
            this.first = DoublyLinkedNode(oneEnd)
            this.last = DoublyLinkedNode(last)
            this.first!!.connectWithNext(this.last!!)
            this.last!!.connectWithPrevious(this.first!!)
        }
    }

    fun firstFaceNode(): DoublyLinkedNode<Node> {
        return first!!
    }

    fun lastFaceNode(): DoublyLinkedNode<Node> {
        return last!!
    }

    fun moveFirstFaceNode(newFirst: DoublyLinkedNode<Node>) {
        first = newFirst
    }

    fun moveLastFaceNode(newLast: DoublyLinkedNode<Node>) {
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
