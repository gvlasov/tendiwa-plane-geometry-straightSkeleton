package org.tendiwa.plane.geometry.straightSkeleton;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.polygons.Polygon;
import org.tendiwa.geometry.segments.Segment;
import org.tendiwa.math.constants.ConstantsKt;
import org.tendiwa.plane.geometry.graphs.constructors.Graph2DConstructorsKt;
import org.tenidwa.collections.utils.Collectors;

public final class StraightSkeleton {
    private static int skeletonNumber = 0;
    final Debug debug = new Debug();
    private final InitialListOfActiveVertices initialLav;
    private final PriorityQueue<SkeletonEvent> queue;
    private final Multimap<Point, Point> arcs = HashMultimap.create();
    private final Polygon polygon;
    private int hash = skeletonNumber++;

    public StraightSkeleton(Polygon polygon) {
        this(polygon, false);
    }

    private StraightSkeleton(Polygon polygon, boolean trustCounterClockwise) {
        this.polygon = EdgePerturbationsKt.getParallelAndPerpendicularEdgesDeflected(
            polygon
        );
//		Utils.printListOfPoints(vertices);

        this.initialLav = new InitialListOfActiveVertices(
            this.polygon.getPoints(),
            trustCounterClockwise
        );
        this.queue = new PriorityQueue<>(initialLav.size());

        // [Obdrzalek 1998, paragraph 2.2, algorithm step 1c]
        initialLav.nodes.forEach(this::queueEventFromNode);
        assert !queue.isEmpty();

        while (!queue.isEmpty()) {
            // Convex 2a
            SkeletonEvent event = queue.poll();
            event.handle(this);
//			debug.drawEventHeight(event);
            assert Boolean.TRUE;
        }
        assert !arcs.isEmpty();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Makes a {@link Node} produce a {@link SkeletonEvent} and adds that event
     * to the event queue if it is not null. It it is null, this method does
     * nothing.
     * @param node A node that produces an event.
     */
    void queueEventFromNode(Node node) {
        SkeletonEvent e = node.computeNearerBisectorsIntersection();
        if (e != null) {
            queue.add(e);
        }
    }

    void outputArc(Point start, Point end) {
        assert start != null;
        assert end != null;
        arcs.put(start, end);
        debug.testForNoIntersection(arcs, start, end);
    }

    public UndirectedGraph<Point, Segment> graph() {
        UndirectedGraph<Point, Segment> graph = Graph2DConstructorsKt.Graph2D();
        for (Map.Entry<Point, Collection<Point>> startToEnds : arcs.asMap().entrySet()) {
            Point start = startToEnds.getKey();
            graph.addVertex(start);
            for (Point end : startToEnds.getValue()) {
                graph.addVertex(end);
                graph.addEdge(start, end);
            }
        }
        return graph;
    }

    public List<Segment> originalEdges() {
        return initialLav.edges;
    }

    public ImmutableSet<Polygon> cap(double depth) {
        if (depth <= -ConstantsKt.getEPSILON()) {
            throw new IllegalArgumentException("Cap depth can't be negative");
        }
        if (depth <= ConstantsKt.getEPSILON()) {
            return ImmutableSet.of(polygon);
        } else {
            return new ShrinkedFront(faces(), depth).polygons();
        }
    }

    public Set<StraightSkeletonFace> faces() {
        return initialLav.nodes.stream()
            .map(node -> node.face().toPolygon())
            .collect(Collectors.toLinkedHashSet());
    }

    void queueEvent(SplitEvent splitEvent) {
        assert splitEvent != null;
        queue.add(splitEvent);
    }
}
