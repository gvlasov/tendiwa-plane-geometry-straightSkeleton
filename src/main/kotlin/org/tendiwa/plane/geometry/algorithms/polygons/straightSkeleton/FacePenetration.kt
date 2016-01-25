package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.canvas.algorithms.geometry.draw
import org.tendiwa.canvas.awt.AwtCanvas
import org.tendiwa.plane.geometry.circles.Circle
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.points.comparePointsLinewise
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.parallel
import org.tendiwa.plane.grid.dimensions.by
import java.awt.Color
import java.util.*

internal class FacePenetration(face: StraightSkeletonFace, front: Penetrable) : Iterator<Point> {

    private val queue: java.util.Queue<Point>

    init {
        //		TestCanvas.canvas.draw(faceFront(face), DrawingSegment.withColorDirected(Color.cyan, 1));
        this.queue = PriorityQueue<Point>(::comparePointsLinewise)
        val intruded = face.intrudeFront(front.depth())
        face.polygon.segments
            .map({ segment -> front.obtainIntersectionPoint(segment, intruded) })
            .filterNotNull()
            .forEach({ queue.add(it) })

        validateSize(face, intruded)
    }

    private fun validateSize(face: StraightSkeletonFace, intruded: Segment) {
        val ok = queue.size % 2 == 0
        if (!ok) {
            val canvas = AwtCanvas(100.by(100), 4)
            canvas.draw(face.polygon, Color.red)
            canvas.draw(
                Circle(queue.peek(), 1.0),
                Color.blue)
            canvas.draw(intruded, Color.green)
        }
        assert(ok) { queue.size }
    }


    override fun hasNext(): Boolean {
        return !queue.isEmpty()
    }

    override fun next(): Point {
        return queue.poll()
    }
}

private fun StraightSkeletonFace.intrudeFront(depth: Double): Segment =
    front.parallel(depth, true)
