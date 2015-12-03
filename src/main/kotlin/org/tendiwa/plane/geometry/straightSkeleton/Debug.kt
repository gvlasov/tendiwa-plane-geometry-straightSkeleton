package org.tendiwa.plane.geometry.straightSkeleton

import com.google.common.collect.Multimap
import org.tendiwa.canvas.algorithms.geometry.drawBillboard
import org.tendiwa.canvas.api.Canvas
import org.tendiwa.canvas.awt.AwtCanvas
import org.tendiwa.geometry.algorithms.intersections.areIntersected
import org.tendiwa.geometry.points.Point
import org.tendiwa.geometry.segments.Segment
import org.tendiwa.grid.dimensions.by
import java.awt.Color

internal class Debug {
    private val debug = true

    fun drawSplitEventArc(event: SplitEvent) {
        if (!debug) return
        canvas.draw(
            Segment(event.parent().vertex, event.point),
            Color.red)
    }

    fun drawEdgeEventArcs(leftParent: Node, rightParent: Node, point: Point) {
        if (!debug) return
        canvas.draw(
            Segment(leftParent.vertex, point),
            Color.orange)
        canvas.draw(
            Segment(rightParent.vertex, point),
            Color.yellow)
    }

    fun testForNoIntersection(
        arcs: Multimap<Point, Point>,
        start: Point,
        end: Point) {
        if (!debug) return
        if (arcs.entries().map({ e -> Segment(e.key, e.value) }).areIntersected()) {
            drawIntersectingArc(start, end)
            println(start)
            assert(false)
        }
    }

    fun drawEventHeight(event: SkeletonEvent) {
        canvas.drawBillboard(
            event.point,
            "%1.6s".format(event.distanceToOriginalEdge),
            Color.black,
            Color.white)
    }

    fun drawIntersectingArc(start: Point, end: Point) {
        if (!debug) return
        canvas.draw(
            Segment(start, end),
            Color.white)
    }

    fun draw3NodeLavArcs(point: EdgeEvent) {
        if (!debug) return
        canvas.draw(
            Segment(point.leftParent().vertex, point.point),
            Color.cyan)
        canvas.draw(
            Segment(point.rightParent().vertex, point.point),
            Color.cyan)
        canvas.draw(
            Segment(point.leftParent().previous().vertex, point.point),
            Color.cyan)
    }

    fun draw2NodeLavArc(node1: Node, node2: Node) {
        if (!debug) return
        canvas.draw(
            Segment(node1.vertex, node2.vertex),
            Color.magenta)
    }

    companion object {
        val canvas: Canvas = AwtCanvas(400.by(400), 1)
    }
}//        new NullCanvas();
