package org.tendiwa.plane.geometry.straightSkeleton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.tendiwa.canvas.algorithms.geometry.DrawingArrowKt;
import org.tendiwa.collections.DoublyLinkedNode;
import org.tendiwa.geometry.algorithms.intersections.RayIntersection;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.polygons.Polygon;
import org.tendiwa.geometry.polygons.PolygonConstructorsKt;
import org.tendiwa.geometry.segments.Segment;
import org.tendiwa.geometry.segments.SegmentPropertiesKt;
import org.tendiwa.math.constants.ConstantsKt;

final class ShrinkedFront implements Penetrable {

    private final double depth;
    public List<Segment> edges;
    private final LinkedHashMap<Point, DoublyLinkedNode<Point>> pointsToNodes;
    private final BiMap<Point, Segment> intersectionsOnSegments;

    /**
     * @param faces Clockwise polygons partitioning a compound polygon. For each partitioning polygon, its last edge is the only
     * edge touching the perimeter of the compound polygon.
     * @param depth How much to intrude the polygon.
     */
    ShrinkedFront(Collection<StraightSkeletonFace> faces, double depth) {
        assert depth > ConstantsKt.getEPSILON();
        this.depth = depth;
        // Minimum possible number of points on a front is faces.size(), so we pick a value twice as big. That should
        // be enough for most cases and not too much.
        this.pointsToNodes = new LinkedHashMap<>(faces.size() * 2);
        this.intersectionsOnSegments = HashBiMap.create();
        faces.stream()
            .map(face -> new FacePenetration(face, this))
            .forEach(this::integrate);
    }

    void integrate(Iterator<Point> penetration) {
        while (penetration.hasNext()) {
            add(
                // Get two consecutive intersection points
                penetration.next(),
                penetration.next()
            );
        }
    }

    @Override
    public void add(Point point1, Point point2) {
//		TestCanvas.canvas.draw(new Segment(point1, point2), DrawingSegment.withColorThin(Color.orange));
        DoublyLinkedNode<Point> node1 = obtainNode(point1);
        DoublyLinkedNode<Point> node2 = obtainNode(point2);
        node1.uniteWith(node2);
    }

    @Override
    public Optional<Point> obtainIntersectionPoint(
        Segment inner,
        Segment intruded
    ) {
        Segment reverse = SegmentPropertiesKt.getReverse(inner);
        if (intersectionsOnSegments.containsValue(reverse)) {
            return Optional.of(getExistingIntersectionPoint(reverse));
        } else {
            if (intersectionsOnSegments.containsValue(inner)) {
                DrawingArrowKt.drawArrow(
                    Debug.canvas,
                    inner,
                    Color.white,
                    1
                );
                assert false;
            }
            RayIntersection intersection = new RayIntersection(inner, intruded);
            if (intersection.getR() > 0 && intersection.getR() < 1) {
                Point intersectionPoint = new RayIntersection(intruded, inner).commonPoint();
                intersectionsOnSegments.put(intersectionPoint, inner);
                return Optional.of(intersectionPoint);
            }
        }
        return Optional.empty();
    }

    @Override
    public double depth() {
        return depth;
    }

    private Point getExistingIntersectionPoint(Segment reverse) {
        return intersectionsOnSegments
            .inverse()
            .get(reverse);
    }

    /**
     * Returns the existing {@link org.tendiwa.collections.DoublyLinkedNode} for a {@link Point} if one exists, or
     * creates a new one.
     * @param point A point that is payload for a node.
     * @return A node with {@code point} as payload.
     */
    private DoublyLinkedNode<Point> obtainNode(Point point) {
        if (pointsToNodes.containsKey(point)) {
            return pointsToNodes.get(point);
        } else {
            DoublyLinkedNode<Point> newNode = new DoublyLinkedNode<>(point);
            pointsToNodes.put(point, newNode);
            return newNode;
        }
    }

    public ImmutableSet<Polygon> polygons() {
        ImmutableSet.Builder<Polygon> builder = ImmutableSet.builder();
        while (!pointsToNodes.isEmpty()) {
            DoublyLinkedNode<Point> node = pointsToNodes.values().stream()
                .findFirst()
                .get();
            List<Point> points = new ArrayList<>();
            node.forEach(points::add);
            Polygon polygon = PolygonConstructorsKt.Polygon(points);
            builder.add(polygon);
            polygon.getPoints().forEach(pointsToNodes::remove);
        }
        return builder.build();
    }
}
