package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.collections.DoublyLinkedNode


internal class FaceConstructionStep(
    private val face: UnderlyingFace,
    private val end1: Node,
    private val end2: Node
) {
    private val chainAtEnd1: Chain?
    private val chainAtEnd2: Chain?
    private val linkAtEnd1First: Boolean
    private val linkAtEnd2First: Boolean

    init {
        var chain: Chain? = face.startHalfface
        var chainAtEnd1: Chain? = null
        var chainAtEnd2: Chain? = null
        var linkAtEnd1First = false
        var linkAtEnd2First = false
        while (chain != null) {
            if (chainAtEnd1 == null) {
                if (chain.firstFaceNode().payload === end1 && !chain.isZeroLength) {
                    chainAtEnd1 = chain
                    linkAtEnd1First = true
                } else if (chain.lastFaceNode().payload === end1) {
                    chainAtEnd1 = chain
                    linkAtEnd1First = false
                }
            }
            if (chainAtEnd2 == null) {
                if (chain.firstFaceNode().payload === end2 && !chain.isZeroLength) {
                    chainAtEnd2 = chain
                    linkAtEnd2First = true
                } else if (chain.lastFaceNode().payload === end2) {
                    chainAtEnd2 = chain
                    linkAtEnd2First = false
                }
            }
            chain = chain.nextChain
        }
        this.chainAtEnd1 = chainAtEnd1
        this.chainAtEnd2 = chainAtEnd2
        this.linkAtEnd1First = linkAtEnd1First
        this.linkAtEnd2First = linkAtEnd2First
    }

    fun run() {
        val hasLinkAt1 = chainAtEnd1 != null
        val hasLinkAt2 = chainAtEnd2 != null
        if (hasLinkAt1 && hasLinkAt2) {
            uniteChainsWithAddedEdge()
        } else if (hasLinkAt1) {
            prolongChainAtEnd1(end2)
        } else if (hasLinkAt2) {
            prolongChainAtEnd2(end1)
        } else {
            // TODO: This branch only runs when it is a split event, maybe extract it to a separate method?
            createNewLink(end1, end2)
        }
        if ((!(hasLinkAt1 && hasLinkAt2)) && !(face.startHalfface.firstFaceNode().next == null || face.startHalfface.firstFaceNode().previous == null)) {
            assert(false)
        }
    }

    private fun createNewLink(oneEnd: Node, anotherEnd: Node) {
        assert(face.lastAddedChain != null)
        assert(oneEnd.vertex === anotherEnd.vertex)
        face.lastAddedChain = Chain(oneEnd, anotherEnd, face.lastAddedChain)
        assert(face.lastAddedChain.previousChain != null)
        face.lastAddedChain.previousChain!!.nextChain = face.lastAddedChain
        face.addNewSortedEnd(oneEnd)
        //		sortedLinkEnds.add(oneEnd);
        face.addNewSortedEnd(anotherEnd)
        //		sortedLinkEnds.add(anotherEnd);
        face.increaseNumberOfSkeletonNodes(2)
    }


    private fun prolongChainAtEnd1(end2: Node) {
        prolongLink(end2, chainAtEnd1!!, linkAtEnd1First)
    }

    private fun prolongChainAtEnd2(end1: Node) {
        prolongLink(end1, chainAtEnd2!!, linkAtEnd2First)
    }

    private fun uniteChainsWithAddedEdge() {
        assert(chainAtEnd1 != null && chainAtEnd2 != null)
        // We grow chain at end 1,
        // but if chain at end 2 is the initial left or right half-face,
        // then we grow it instead.
        // Of course there is a case when the chain at end 1 is the left (right) half-face,
        // and the chain at end 2 is the another, right (left) half-face. In that case order doesn't matter.
        val shouldBeSwapped = face.isHalfface(chainAtEnd2!!)
        var chainAtEnd1 = this.chainAtEnd1
        var chainAtEnd2 = this.chainAtEnd2
        var linkAtEnd1First = this.linkAtEnd1First
        var linkAtEnd2First = this.linkAtEnd2First
        if (shouldBeSwapped && chainAtEnd1 != face.startHalfface) {
            // Swap
            val chainBuf = chainAtEnd1
            chainAtEnd1 = chainAtEnd2
            chainAtEnd2 = chainBuf
            // Swap
            val firstBuf = linkAtEnd1First
            linkAtEnd1First = linkAtEnd2First
            linkAtEnd2First = firstBuf
        }

        if (face.isHalfface(chainAtEnd1!!)) {
            face.forgetNodeProjection(if (linkAtEnd2First)
                chainAtEnd2!!.lastFaceNode().payload
            else
                chainAtEnd2!!.firstFaceNode().payload)
        }
        face.forgetNodeProjection(if (linkAtEnd1First) chainAtEnd1!!.firstFaceNode().payload else chainAtEnd1!!.lastFaceNode().payload)
        face.forgetNodeProjection(if (linkAtEnd2First) chainAtEnd2!!.firstFaceNode().payload else chainAtEnd2!!.lastFaceNode().payload)

        assert(chainAtEnd1 != null && chainAtEnd2 != null)
        if (linkAtEnd1First && linkAtEnd2First) {
            val first1 = chainAtEnd1.firstFaceNode()
            val oldFirst2 = chainAtEnd2.firstFaceNode()
            oldFirst2.revertChain()
            first1.connectWithPrevious(oldFirst2)
            oldFirst2.connectWithNext(first1)
            chainAtEnd1.moveFirstFaceNode(chainAtEnd2.lastFaceNode())
        } else if (linkAtEnd1First) {
            assert(!linkAtEnd2First)
            val first1 = chainAtEnd1.firstFaceNode()
            val last2 = chainAtEnd2.lastFaceNode()
            first1.connectWithPrevious(last2)
            last2.connectWithNext(first1)
            chainAtEnd1.moveFirstFaceNode(chainAtEnd2.firstFaceNode())
        } else if (linkAtEnd2First) {
            assert(!linkAtEnd1First)
            val last1 = chainAtEnd1.lastFaceNode()
            val first2 = chainAtEnd2.firstFaceNode()
            last1.connectWithNext(first2)
            first2.connectWithPrevious(last1)
            chainAtEnd1.moveLastFaceNode(chainAtEnd2.lastFaceNode())
        } else {
            assert(!linkAtEnd1First && !linkAtEnd2First)
            val last1 = chainAtEnd1.lastFaceNode()
            val oldLast2 = chainAtEnd2.lastFaceNode()
            oldLast2.revertChain()
            last1.connectWithNext(oldLast2)
            oldLast2.connectWithPrevious(last1)
            chainAtEnd1.moveLastFaceNode(chainAtEnd2.firstFaceNode())
        }
        chainAtEnd2.removeFromFace()
        if (face.lastAddedChain == chainAtEnd2) {
            face.lastAddedChain = chainAtEnd2.previousChain!!
        }
    }

    private fun prolongLink(end: Node, chain: Chain, isFirst: Boolean) {
        face.increaseNumberOfSkeletonNodes(1)
        if (isFirst) {
            face.forgetNodeProjection(chain.firstFaceNode().payload)
            val newFirst = DoublyLinkedNode(end)
            val first = chain.firstFaceNode()
            first.connectWithPrevious(newFirst)
            newFirst.connectWithNext(first)
            chain.moveFirstFaceNode(newFirst)
        } else {
            face.forgetNodeProjection(chain.lastFaceNode().payload)
            val newLast = DoublyLinkedNode(end)
            val last = chain.lastFaceNode()
            last.connectWithNext(newLast)
            newLast.connectWithPrevious(last)
            chain.moveLastFaceNode(newLast)
        }
        if (!face.isHalfface(chain)) {
            face.addNewSortedEnd(end)
        }
    }
}
