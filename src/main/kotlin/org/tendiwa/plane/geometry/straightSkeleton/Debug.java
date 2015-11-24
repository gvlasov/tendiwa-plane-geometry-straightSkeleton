package org.tendiwa.plane.geometry.straightSkeleton;

import com.google.common.collect.Multimap;
import java.awt.*;
import org.tendiwa.canvas.algorithms.geometry.DrawingBillboardKt;
import org.tendiwa.canvas.api.Canvas;
import org.tendiwa.canvas.awt.AwtCanvas;
import org.tendiwa.geometry.algorithms.intersections.ShamosHoeyAlgorithmKt;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.segments.Segment;
import org.tendiwa.grid.dimensions.GridDimensionConstructorsKt;
import static java.util.stream.Collectors.toList;

final class Debug {
    private final boolean debug = true;
    public final static Canvas canvas =
        new AwtCanvas(GridDimensionConstructorsKt.by(400, 400), 1);

    Debug() {
    }

    void drawSplitEventArc(SplitEvent event) {
        if (!debug) return;
        canvas.draw(
            new Segment(event.parent().vertex, event.point),
            Color.red
        );
    }

    void drawEdgeEventArcs(Node leftParent, Node rightParent, Point point) {
        if (!debug) return;
        canvas.draw(
            new Segment(leftParent.vertex, point),
            Color.orange
        );
        canvas.draw(
            new Segment(rightParent.vertex, point),
            Color.yellow
        );
    }

    void testForNoIntersection(
        Multimap<Point, Point> arcs,
        Point start,
        Point end
    ) {
        if (!debug) return;
        if (
            ShamosHoeyAlgorithmKt.areIntersected(
                arcs.entries().stream()
                    .map(e -> new Segment(e.getKey(), e.getValue()))
                    .collect(toList()))
            ) {
            drawIntersectingArc(start, end);
            System.out.println(start);
            assert false;
        }
    }

    void drawEventHeight(SkeletonEvent event) {
        DrawingBillboardKt.drawBillboard(
            canvas,
            event.point,
            String.format("%1.6s", event.distanceToOriginalEdge),
            Color.black,
            Color.white

        );
    }

    void drawIntersectingArc(Point start, Point end) {
        if (!debug) return;
        canvas.draw(
            new Segment(start, end),
            Color.white
        );
    }

    void draw3NodeLavArcs(EdgeEvent point) {
        if (!debug) return;
        canvas.draw(
            new Segment(point.leftParent().vertex, point.point),
            Color.cyan
        );
        canvas.draw(
            new Segment(point.rightParent().vertex, point.point),
            Color.cyan
        );
        canvas.draw(
            new Segment(point.leftParent().previous().vertex, point.point),
            Color.cyan
        );
    }

    public void draw2NodeLavArc(Node node1, Node node2) {
        if (!debug) return;
        canvas.draw(
            new Segment(node1.vertex, node2.vertex),
            Color.magenta
        );
    }
}
