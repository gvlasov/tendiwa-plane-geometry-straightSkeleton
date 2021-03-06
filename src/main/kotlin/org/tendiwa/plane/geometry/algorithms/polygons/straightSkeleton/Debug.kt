package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.google.common.collect.Multimap
import org.tendiwa.canvas.algorithms.geometry.drawBillboard
import org.tendiwa.canvas.api.Canvas
import org.tendiwa.canvas.implementations.NullCanvas
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.areIntersected
import java.awt.Color

internal class Debug {
    private val debug = false

    fun drawSplitEventArc(event: SplitEvent) {
        if (!debug) return
        canvas.drawSegment(
            Segment(event.parent().vertex, event.point),
            Color.red
        )
    }

    fun drawEdgeEventArcs(leftParent: Node, rightParent: Node, point: Point) {
        if (!debug) return
        canvas.drawSegment(
            Segment(leftParent.vertex, point),
            Color.orange
        )
        canvas.drawSegment(
            Segment(rightParent.vertex, point),
            Color.yellow
        )
    }

    fun testForNoIntersection(
        arcs: Multimap<Point, Point>,
        start: Point,
        end: Point) {
        if (!debug) return
        if (
        arcs
            .entries()
            .map({ e -> Segment(e.key, e.value) })
            .areIntersected()
        ) {
            drawIntersectingArc(start, end)
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
        canvas.drawSegment(
            Segment(start, end),
            Color.white)
    }

    fun draw3NodeLavArcs(point: EdgeEvent) {
        if (!debug) return
        canvas.drawSegment(
            Segment(point.leftParent().vertex, point.point),
            Color.cyan
        )
        canvas.drawSegment(
            Segment(point.rightParent().vertex, point.point),
            Color.cyan
        )
        canvas.drawSegment(
            Segment(point.leftParent().previous().vertex, point.point),
            Color.cyan
        )
    }

    fun draw2NodeLavArc(node1: Node, node2: Node) {
        if (!debug) return
        canvas.drawSegment(
            Segment(node1.vertex, node2.vertex),
            Color.magenta)
    }

    companion object {
        val canvas: Canvas =
            NullCanvas()
        //            AwtCanvas(400.by(400), 1)
    }
}
