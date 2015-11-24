package org.tendiwa.plane.geometry.straightSkeleton;


import org.tendiwa.geometry.points.Point;

final class CenterNode extends Node {

    CenterNode(Point point) {
        super(point);
    }

    @Override
    boolean hasPair() {
        return false;
    }
}
