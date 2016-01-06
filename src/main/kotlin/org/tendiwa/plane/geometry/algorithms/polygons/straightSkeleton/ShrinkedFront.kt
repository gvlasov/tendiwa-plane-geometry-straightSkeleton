package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableSet
import org.tendiwa.canvas.algorithms.geometry.drawArrow
import org.tendiwa.collections.DoublyLinkedNode
import org.tendiwa.math.constants.EPSILON
import org.tendiwa.plane.geometry.rays.RayIntersection
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.reverse
import java.awt.Color
import java.util.*

internal class ShrinkedFront
/**
 * @param faces Clockwise polygons partitioning a compound polygon. For each
 * partitioning polygon, its last edge is the only edge touching the perimeter
 * of the compound polygon.
 * @param depth How much to intrude the polygon.
 */
(faces: Collection<StraightSkeletonFace>, private val depth: Double) : Penetrable {
    private val pointsToNodes: LinkedHashMap<Point, DoublyLinkedNode<Point>>
    private val intersectionsOnSegments: BiMap<Point, Segment>

    init {
        assert(depth > EPSILON)
        // Minimum possible number of points on a front is faces.size(), so we pick a value twice as big. That should
        // be enough for most cases and not too much.
        this.pointsToNodes = LinkedHashMap<Point, DoublyLinkedNode<Point>>(faces.size * 2)
        this.intersectionsOnSegments = HashBiMap.create<Point, Segment>()
        faces
            .map({ face -> FacePenetration(face, this) })
            .forEach({ this.integrate(it) })
    }

    fun integrate(penetration: Iterator<Point>) {
        while (penetration.hasNext()) {
            add(
                // Get two consecutive intersection points
                penetration.next(),
                penetration.next()
            )
        }
    }

    override fun add(point1: Point, point2: Point) {
        //		TestCanvas.canvas.draw(new Segment(point1, point2), DrawingSegment.withColorThin(Color.orange));
        val node1 = obtainNode(point1)
        val node2 = obtainNode(point2)
        node1.uniteWith(node2)
    }

    override fun obtainIntersectionPoint(
        inner: Segment,
        intruded: Segment): Optional<Point> {
        val reverse = inner.reverse
        if (intersectionsOnSegments.containsValue(reverse)) {
            return Optional.of(getExistingIntersectionPoint(reverse))
        } else {
            if (intersectionsOnSegments.containsValue(inner)) {
                Debug.canvas.drawArrow(
                    inner,
                    Color.white,
                    1.0
                )
                assert(false)
            }
            val intersection = RayIntersection(inner, intruded)
            if (intersection.r > 0 && intersection.r < 1) {
                val intersectionPoint = RayIntersection(intruded, inner).commonPoint()
                intersectionsOnSegments.put(intersectionPoint, inner)
                return Optional.of(intersectionPoint)
            }
        }
        return Optional.empty<Point>()
    }

    override fun depth(): Double {
        return depth
    }

    private fun getExistingIntersectionPoint(reverse: Segment): Point {
        return intersectionsOnSegments.inverse()[reverse]!!
    }

    /**
     * Returns the existing [DoublyLinkedNode] for a [Point] if one
     * exists, or creates a new one.
     * @param point A point that is payload for a node.
     * *
     * @return A node with `point` as payload.
     */
    private fun obtainNode(point: Point): DoublyLinkedNode<Point> {
        if (pointsToNodes.containsKey(point)) {
            return pointsToNodes[point]!!
        } else {
            val newNode = DoublyLinkedNode(point)
            pointsToNodes.put(point, newNode)
            return newNode
        }
    }

    fun polygons(): ImmutableSet<Polygon> {
        val builder = ImmutableSet.builder<Polygon>()
        while (!pointsToNodes.isEmpty()) {
            val node = pointsToNodes.values.first()
            val points = ArrayList<Point>()
            node.forEach({ points.add(it) })
            val polygon = Polygon(points)
            builder.add(polygon)
            polygon.points.forEach({ pointsToNodes.remove(it) })
        }
        return builder.build()
    }
}
