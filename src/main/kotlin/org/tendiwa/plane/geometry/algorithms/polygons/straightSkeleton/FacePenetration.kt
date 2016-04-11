package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.points.comparePointsLinewise
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.parallel
import java.util.*

internal class FacePenetration(
    face: StraightSkeletonFace,
    front: Penetrable
) : Iterator<Point> {

    private val queue: Queue<Point>

    init {
        this.queue = PriorityQueue<Point>(::comparePointsLinewise)
        val intruded = face.intrudeFront(front.depth)
        face.polygon.segments
            .map { it -> front.obtainIntersectionPoint(it, intruded) }
            .filterNotNull()
            .forEach({ queue.add(it) })
        assert(queue.size % 2 == 0)
    }

    override fun hasNext(): Boolean =
        !queue.isEmpty()

    override fun next(): Point =
        queue.poll()
}

private fun StraightSkeletonFace.intrudeFront(depth: Double): Segment =
    front.parallel(depth, true)
