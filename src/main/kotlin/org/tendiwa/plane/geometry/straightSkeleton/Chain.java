package org.tendiwa.plane.geometry.straightSkeleton;

import org.jetbrains.annotations.Nullable;
import org.tendiwa.collections.DoublyLinkedNode;


/**
 * Holds start and end of a subchain of a {@link IncompleteMutableFace}
 */
final class Chain {
    @Nullable
    Chain nextChain;
    @Nullable
    Chain previousChain;
    private DoublyLinkedNode<Node> first;
    private DoublyLinkedNode<Node> last;

    Chain(Node oneEnd, Node last, @Nullable Chain previousChain) {
        if (oneEnd == last) {
            this.first = new DoublyLinkedNode<>(oneEnd);
            this.last = this.first;
        } else {
            this.first = new DoublyLinkedNode<>(oneEnd);
            this.last = new DoublyLinkedNode<>(last);
            this.first.connectWithNext(this.last);
            this.last.connectWithPrevious(this.first);
        }

        this.previousChain = previousChain;
    }

    DoublyLinkedNode<Node> firstFaceNode() {
        return first;
    }

    DoublyLinkedNode<Node> lastFaceNode() {
        return last;
    }

    void setNextChain(@Nullable Chain nextChain) {
        this.nextChain = nextChain;
    }

    void moveFirstFaceNode(DoublyLinkedNode<Node> newFirst) {
        first = newFirst;
    }

    void moveLastFaceNode(DoublyLinkedNode<Node> newLast) {
        last = newLast;
    }

    void removeFromFace() {
        if (nextChain != null) {
            nextChain.previousChain = previousChain;
        }
        if (previousChain != null) {
            previousChain.setNextChain(nextChain);
        }
    }

    boolean isZeroLength() {
        return firstFaceNode().getPayload() == lastFaceNode().getPayload();
    }
}
