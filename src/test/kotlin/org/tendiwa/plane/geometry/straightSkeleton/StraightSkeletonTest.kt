package org.tendiwa.plane.geometry.straightSkeleton

import org.junit.Test
import org.tendiwa.geometry.trails.Trail
import org.tendiwa.geometry.trails.polygon

class StraightSkeletonTest {
    @Test fun straightSkeleton() {
        StraightSkeleton(
            Trail(0.0, 0.0).apply {
                move(10.0, 0.0)
                move(10.0, 10.0)
            }.polygon
        )
    }
}
