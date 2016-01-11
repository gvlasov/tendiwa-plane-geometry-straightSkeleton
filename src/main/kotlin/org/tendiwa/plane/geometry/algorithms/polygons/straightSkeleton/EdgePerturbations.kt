package org.tendiwa.plane.geometry.algorithms.polygons.straightSkeleton

import org.tendiwa.math.angles.Angle
import org.tendiwa.math.angles.cos
import org.tendiwa.math.angles.sin
import org.tendiwa.math.angles.times
import org.tendiwa.math.doubles.isNegativeZero
import org.tendiwa.plane.geometry.points.Point
import org.tendiwa.plane.geometry.polygons.Polygon
import org.tendiwa.plane.geometry.segments.Segment
import org.tendiwa.plane.geometry.segments.dx
import org.tendiwa.plane.geometry.segments.dy
import java.util.*

internal val Polygon.parallelAndPerpendicularEdgesDeflected: Polygon
    get() {
        return if (this.anySegmentsHaveSameOrPerpendicularSlope) {
            attempt(
                attempts = 10,
                producer = { i -> this.perturb(Random(i.toLong())) },
                success = { !it.anySegmentsHaveSameOrPerpendicularSlope }
            )
        } else {
            this
        }
    }

private fun <T> attempt(
    attempts: Int,
    producer: (Int) -> T,
    success: (T) -> Boolean
): T {
    for (i in 1..attempts) {
        val candidate = producer(i)
        if (success(candidate)) {
            return candidate
        }
    }
    throw RuntimeException("Could not compute in $attempts attempts")
}

private val Polygon.anySegmentsHaveSameOrPerpendicularSlope: Boolean
    get() {
        return this.segmentsToSlopes
            .run { values.toHashSet().size != size }
    }

private val Polygon.segmentsToSlopes: Map<Segment, Double>
    get() =
    this.segments
        .map { Pair(it, it.run { absSmallerOverAbsGreater(dx, dy) }) }
        .map { treatNegative0SlopeAsJust0(it) }
        .toMap()

fun treatNegative0SlopeAsJust0(
    it: Pair<Segment, Double>
): Pair<Segment, Double> {
    return if (it.second.isNegativeZero) {
        Pair(it.first, 0.0)
    } else {
        it
    }
}

fun absSmallerOverAbsGreater(a: Double, b: Double): Double =
    if (Math.abs(a) < Math.abs(b)) a / b else b / a

private fun Polygon.perturb(random: Random): Polygon {
    return Polygon(
        this.points.map { it.shiftRandomly(random) }
    )
}

val SHIFT = 1e-4

private fun Point.shiftRandomly(random: Random): Point {
    val angle = Angle.FULL_CIRCLE * random.nextDouble()
    return Point(
        x + SHIFT * angle.cos,
        y + SHIFT * angle.sin
    )
}
