package org.tendiwa.plane.geometry.straightSkeleton

import org.tendiwa.geometry.vectors.*

internal class VectorSector(cw: Vector, ccw: Vector) {
	private var isReflex: Boolean

	val sumVector: Vector

	val bisector: Vector
		get()  = if (isReflex) sumVector.reverse() else sumVector

	init {
        if (cw.isZero) {
			throw IllegalArgumentException(
				"Trying to compute bisector when one of the vectors is 0"
			);
		}
		if (ccw.isZero) {
			throw IllegalArgumentException(
				"Trying to compute bisector when one of the vectors is 0"
			);
		}
		var bisectorDirection = cw.normalize() + ccw.normalize();
		if (bisectorDirection.isZero) {
			bisectorDirection = ccw.rotateQuarterClockwise();
		}
		sumVector = bisectorDirection.normalize() * averageMagnitude(cw, ccw)
		isReflex = ccw.makesReflexAngle(cw);
    }

	private fun averageMagnitude(cw: Vector, ccw: Vector): Double =
			cw.magnitude / 2 + ccw.magnitude / 2
	/**
	 * Checks if clockwise angle between this vector and another vector is
	 * `>Math.PI`. Relative to angle's bisector, this vector is considered
	 * counter-clockwise, and another is considered clockwise.
	 *
	 * @param cw Another vector.
	 * @return true if the angle between vectors going clockwise from this
	 * vector to {@code another} is reflex, false otherwise.
	 */
	private fun Vector.makesReflexAngle(cw: Vector): Boolean {
		return cw dotPerp this > 0;
	}
}
