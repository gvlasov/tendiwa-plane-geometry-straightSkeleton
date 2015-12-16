package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.google.common.collect.Lists
import org.tendiwa.collections.goForwardLooped
import org.tendiwa.collections.loopedTriLinks
import org.tendiwa.plane.geometry.bends.Bend
import org.tendiwa.plane.geometry.bends.isStraight
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.segments.Segment
import java.util.*

/**
 * From a list of vertices forming a polygon, creates [OriginalEdgeStart]s
 * and connects those [OriginalEdgeStart]s with each other.
 */
internal class InitialListOfActiveVertices
/**
 * @param polygon Counter-clockwise polygon.
 */
(
    polygon: Polygon,
    trustCounterClockwise: Boolean
) {
    val nodes: MutableList<OriginalEdgeStart> = ArrayList()
    val edges: List<Segment>
    private val size: Int

    init {
        var vertices = polygon.points
        if (!trustCounterClockwise && polygon.isClockwise()) {
            vertices = Lists.reverse(vertices)
        }
        vertices = filterExtraVertices(vertices)
        edges = createEdgesBetweenVertices(vertices)
        assert(vertices.size == edges.size)
        createAndConnectNodes(edges)
        nodes.forEach({ it.computeReflexAndBisector() })
        this.size = edges.size
    }


    // TODO: Remove this method, use field size instead
    fun size(): Int {
        return size
    }

    private fun createAndConnectNodes(edges: List<Segment>) {
        val l = edges.size
        var previous: OriginalEdgeStart? = null
        for (i in 0..l - 1) {
            val node = OriginalEdgeStart(edges[i])
            if (i > 0) {
                node.setPreviousInLav(previous)
                node.setPreviousInitial(previous!!)
            }
            previous = node
            nodes.add(node)
        }
        val first = nodes[0]
        val last = nodes[l - 1]
        first.setPreviousInLav(last)
        first.setPreviousInitial(last)
        nodes.forEach({ it.initFace() })
    }

    private fun createEdgesBetweenVertices(vertices: List<Point>): List<Segment> {
        val l = vertices.size
        val edges = ArrayList<Segment>(l)
        for (i in 0..l - 1) {
            edges.add(
                Segment(
                    vertices[i],
                    vertices.goForwardLooped(i, 1)))
        }
        return edges
    }
}

private fun filterExtraVertices(vertices: List<Point>): List<Point> {
    val answer = ArrayList<Point>()
    for (triple in vertices.loopedTriLinks) {
        val bend = Bend(triple.first, triple.second, triple.third)
        if (!bend.isStraight) {
            answer.add(bend.middle)
        }
    }
    assert(answer.size == vertices.size)
    return answer
}
