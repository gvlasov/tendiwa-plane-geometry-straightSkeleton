package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import com.google.common.collect.Iterators
import org.tendiwa.canvas.algorithms.geometry.drawArrow
import org.tendiwa.math.constants.EPSILON
import org.tendiwa.plane.geometry.circles.Circle
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.points.distanceTo
import org.tendiwa.plane.geometry.points.radiusVector
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.rays.RayIntersection
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.isParallel
import org.tendiwa.plane.geometry.segments.reverse
import org.tendiwa.plane.geometry.segments.vector
import org.tendiwa.plane.geometry.vectors.*
import org.tendiwa.plane.geometry.vectors.Vector
import org.tenidwa.collections.utils.SuccessiveTuples
import java.awt.Color
import java.util.*

/**
 * A node in a circular list of active vertices.
 */
internal abstract class Node protected constructor(val vertex: Point) : Iterable<Node> {
    protected var next: Node? = null
    /**
     * Along with [.previousEdgeStart], determines the direction of
     * [.bisector] as well as two faces that this Node divides.
     */
    internal var currentEdgeStart: OriginalEdgeStart? = null
    internal var previousEdgeStart: OriginalEdgeStart? = null
    internal var currentEdge: Segment? = null
    // TODO: Does this really have to be "Segment?" rather than "Segment"?
    var bisector: Segment? = null
    var isReflex: Boolean = false
    var isProcessed = false
        private set // As said in 1a in [Obdrzalek 1998, paragraph 2.1]
    /**
     * This field is variable because to initialize it, we must construct all
     * the nodes that form the [InitialListOfActiveVertices].
     */
    private var previous: Node? = null

    constructor(
        point: Point,
        previousEdgeStart: OriginalEdgeStart,
        currentEdgeStart: OriginalEdgeStart
    ) : this(point) {
        if (previousEdgeStart == currentEdgeStart) {
            assert(false)
        }
        currentEdge = currentEdgeStart.currentEdge()
        this.currentEdgeStart = currentEdgeStart
        this.previousEdgeStart = previousEdgeStart
        if (previousEdge() == currentEdge()) {
            assert(false)
        }
        // TODO: Lol, dafuq! Previous is parallel to previous?
        assert(
            !(previousEdge() == currentEdge()
                && previousEdge().isParallel(previousEdge()))
        )
    }

    private fun trySameLineIntersection(
        intersection: RayIntersection,
        current: Node,
        target: Node
    ): EdgeEvent? {
        if (java.lang.Double.isInfinite(intersection.r)) {
            return EdgeEvent(
                // TODO: Extract this expression into a method
                Point(
                    (target.vertex.x + current.vertex.x) / 2,
                    (target.next().vertex.y + current.vertex.y) / 2
                ),
                current,
                target
            )
        }
        return null
    }

    /**
     * Given 3 counter-clockwise points of a polygon, check if the middle one is convex or reflex.
     * @param previous Beginning of vector 1.
     * @param point End of vector 1 and beginning of vector 2.
     * @param next End of vector 2.
     * @return true if `point` is non-convex, false if it is convex.
     */
    private fun isPointNonConvex(
        previous: Point,
        point: Point,
        next: Point
    ): Boolean
        // TODO: There is similar method isReflex; remove this method.
    {
        assert(
            !Polygon(
                listOf(previous, point, next)
            ).isClockwise()
        )
        return perpDotProduct(
            doubleArrayOf(point.x - previous.x, point.y - previous.y),
            doubleArrayOf(next.x - point.x, next.y - point.y)
        ) <= 0
    }

    // TODO: Refactor this to use Vectors instead of arrays
    private fun perpDotProduct(a: DoubleArray, b: DoubleArray): Double =
        Vector(a[0], a[1]).dotPerp(Vector(b[0], b[1]))

    /**
     * Adds `newNode` to faces at [.currentEdgeStart] and [.previousEdgeStart] *if* it is
     * necessary.
     */
    fun growAdjacentFaces(newNode: Node) {
        growLeftFace(newNode)
        growRightFace(newNode)
    }

    fun growRightFace(newNode: Node) {
        growFace(newNode, currentEdgeStart!!)
    }

    fun growLeftFace(newNode: Node) {
        growFace(newNode, previousEdgeStart!!)
    }

    private fun growFace(newNode: Node, faceStart: OriginalEdgeStart) {
        faceStart.face().addLink(this, newNode)
    }

    fun drawLav() {
        SuccessiveTuples.forEachLooped(
            this
        ) { a, b ->
            Debug.canvas.drawArrow(
                Segment(a.vertex, b.vertex),
                Color.cyan,
                1.0
            )
        }
    }

    internal abstract fun hasPair(): Boolean

    open val pair: SplitNode?
        get() = throw RuntimeException(
            "${this.javaClass.name} can't have a pair; only SplitNode can"
        )

    operator fun next(): Node =
        next!!

    fun previous(): Node =
        previous!!

    fun previousEdge(): Segment =
        previousEdgeStart!!.currentEdge!!

    fun currentEdge(): Segment =
        currentEdgeStart!!.currentEdge!!

    /**
     * Remembers that this point is processed, that is, it is not a part of some LAV anymore.
     */
    fun setProcessed() {
        assert(!isProcessed)
        isProcessed = true
    }

    fun computeReflexAndBisector() {
        assert(bisector == null)
        isReflex = this is OriginalEdgeStart && isReflex(
            previous!!.vertex,
            vertex,
            vertex,
            next!!.vertex
        )
        val bisector1 = VectorSector(
            currentEdgeStart!!.currentEdge().vector,
            previousEdgeStart!!.currentEdge().vector.unaryMinus()
        )
        val bisectorVector = if (isReflex) {
            bisector1.bisector
        } else {
            bisector1.sumVector
        }
        // TODO: Extract this expression into a method
        bisector = Segment(
            vertex,
            vertex.radiusVector.plus(bisectorVector).point
        )
        //		if (bisector.start.distanceTo(new Point(416, 384)) < 12) {
        //			TestCanvas.canvas.draw(bisector, DrawingSegment.withColorDirected(Color.green, 1));
        //		}
    }

    /**
     * Finds if two edges going counter-clockwise make a convex or a reflex
     * angle.
     * @param a1 Start of the first edge.
     * @param a2 End of the first edge.
     * @param b1 Start of the second edge.
     * @param b2 End of the second edge.
     * @return True if the angle to the left between two edges
     * `> Math.PI` (reflex), false otherwise (convex).
     */
    private fun isReflex(a1: Point, a2: Point, b1: Point, b2: Point): Boolean =
        perpDotProduct(
            doubleArrayOf(a2.x - a1.x, a2.y - a1.y),
            doubleArrayOf(b2.x - b1.x, b2.y - b1.y)
        ) < 0

    fun setPreviousInLav(previous: Node?) {
        assert(previous !== this)
        assert(previous != null)
        this.previous = previous
        previous!!.next = this
    }

    val isInLavOf2Nodes: Boolean
        get() = next!!.next === this

    /**
     * Iterates over the current LAV of the node. All the nodes iterated upon
     * are non-[processed][Node.isProcessed].
     */
    override fun iterator(): Iterator<Node> {
        if (isProcessed) {
            assert(false)
        }
        return object : Iterator<Node> {
            internal var points: MutableList<Point> = ArrayList(100)
            internal var start = this@Node
            internal var node: Node = this@Node.previous!!
            internal var i = 0

            override fun hasNext(): Boolean {
                return node.next !== start || i == 0
            }

            override fun next(): Node {
                node = node.next!!
                if (node.isProcessed) {
                    showCurrentLav()
                    throw RuntimeException("Node not in lav")
                }
                checkLavCorrectness()
                points.add(node.vertex)
                return node
            }

            private fun showCurrentLav() {
                var current = start
                do {
                    Debug.canvas.drawArrow(
                        Segment(current.vertex, current.next!!.vertex),
                        Color.cyan,
                        0.5)
                    current = current.next!!
                } while (current !== node)
                assert(java.lang.Boolean.TRUE)
            }

            private fun checkLavCorrectness() {
                if (++i > 1000) {
                    drawLav()
                    throw RuntimeException("Too many iterations")
                }
            }

            private fun drawLav() {
                val colors = Iterators.cycle(
                    Color.darkGray,
                    Color.gray,
                    Color.lightGray,
                    Color.white)
                for (i in 0..points.size - 1 - 1) {
                    Debug.canvas.draw(
                        Segment(points[i], points[i + 1]),
                        colors.next())
                }
                Debug.canvas.draw(
                    Circle(start.vertex, 2.0),
                    Color.yellow)
            }
        }
    }

    /**
     * A usual node is never a pair for some other node. Only a
     * [SplitNode] may be a pair to another [SplitNode].
     * @param node Another node.
     * *
     * @return true if this node and `node` were created by the same
     * * [SplitEvent], false
     * * otherwise.
     */
    open fun isPair(node: Node): Boolean =
        false

    fun computeNearerBisectorsIntersection(): SkeletonEvent? {
        // Non-convex 1c
        val nextIntersection = bisectorsIntersection(next())
        var sameLineIntersection = trySameLineIntersection(nextIntersection, this, next())
        if (sameLineIntersection != null) {
            return sameLineIntersection
        }

        val previousIntersection = bisectorsIntersection(previous())
        sameLineIntersection = trySameLineIntersection(previousIntersection, this, previous())
        if (sameLineIntersection != null) {
            return sameLineIntersection
        }

        var shrinkPoint: Point? = null
        var va: Node? = null
        var vb: Node? = null
        if (nextIntersection.r > 0 || previousIntersection.r > 0) {
            if (previousIntersection.r < 0 && nextIntersection.r > 0 || nextIntersection.r > 0 && nextIntersection.r <= previousIntersection.r) {
                if (next().bisectorsIntersection(this).r > 0 && nextIntersection.r > 0) {
                    shrinkPoint = nextIntersection.commonPoint()
                    va = this
                    vb = next()
                }
            } else if (nextIntersection.r < 0 && previousIntersection.r > 0 || previousIntersection.r > 0 && previousIntersection.r <= nextIntersection.r) {
                if (previous().bisectorsIntersection(this).r > 0 && previousIntersection.r > 0) {
                    shrinkPoint = previousIntersection.commonPoint()
                    va = previous()
                    vb = this
                }
            }
        }
        if (isReflex) {
            val splitEvent = findSplitEvent()
            if (splitPointIsBetterThanShrinkPoint(splitEvent, shrinkPoint)) {
                return splitEvent
            }
        }
        assert(shrinkPoint == null || va != null && vb != null)
        assert(va == null && vb == null || va!!.next() === vb)
        if (shrinkPoint == null) {
            return null
        }
        // TODO: I have to va!!, but I don't have to vb!!
        return EdgeEvent(shrinkPoint, va!!, vb)
    }

    private fun splitPointIsBetterThanShrinkPoint(
        splitEvent: SkeletonEvent?,
        shrinkPoint: Point?): Boolean {
        if (splitEvent == null) {
            return false
        } else if (shrinkPoint == null) {
            return true
        }
        return vertex.distanceTo(splitEvent.point) < vertex.distanceTo(shrinkPoint)
    }

    private fun bisectorsIntersection(node: Node): RayIntersection =
        RayIntersection(bisector!!, node.bisector!!)

    /**
     * [Obdrzalek 1998, paragraph 2.2, figure 4]
     *
     *
     * Computes the point where a split event occurs.
     * @return The point where split event occurs, or null if there is no split
     * * event emanated from `reflexNode`.
     */
    private fun findSplitEvent(): SplitEvent? {
        assert(isReflex)
        var splitPoint: Point? = null
        var originalEdgeStart: Node? = null
        for (node in this) {
            if (nodeIsAppropriate(node)) {
                val point = computeSplitPoint(node.currentEdge())
                if (node.isPointInAreaBetweenEdgeAndItsBisectors(point)) {
                    if (newSplitPointIsBetter(splitPoint, point)) {
                        splitPoint = point
                        originalEdgeStart = node
                    }
                }
            }
        }
        if (splitPoint == null) {
            return null
        }
        return SplitEvent(
            splitPoint,
            this,
            originalEdgeStart!!.currentEdgeStart!!
        )
    }

    /**
     * [Obdrzalek 1998, paragraph 2.2, Figure 4]
     *
     *
     * Computes point B_i.
     * @param oppositeEdge The tested line segment.
     * *
     * @return Intersection between the bisector at `currentNode` and the
     * * axis of the angle between one of the edges starting at
     * * `currentNode` and the tested line segment `oppositeEdge`.
     */
    private fun computeSplitPoint(oppositeEdge: Segment): Point {
        assert(isReflex)
        val bisectorStart = RayIntersection(
            if (previousEdge().isParallel(
                oppositeEdge))
                currentEdge()
            else
                previousEdge(),
            oppositeEdge).commonPoint()
        val cw = RayIntersection(
            bisector!!,
            oppositeEdge).commonPoint().radiusVector.minus(
            bisectorStart.radiusVector)
        val ccw = vertex.radiusVector.minus(
            bisectorStart.radiusVector)
        val anotherBisector = VectorSector(cw, ccw)
        val intersection = RayIntersection(
            Segment(
                bisectorStart,
                bisectorStart.radiusVector.plus(
                    anotherBisector.sumVector).point),
            bisector!!
        )
        return intersection.commonPoint()
    }

    private fun nodeIsAppropriate(node: Node): Boolean =
        // TODO: If the previous condition is unnecessary, then this condition is unnecessary too.
        !(
            nodeIsNeighbor(node)
                || intersectionIsBehindReflexNode(node)
                || previousEdgeIntersectsInFrontOfOppositeEdge(node)
                || currentEdgeIntersectsInFrontOfOppositeEdge(node)
            )

    private fun newSplitPointIsBetter(
        oldSplitPoint: Point?,
        newSplitPoint: Point
    ): Boolean =
        oldSplitPoint == null
            || vertex.distanceTo(oldSplitPoint) > vertex.distanceTo(newSplitPoint)

    private fun nodeIsNeighbor(node: Node): Boolean =
        node === this
            || node === previous()
            || node === next()

    private fun currentEdgeIntersectsInFrontOfOppositeEdge(
        oppositeEdgeStartCandidate: Node
    ): Boolean =
        RayIntersection(
            currentEdge!!.reverse,
            oppositeEdgeStartCandidate.currentEdge!!
        ).r <= 1

    private fun previousEdgeIntersectsInFrontOfOppositeEdge(
        oppositeEdgeStartCandidate: Node
    ): Boolean =
        RayIntersection(
            previousEdge(),
            oppositeEdgeStartCandidate.currentEdge!!
        ).r <= 1

    private fun intersectionIsBehindReflexNode(
        anotherRay: Node
    ): Boolean =
        RayIntersection(
            bisector!!,
            anotherRay.currentEdge!!
        ).r <= EPSILON

    /**
     * [Obdrzalek 1998, paragraph 2.2, Figure 4]
     *
     *
     * Checks if a point (namely point B coming from a reflex vertex) is located
     * in an area bounded by an edge and bisectors coming from start and end
     * nodes of this edge.
     * @param point The point to test.
     * *
     * @return true if the point is located within the area marked by an edge
     * * and edge's bisectors, false otherwise.
     */
    private fun isPointInAreaBetweenEdgeAndItsBisectors(point: Point): Boolean {
        val a = bisector!!.end
        val b = this.currentEdge!!.start
        val c = this.currentEdge!!.end
        val d = next().bisector!!.end
        return isPointNonConvex(a, point, b) && isPointNonConvex(b, point, c) && isPointNonConvex(c, point, d)
    }

    fun eliminate2NodeLav(neighbor: Node) {
        // TODO: Move this method to the Node class
        assert(next() === neighbor && neighbor.next() === this)
    }
}
