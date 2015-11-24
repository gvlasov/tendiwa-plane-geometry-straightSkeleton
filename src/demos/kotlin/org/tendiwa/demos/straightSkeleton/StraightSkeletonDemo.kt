package org.tendiwa.demos.straightSkeleton

import org.tendiwa.canvas.algorithms.geometry.draw
import org.tendiwa.canvas.awt.AwtCanvas
import org.tendiwa.geometry.trails.Trail
import org.tendiwa.geometry.trails.polygon
import org.tendiwa.plane.directions.CardinalDirection.*
import org.tendiwa.plane.directions.OrdinalDirection.NE
import org.tendiwa.plane.geometry.polygons.shrinking.shrink
import java.awt.Color

fun main(args: Array<String>) {
    AwtCanvas().apply {
        val polygon =
//            Rectangle(
//                ZeroPoint(),
//                100.0 by 100.0
//            )
        Trail(100.0, 100.0).apply {
            move(30.0, NE)
            move(35.0, E)
            move(13.0, S)
            move(20.0, NE)
            move(70.0, S)
            move(170.0, W)
        }
            .polygon
        draw(polygon, Color.black)

        val shrink = polygon.shrink(8.0)
        println(shrink)
        shrink.forEach {
            draw(it, Color.red)
        }
    }
}

