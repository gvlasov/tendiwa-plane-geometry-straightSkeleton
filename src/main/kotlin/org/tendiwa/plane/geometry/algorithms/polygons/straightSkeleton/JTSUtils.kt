package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.vividsolutions.jts.algorithm.CGAlgorithms
import com.vividsolutions.jts.geom.Coordinate
import org.tendiwa.plane.geometry.points.Point

internal object JTSUtils {
    // TODO: http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
    fun isYDownCCW(vertices: List<Point>): Boolean {

        val l = vertices.size
        val coordinates = arrayOfNulls<Coordinate>(l + 1)
        var i = 0
        for (point in vertices) {
            coordinates[i++] = Coordinate(point.x, point.y)
        }
        coordinates[l] = coordinates[0]
        // JTS's isCCW assumes y-up
        return !CGAlgorithms.isCCW(coordinates)
    }
}
