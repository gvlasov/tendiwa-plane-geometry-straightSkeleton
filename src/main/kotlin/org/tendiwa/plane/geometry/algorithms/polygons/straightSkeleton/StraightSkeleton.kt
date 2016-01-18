package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableSet
import org.jgrapht.UndirectedGraph
import org.tendiwa.math.constants.EPSILON
import org.tendiwa.plane.geometry.graphs.constructors.Graph2D
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.segments.Segment
import java.util.*

class StraightSkeleton private constructor(
    polygon: Polygon,
    trustCounterClockwise: Boolean
) {
    internal val debug = Debug()
    private val initialLav: InitialListOfActiveVertices
    private val queue: PriorityQueue<SkeletonEvent>
    private val arcs = HashMultimap.create<Point, Point>()
    private val polygon: Polygon
    private val hash = skeletonNumber++

    constructor(polygon: Polygon) : this(polygon, false) {
    }

    init {
        this.polygon = polygon.parallelAndPerpendicularEdgesDeflected
        //		Utils.printListOfPoints(vertices);

        this.initialLav = InitialListOfActiveVertices(
            this.polygon,
            trustCounterClockwise
        )
        this.queue = PriorityQueue<SkeletonEvent>(initialLav.size)

        // [Obdrzalek 1998, paragraph 2.2, algorithm step 1c]
        initialLav.nodes.forEach({ this.queueEventFromNode(it) })
        assert(!queue.isEmpty())

        while (!queue.isEmpty()) {
            // Convex 2a
            val event = queue.poll()
            event.handle(this)
            //			debug.drawEventHeight(event);
            assert(java.lang.Boolean.TRUE)
        }
        assert(!arcs.isEmpty)
    }

    override fun hashCode(): Int {
        return hash
    }

    /**
     * Makes a [Node] produce a [SkeletonEvent] and adds that event
     * to the event queue if it is not null. It it is null, this method does
     * nothing.
     * @param node A node that produces an event.
     */
    internal fun queueEventFromNode(node: Node) {
        val e = node.computeNearerBisectorsIntersection()
        if (e != null) {
            queue.add(e)
        }
    }

    internal fun outputArc(start: Point, end: Point) {
        arcs.put(start, end)
        debug.testForNoIntersection(arcs, start, end)
    }

    fun graph(): UndirectedGraph<Point, Segment> {
        val graph = Graph2D()
        for (startToEnds in arcs.asMap().entries) {
            val start = startToEnds.key
            graph.addVertex(start)
            for (end in startToEnds.value) {
                graph.addVertex(end)
                graph.addEdge(start, end)
            }
        }
        return graph
    }

    fun originalEdges(): List<Segment> {
        return initialLav.edges
    }

    fun cap(depth: Double): ImmutableSet<Polygon> {
        if (depth <= -EPSILON) {
            throw IllegalArgumentException("Cap depth can't be negative")
        }
        if (depth <= EPSILON) {
            return ImmutableSet.of(polygon)
        } else {
            return ShrinkedFront(faces(), depth).polygons()
        }
    }

    fun faces(): Set<StraightSkeletonFace> {
        return initialLav.nodes
            .map({ node -> node.face().toPolygon() })
            .toCollection(LinkedHashSet())
    }

    internal fun queueEvent(splitEvent: SplitEvent?) {
        assert(splitEvent != null)
        queue.add(splitEvent)
    }

    companion object {
        private var skeletonNumber = 0
    }
}
