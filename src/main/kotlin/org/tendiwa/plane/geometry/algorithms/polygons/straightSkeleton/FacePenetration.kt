package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.canvas.algorithms.geometry.drawPolygon
import org.tendiwa.canvas.awt.AwtCanvas
import org.tendiwa.plane.geometry.circles.Circle
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.points.comparePointsLinewise
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.parallel
import org.tendiwa.plane.grid.dimensions.by
import java.awt.Color
import java.util.*

internal class FacePenetration(
    face: StraightSkeletonFace,
    front: Penetrable
) : Iterator<Point> {

    private val queue: Queue<Point>

    init {
        //		TestCanvas.canvas.draw(faceFront(face), DrawingSegment.withColorDirected(Color.cyan, 1));
        this.queue = PriorityQueue<Point>(::comparePointsLinewise)
        val intruded = face.intrudeFront(front.depth)
        face.polygon.segments
            .map { it -> front.obtainIntersectionPoint(it, intruded) }
            .filterNotNull()
            .forEach({ queue.add(it) })
        validateSize(face, intruded)
    }

    private fun validateSize(
        face: StraightSkeletonFace,
        intruded: Segment
    ) {
        val ok = queue.size % 2 == 0
        if (!ok) {
            val canvas = AwtCanvas(100.by(100), 4)
            canvas.drawPolygon(face.polygon, Color.red)
            canvas.drawCircle(
                Circle(queue.peek(), 1.0),
                Color.blue
            )
            canvas.drawSegment(intruded, Color.green)
        }
        assert(ok) { queue.size }
    }


    override fun hasNext(): Boolean =
        !queue.isEmpty()

    override fun next(): Point =
        queue.poll()
}

private fun StraightSkeletonFace.intrudeFront(depth: Double): Segment =
    front.parallel(depth, true)
