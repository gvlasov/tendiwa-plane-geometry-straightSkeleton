package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.canvas.algorithms.geometry.draw
import org.tendiwa.canvas.awt.AwtCanvas
import org.tendiwa.geometry.circles.Circle
import org.tendiwa.geometry.points.Point
import org.tendiwa.geometry.points.comparePointsLinewise
import org.tendiwa.geometry.segments.Segment
import org.tendiwa.geometry.segments.parallel
import org.tendiwa.grid.dimensions.by
import java.awt.Color
import java.util.*

internal class FacePenetration(face: StraightSkeletonFace, front: Penetrable) : Iterator<Point> {

    private val queue: Queue<Point>

    init {
        //		TestCanvas.canvas.draw(faceFront(face), DrawingSegment.withColorDirected(Color.cyan, 1));
        this.queue = PriorityQueue<Point>(::comparePointsLinewise)
        val intruded = intrudeFaceFront(face, front.depth())
        face.polygon.segments
            .map({ segment -> front.obtainIntersectionPoint(segment, intruded) })
            .filter({ it.isPresent })
            .map({ it.get() })
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

    // TODO: Make this an extension method of StraightSkeletonFace
    private fun intrudeFaceFront(face: StraightSkeletonFace, depth: Double): Segment {
        //		TestCanvas.canvas.draw(new Segment(face.get(0), face.get(face.size() - 1)), DrawingSegment.withColorThin(Color
        //			.magenta));
        return face.front.parallel(depth, true)
    }

    override fun hasNext(): Boolean {
        return !queue.isEmpty()
    }

    override fun next(): Point {
        return queue.poll()
    }
}
