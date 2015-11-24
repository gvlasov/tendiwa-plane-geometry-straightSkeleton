package org.tendiwa.plane.geometry.straightSkeleton;


import org.tendiwa.geometry.points.Point;

abstract class SplitNode extends Node {
    /**
     * Node at the same {@link Point} created as a result of
     * {@link SplitEvent}. Its {@link #pair} points to this node.
     * This field is not final just because nodes have to be added mutually, and
     * for that both nodes must already be constructed.
     */
    private SplitNode pair;

    SplitNode(
        Point point,
        OriginalEdgeStart previousEdgeStart,
        OriginalEdgeStart currentEdgeStart
    ) {
        super(point, previousEdgeStart, currentEdgeStart);
    }

    @Override
    boolean hasPair() {
        return true;
    }

    @Override
    SplitNode getPair() {
        return pair;
    }

    void setPair(SplitNode pair) {
        assert pair.vertex.equals(vertex);
        this.pair = pair;
    }

    @Override
    public boolean isPair(Node node) {
        return pair == node;
    }

    abstract boolean isLeft();
}
