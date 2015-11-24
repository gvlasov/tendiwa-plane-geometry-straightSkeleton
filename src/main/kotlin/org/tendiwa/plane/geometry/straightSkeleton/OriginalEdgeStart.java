package org.tendiwa.plane.geometry.straightSkeleton;


import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.tendiwa.geometry.algorithms.intersections.RayIntersection;
import org.tendiwa.geometry.algorithms.predicates.PointPositionsKt;
import org.tendiwa.geometry.segments.Segment;
import org.tenidwa.collections.utils.SuccessiveTuples;

/**
 * Apart from being a {@link Node}, this class acts as an access point to an
 * original edge of a polygon emanating from this node.
 */
final class OriginalEdgeStart extends Node {
    private MutableFace mutableFace;

    OriginalEdgeStart(Segment edge) {
        super(edge.getStart());
        currentEdge = edge;
        currentEdgeStart = this;
    }

    void setPreviousInitial(OriginalEdgeStart node) {
        previousEdgeStart = node;
    }

    void initFace() {
        this.mutableFace = new IncompleteMutableFace(
            currentEdgeStart,
            (OriginalEdgeStart) currentEdgeStart.next()
        );
    }


    @Override
    boolean hasPair() {
        return false;
    }

    MutableFace face() {
        return mutableFace;
    }

    void integrateSplitNodes(
        Node parent,
        LeftSplitNode leftNode,
        RightSplitNode rightNode
    ) {
        Node leftLavNextNode, rightLavPreviousNode;
        assert !mutableFace.isClosed();
        leftLavNextNode = mutableFace.getNodeFromLeft(leftNode);
        rightLavPreviousNode = mutableFace.getNodeFromRight(rightNode);

        leftNode.setPreviousInLav(parent.previous());
        leftLavNextNode.setPreviousInLav(leftNode);

        rightNode.setPreviousInLav(rightLavPreviousNode);
        parent.next().setPreviousInLav(rightNode);

        parent.setProcessed();

        parent.growRightFace(rightNode);
        parent.growLeftFace(leftNode);
        mutableFace.addLink(leftNode, rightNode);
    }

    OriginalEdgeStart findAnotherOppositeEdgeStart(Node parent) {
        Node leftLavNextNode, rightLavPreviousNode;
        Segment oppositeInClosed = findClosestIntersectedSegment(parent.bisector);
        Node oneNode = null, anotherNode = null;
        for (Node node : mutableFace) {
            if (node.vertex.equals(oppositeInClosed.getStart())
                || node.vertex.equals(oppositeInClosed.getEnd())) {
                if (oneNode == null) {
                    oneNode = node;
                } else {
                    assert anotherNode == null;
                    anotherNode = node;
                }
            }
        }
        assert oneNode != null && anotherNode != null;
        if (PointPositionsKt.isLeftOf(oneNode.vertex, parent.bisector)) {
            leftLavNextNode = oneNode;
            rightLavPreviousNode = anotherNode;
        } else {
            leftLavNextNode = anotherNode;
            rightLavPreviousNode = oneNode;
        }
        while (leftLavNextNode.isProcessed()) {
            leftLavNextNode = leftLavNextNode.next();
        }
        while (rightLavPreviousNode.isProcessed()) {
            rightLavPreviousNode = rightLavPreviousNode.previous();
        }
        return leftLavNextNode.previousEdgeStart;
    }

    private Stream<Segment> asSegmentStream(MutableFace mutableFace) {
        Collection<Segment> segments = new ArrayList<>();
        SuccessiveTuples.forEachLooped(mutableFace, (a, b) -> {
            if (a.vertex != b.vertex) {
                segments.add(new Segment(a.vertex, b.vertex));
            }
        });
        return segments.stream();
    }

    private Segment findClosestIntersectedSegment(Segment ray) {
        assert mutableFace.isClosed();
        return asSegmentStream(mutableFace)
            .filter(s -> {
                double r = new RayIntersection(s, ray).getR();
                return r < 1. && r > 0.;
            })
            .min(
                (a, b) ->
                    (int) Math.signum(
                        new RayIntersection(ray, a).getR() -
                            new RayIntersection(ray, b).getR()
                    )
            )
            .get();
    }
}
