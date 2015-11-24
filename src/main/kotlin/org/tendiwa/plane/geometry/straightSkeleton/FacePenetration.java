package org.tendiwa.plane.geometry.straightSkeleton;

import java.awt.Color;
import java.util.Iterator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import org.tendiwa.canvas.algorithms.geometry.DrawingGeometryKt;
import org.tendiwa.canvas.awt.AwtCanvas;
import org.tendiwa.geometry.circles.Circle;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.points.PointOperationsKt;
import org.tendiwa.geometry.segments.Segment;
import org.tendiwa.geometry.segments.SegmentOperationsKt;
import org.tendiwa.grid.dimensions.GridDimensionConstructorsKt;

final class FacePenetration implements Iterator<Point> {

    private final Queue<Point> queue;

    public FacePenetration(StraightSkeletonFace face, Penetrable front) {
//		TestCanvas.canvas.draw(faceFront(face), DrawingSegment.withColorDirected(Color.cyan, 1));
        this.queue =
            new PriorityQueue<>(PointOperationsKt::comparePointsLinewise);
        Segment intruded = intrudeFaceFront(face, front.depth());
        face.polygon().getSegments().stream()
            .map(segment -> front.obtainIntersectionPoint(segment, intruded))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(queue::add);

        validateSize(face, intruded);
    }

    private void validateSize(StraightSkeletonFace face, Segment intruded) {
        boolean ok = queue.size() % 2 == 0;
        if (!ok) {
            final AwtCanvas canvas =
                new AwtCanvas(GridDimensionConstructorsKt.by(100, 100), 4);
            DrawingGeometryKt.draw(canvas, face.polygon(), Color.red);
            canvas.draw(
                new Circle(queue.peek(), 1),
                Color.blue
            );
            canvas.draw(intruded, Color.green);
        }
        assert ok : queue.size();
    }

    // TODO: Make this an extension method of StraightSkeletonFace
    private Segment intrudeFaceFront(StraightSkeletonFace face, double depth) {
//		TestCanvas.canvas.draw(new Segment(face.get(0), face.get(face.size() - 1)), DrawingSegment.withColorThin(Color
//			.magenta));
        return SegmentOperationsKt.parallel(face.front(), depth, true);
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    @Override
    public Point next() {
        return queue.poll();
    }
}
