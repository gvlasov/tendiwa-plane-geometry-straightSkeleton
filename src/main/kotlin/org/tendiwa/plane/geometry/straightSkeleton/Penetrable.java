package org.tendiwa.plane.geometry.straightSkeleton;

import java.util.Optional;
import org.tendiwa.geometry.points.Point;
import org.tendiwa.geometry.segments.Segment;

interface Penetrable {
    void add(Point poll, Point poll1);

    Optional<Point> obtainIntersectionPoint(
        Segment intersected,
        Segment intersecting
    );

    double depth();
}
