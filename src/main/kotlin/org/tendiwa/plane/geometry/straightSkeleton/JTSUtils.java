package org.tendiwa.plane.geometry.straightSkeleton;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import org.tendiwa.geometry.points.Point;

class JTSUtils {
	// TODO: http://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
	public static boolean isYDownCCW(List<Point> vertices) {

		int l = vertices.size();
		Coordinate[] coordinates = new Coordinate[l + 1];
		int i = 0;
		for (Point point : vertices) {
			coordinates[i++] = new Coordinate(point.getX(), point.getY());
		}
		coordinates[l] = coordinates[0];
		// JTS's isCCW assumes y-up
		return !CGAlgorithms.isCCW(coordinates);
	}
}
