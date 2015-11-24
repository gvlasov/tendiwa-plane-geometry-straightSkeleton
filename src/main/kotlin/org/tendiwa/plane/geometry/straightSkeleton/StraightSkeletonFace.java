package org.tendiwa.plane.geometry.straightSkeleton;

import java.util.List;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.polygons.Polygon;
import org.tendiwa.geometry.polygons.PolygonConstructorsKt;
import org.tendiwa.geometry.segments.Segment;

final class StraightSkeletonFace {
    private final Polygon polygon;
    private Segment front;

    public StraightSkeletonFace(List<Point> points) {
        this.polygon = PolygonConstructorsKt.Polygon(points);
    }

    public Segment front() {
        if (front == null) {
            front = new Segment(
                polygon.getPoints().get(0),
                polygon.getPoints().get(polygon.getPoints().size() - 1)
            );
        }
        return front;
    }

    public Polygon polygon() {
        return polygon;
    }
}
