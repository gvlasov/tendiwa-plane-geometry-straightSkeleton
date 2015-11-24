package org.tendiwa.plane.geometry.straightSkeleton;

import com.google.common.collect.Iterators;
import com.sun.istack.internal.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.tendiwa.canvas.algorithms.geometry.DrawingArrowKt;
import org.tendiwa.geometry.algorithms.intersections.RayIntersection;
import org.tendiwa.geometry.circles.Circle;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.points.PointPropertiesKt;
import org.tendiwa.geometry.points.PointTransformationsKt;
import org.tendiwa.geometry.segments.Segment;
import org.tendiwa.geometry.segments.SegmentOperationsKt;
import org.tendiwa.geometry.segments.SegmentPropertiesKt;
import org.tendiwa.geometry.vectors.Vector;
import org.tendiwa.geometry.vectors.VectorOperationsKt;
import org.tendiwa.geometry.vectors.VectorPropertiesKt;
import org.tendiwa.math.constants.ConstantsKt;
import org.tenidwa.collections.utils.SuccessiveTuples;

/**
 * A node in a circular list of active vertices.
 */
abstract class Node implements Iterable<Node> {
    Segment bisector;
    private boolean isProcessed = false; // As said in 1a in [Obdrzalek 1998, paragraph 2.1]
    boolean isReflex;
    protected Node next;
    private Node previous;
    /**
     * Along with {@link #previousEdgeStart}, determines the direction of {@link #bisector} as well as two faces that
     * this Node divides.
     */
    protected OriginalEdgeStart currentEdgeStart;
    protected OriginalEdgeStart previousEdgeStart;
    protected Segment currentEdge;
    final Point vertex;

    Node(
        Point point,
        OriginalEdgeStart previousEdgeStart,
        OriginalEdgeStart currentEdgeStart
    ) {
        this(point);
        if (previousEdgeStart == currentEdgeStart) {
            assert false;
        }
        currentEdge = currentEdgeStart.currentEdge;
        this.currentEdgeStart = currentEdgeStart;
        this.previousEdgeStart = previousEdgeStart;
        if (previousEdge().equals(currentEdge())) {
            assert false;
        }
        // TODO: Lol, dafuq! Previous is parallel to previous?
        assert !(previousEdge().equals(currentEdge())
            && SegmentOperationsKt.isParallel(previousEdge(), previousEdge()));
    }

    /**
     * Adds {@code newNode} to faces at {@link #currentEdgeStart} and {@link #previousEdgeStart} <i>if</i> it is
     * necessary.
     */
    void growAdjacentFaces(Node newNode) {
        growLeftFace(newNode);
        growRightFace(newNode);
    }

    void growRightFace(Node newNode) {
        growFace(newNode, currentEdgeStart);
    }

    void growLeftFace(Node newNode) {
        growFace(newNode, previousEdgeStart);
    }

    private void growFace(Node newNode, OriginalEdgeStart faceStart) {
        faceStart.face().addLink(this, newNode);
    }

    protected Node(Point vertex) {
        this.vertex = vertex;
    }

    public void drawLav() {
        SuccessiveTuples.forEachLooped(
            this,
            (a, b) -> {
                DrawingArrowKt.drawArrow(
                    Debug.canvas,
                    new Segment(a.vertex, b.vertex),
                    Color.cyan,
                    1
                );
            }
        );
    }

    abstract boolean hasPair();

    SplitNode getPair() {
        throw new RuntimeException(this.getClass().getName() + " can't have a pair; only SplitNode can");
    }

    Node next() {
        assert next != null;
        return next;
    }

    Node previous() {
        assert previous != null;
        return previous;
    }

    Segment previousEdge() {
        return previousEdgeStart.currentEdge;
    }

    public Segment currentEdge() {
        return currentEdgeStart.currentEdge;
    }

    /**
     * Remembers that this point is processed, that is, it is not a part of some LAV anymore.
     */
    void setProcessed() {
        assert !isProcessed;
        isProcessed = true;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void computeReflexAndBisector() {
        assert bisector == null;
        isReflex = this instanceof OriginalEdgeStart && isReflex(
            previous.vertex,
            vertex,
            vertex,
            next.vertex
        );
        VectorSector bisector1 = new VectorSector(

            SegmentPropertiesKt.getVector(
                currentEdgeStart.currentEdge
            ),
            VectorOperationsKt.unaryMinus(
                SegmentPropertiesKt.getVector(previousEdgeStart.currentEdge)
            )
        );
        Vector bisectorVector = isReflex
            ? bisector1.getBisector()
            : bisector1.getSumVector();
        bisector = new Segment(
            vertex,
            VectorPropertiesKt.getPoint(
                VectorOperationsKt.plus(
                    PointPropertiesKt.getRadiusVector(
                        vertex
                    ),
                    bisectorVector
                )
            )
        );
//		if (bisector.start.distanceTo(new Point(416, 384)) < 12) {
//			TestCanvas.canvas.draw(bisector, DrawingSegment.withColorDirected(Color.green, 1));
//		}
    }

    /**
     * Finds if two edges going counter-clockwise make a convex or a reflex angle.
     * @param a1 Start of the first edge.
     * @param a2 End of the first edge.
     * @param b1 Start of the second edge.
     * @param b2 End of the second edge.
     * @return True if the angle to the left between two edges > Math.PI (reflex), false otherwise (convex).
     */
    private boolean isReflex(Point a1, Point a2, Point b1, Point b2) {
        return perpDotProduct(
            new double[]{a2.getX() - a1.getX(), a2.getY() - a1.getY()},
            new double[]{b2.getX() - b1.getX(), b2.getY() - b1.getY()}
        ) > 0;
    }


    public void setPreviousInLav(Node previous) {
        assert previous != this;
        assert previous != null;
        this.previous = previous;
        previous.next = this;
    }

    public boolean isInLavOf2Nodes() {
        return next.next == this;
    }

    /**
     * Iterates over the current LAV of the node. All the nodes iterated upon are non-processed.
     */
    @Override
    public Iterator<Node> iterator() {
        if (isProcessed) {
            assert false;
        }
        return new Iterator<Node>() {
            List<Point> points = new ArrayList<>(100);
            Node start = Node.this;
            Node node = Node.this.previous;
            int i = 0;

            @Override
            public boolean hasNext() {
                return node.next != start || i == 0;
            }

            @Override
            public Node next() {
                node = node.next;
                if (node.isProcessed()) {
                    showCurrentLav();
                    throw new RuntimeException("Node not in lav");
                }
                checkLavCorrectness();
                points.add(node.vertex);
                return node;
            }

            private void showCurrentLav() {
                Node current = start;
                do {
                    DrawingArrowKt.drawArrow(
                        Debug.canvas,
                        new Segment(current.vertex, current.next.vertex),
                        Color.cyan,
                        0.5
                    );
                    current = current.next;
                } while (current != node);
                assert Boolean.TRUE;
            }

            private void checkLavCorrectness() {
                if (++i > 1000) {
                    drawLav();
                    throw new RuntimeException("Too many iterations");
                }
            }

            private void drawLav() {
                Iterator<Color> colors = Iterators.cycle(Color.darkGray, Color.gray, Color.lightGray, Color.white);
                for (int i = 0; i < points.size() - 1; i++) {
                    Debug.canvas.draw(
                        new Segment(points.get(i), points.get(i + 1)),
                        colors.next()
                    );
                }
                Debug.canvas.draw(
                    new Circle(start.vertex, 2),
                    Color.yellow
                );
            }
        };
    }

    /**
     * A usual node is never a pair for some other node. Only a
     * {@link SplitNode} may be a pair to another {@link SplitNode}.
     * @param node Another node.
     * @return true if this node and {@code node} were created by the same
     * {@link org.tendiwa.plane.geometry.straightSkeleton.SplitEvent}, false
     * otherwise.
     */
    public boolean isPair(Node node) {
        return false;
    }

    @Nullable
    protected SkeletonEvent computeNearerBisectorsIntersection() {
        // Non-convex 1c
        RayIntersection nextIntersection = bisectorsIntersection(next());
        EdgeEvent sameLineIntersection = trySameLineIntersection(nextIntersection, this, next());
        if (sameLineIntersection != null) {
            return sameLineIntersection;
        }

        RayIntersection previousIntersection = bisectorsIntersection(previous());
        sameLineIntersection = trySameLineIntersection(previousIntersection, this, previous());
        if (sameLineIntersection != null) {
            return sameLineIntersection;
        }

        Point shrinkPoint = null;
        Node va = null;
        Node vb = null;
        if (nextIntersection.getR() > 0 || previousIntersection.getR() > 0) {
            if (previousIntersection.getR() < 0 && nextIntersection.getR() >
                0 || nextIntersection.getR() > 0 && nextIntersection.getR()
                <= previousIntersection.getR()) {
                if (next().bisectorsIntersection(this).getR() > 0 && nextIntersection.getR() > 0) {
                    shrinkPoint = nextIntersection.commonPoint();
                    va = this;
                    vb = next();
                }
            } else if (
                nextIntersection.getR() < 0 && previousIntersection.getR() > 0
                    || previousIntersection.getR() > 0 && previousIntersection.getR() <= nextIntersection.getR()) {
                if (previous().bisectorsIntersection(this).getR() > 0 && previousIntersection.getR() > 0) {
                    shrinkPoint = previousIntersection.commonPoint();
                    va = previous();
                    vb = this;
                }
            }
        }
        if (isReflex) {
            SkeletonEvent splitEvent = findSplitEvent();
            if (splitPointIsBetterThanShrinkPoint(splitEvent, shrinkPoint)) {
                return splitEvent;
            }
        }
        assert shrinkPoint == null || va != null && vb != null;
        assert va == null && vb == null || va.next() == vb;
        if (shrinkPoint == null) {
            return null;
        }
        return new EdgeEvent(shrinkPoint, va, vb);
    }

    private boolean splitPointIsBetterThanShrinkPoint(
        @Nullable SkeletonEvent splitEvent,
        @Nullable Point shrinkPoint
    ) {
        if (splitEvent == null) {
            return false;
        } else if (shrinkPoint == null) {
            return true;
        }
        return PointTransformationsKt.distanceTo(vertex, splitEvent.point) <
            PointTransformationsKt.distanceTo(vertex, shrinkPoint);
    }

    private RayIntersection bisectorsIntersection(Node node) {
        return new RayIntersection(bisector, node.bisector);
    }

    @Nullable
    private static EdgeEvent trySameLineIntersection(
        RayIntersection intersection,
        Node current,
        Node target
    ) {
        if (Double.isInfinite(intersection.getR())) {
            return new EdgeEvent(
                new Point(
                    (target.vertex.getX() + current.vertex.getX()) / 2,
                    (target.next().vertex.getY() + current.vertex.getY()) / 2
                ),
                current,
                target
            );
        }
        return null;
    }

    /**
     * [Obdrzalek 1998, paragraph 2.2, figure 4]
     * <p>
     * Computes the point where a split event occurs.
     * @return The point where split event occurs, or null if there is no split event emanated from {@code reflexNode}.
     */
    @Nullable
    private SplitEvent findSplitEvent() {
        assert isReflex;
        Point splitPoint = null;
        Node originalEdgeStart = null;
        for (Node node : this) {
            if (nodeIsAppropriate(node)) {
                Point point = computeSplitPoint(node.currentEdge());
                if (node.isPointInAreaBetweenEdgeAndItsBisectors(point)) {
                    if (newSplitPointIsBetter(splitPoint, point)) {
                        splitPoint = point;
                        originalEdgeStart = node;
                    }
                }
            }
        }
        if (splitPoint == null) {
            return null;
        }
        return new SplitEvent(
            splitPoint,
            this,
            originalEdgeStart.currentEdgeStart
        );
    }

    /**
     * [Obdrzalek 1998, paragraph 2.2, Figure 4]
     * <p>
     * Computes point B_i.
     * @param oppositeEdge The tested line segment.
     * @return Intersection between the bisector at {@code currentNode} and the axis of the angle between one of the
     * edges starting at {@code currentNode} and the tested line segment {@code oppositeEdge}.
     */
    private Point computeSplitPoint(Segment oppositeEdge) {
        assert isReflex;
        Point bisectorStart = new RayIntersection(
            SegmentOperationsKt.isParallel(
                previousEdge(),
                oppositeEdge
            )
                ? currentEdge()
                : previousEdge(),
            oppositeEdge
        ).commonPoint();
        Vector cw =
            VectorOperationsKt.minus(
                PointPropertiesKt.getRadiusVector(
                    new RayIntersection(
                        bisector,
                        oppositeEdge
                    ).commonPoint()
                ),
                PointPropertiesKt.getRadiusVector(bisectorStart)
            );
        Vector ccw =
            VectorOperationsKt.minus(
                PointPropertiesKt.getRadiusVector(vertex),
                PointPropertiesKt.getRadiusVector(bisectorStart)
            );
        VectorSector anotherBisector = new VectorSector(cw, ccw);
        RayIntersection intersection = new RayIntersection(
            new Segment(
                bisectorStart,
                VectorPropertiesKt.getPoint(
                    VectorOperationsKt.plus(
                        PointPropertiesKt.getRadiusVector(
                            bisectorStart
                        ),
                        anotherBisector.getSumVector()
                    )
                )
            ),
            bisector
        );
        return intersection.commonPoint();
    }

    private boolean nodeIsAppropriate(Node node) {
        return !(nodeIsNeighbor(node)
            || intersectionIsBehindReflexNode(node)
            || previousEdgeIntersectsInFrontOfOppositeEdge(node)
            // TODO: If the previous condition is unnecessary, then this condition is unnecessary too.
            || currentEdgeIntersectsInFrontOfOppositeEdge(node)
        );
    }

    private boolean newSplitPointIsBetter(
        Point oldSplitPoint,
        Point newSplitPoint
    ) {
        return oldSplitPoint == null
            || PointTransformationsKt.distanceTo(vertex, oldSplitPoint) >
            PointTransformationsKt.distanceTo( vertex, newSplitPoint );

    }

    private boolean nodeIsNeighbor(Node node) {
        return node == this || node == previous() || node == next();
    }

    private boolean currentEdgeIntersectsInFrontOfOppositeEdge(Node oppositeEdgeStartCandidate) {
        return new RayIntersection(
            SegmentPropertiesKt.getReverse(currentEdge),
            oppositeEdgeStartCandidate.currentEdge
        ).getR() <= 1;
    }

    private boolean previousEdgeIntersectsInFrontOfOppositeEdge(Node oppositeEdgeStartCandidate) {
        return new RayIntersection(
            previousEdge(),
            oppositeEdgeStartCandidate.currentEdge
        ).getR() <= 1;
    }

    private boolean intersectionIsBehindReflexNode(Node anotherRay) {
        return new RayIntersection(
            bisector,
            anotherRay.currentEdge
        ).getR() <= ConstantsKt.getEPSILON();
    }

    /**
     * [Obdrzalek 1998, paragraph 2.2, Figure 4]
     * <p>
     * Checks if a point (namely point B coming from a reflex vertex) is located in an area bounded by an edge and
     * bisectors coming from start and end nodes of this edge.
     * @param point The point to test.
     * @return true if the point is located within the area marked by an edge and edge's bisectors, false otherwise.
     */
    private boolean isPointInAreaBetweenEdgeAndItsBisectors(Point point) {
        Point a = bisector.getEnd();
        Point b = this.currentEdge.getStart();
        Point c = this.currentEdge.getEnd();
        Point d = next().bisector.getEnd();
        return isPointNonConvex(a, point, b) && isPointNonConvex(b, point, c) && isPointNonConvex(c, point, d);
    }

    /**
     * Given 3 counter-clockwise points of a polygon, check if the middle one is convex or reflex.
     * @param previous Beginning of vector 1.
     * @param point End of vector 1 and beginning of vector 2.
     * @param next End of vector 2.
     * @return true if {@code point} is non-convex, false if it is convex.
     */
    private static boolean isPointNonConvex(
        Point previous,
        Point point,
        Point next
    ) {
        //  TODO: There is similar method isReflex; remove this method.
        return perpDotProduct(
            new double[]{point.getX() - previous.getX(), point.getY() - previous.getY()},
            new double[]{next.getX() - point.getX(), next.getY() - point.getY()}
        ) >= 0;
    }

    // TODO: Refactor this to use Vectors instead of arrays
    private static double perpDotProduct(double[] a, double[] b) {
        return VectorOperationsKt.dotPerp(
            new Vector(a[0], a[1]),
            new Vector(b[0], b[1])
        );
    }

    void eliminate2NodeLav(Node neighbor) {
        // TODO: Move this method to the Node class
        assert next() == neighbor && neighbor.next() == this;
    }
}
