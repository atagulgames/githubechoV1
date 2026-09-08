package com.example.game

import com.example.model.EchoStroke
import com.example.model.Point
import com.example.model.Segment
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object CollisionEngine {

    /**
     * Exact CCW formula:
     * CCW(A, B, C) = (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x)
     */
    fun ccw(a: Point, b: Point, c: Point): Boolean {
        return (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
    }

    /**
     * Determines whether segment P1P2 intersects segment P3P4.
     * Guards against false positives where segments meet cleanly at a shared vertex/node.
     */
    fun doLinesIntersect(
        p1: Point,
        p2: Point,
        p3: Point,
        p4: Point,
        endpointTolerance: Float = 8.0f
    ): Boolean {
        if (p1.distanceTo(p2) < 4.0f || p3.distanceTo(p4) < 4.0f) {
            return false
        }

        // Check if endpoints coincide (e.g. shared node vertex)
        if (p1.distanceTo(p3) <= endpointTolerance ||
            p1.distanceTo(p4) <= endpointTolerance ||
            p2.distanceTo(p3) <= endpointTolerance ||
            p2.distanceTo(p4) <= endpointTolerance
        ) {
            return false
        }

        val ccw1 = ccw(p1, p3, p4)
        val ccw2 = ccw(p2, p3, p4)
        val ccw3 = ccw(p1, p2, p3)
        val ccw4 = ccw(p1, p2, p4)

        return (ccw1 != ccw2) && (ccw3 != ccw4)
    }

    /**
     * Checks if a candidate segment collides with any past echo segments.
     * hitboxScale allows the Echo Shrinker bonus (e.g. 0.5f scale factor).
     */
    fun checkCollisionWithEchoes(
        candidate: Segment,
        echoes: List<Segment>,
        endpointTolerance: Float = 8.0f,
        hitboxScale: Float = 1.0f
    ): Segment? {
        val effectiveTolerance = if (hitboxScale < 1.0f) endpointTolerance * 1.75f else endpointTolerance
        for (echo in echoes) {
            val echoToTest = if (hitboxScale < 1.0f) {
                // Physically shrink the echo segment towards its midpoint so player can slip past ends
                val midX = (echo.p1.x + echo.p2.x) * 0.5f
                val midY = (echo.p1.y + echo.p2.y) * 0.5f
                val p1Shrunk = Point(midX + (echo.p1.x - midX) * hitboxScale, midY + (echo.p1.y - midY) * hitboxScale)
                val p2Shrunk = Point(midX + (echo.p2.x - midX) * hitboxScale, midY + (echo.p2.y - midY) * hitboxScale)
                Segment(p1Shrunk, p2Shrunk, echo.fromNodeId, echo.toNodeId)
            } else {
                echo
            }
            if (doLinesIntersect(candidate.p1, candidate.p2, echoToTest.p1, echoToTest.p2, effectiveTolerance)) {
                return echo
            }
        }
        return null
    }

    /**
     * Self-Intersection check (Anlık Yol Çarpışması):
     * Per user directives:
     * - "Işınlar bir birinin içinde geçebilsin yankılardanda geçebilsin."
     * - "düğümler içinden geçilebilinsin mesela sağ ya sola doğru sol alt çapraz iç içe geçsin yani."
     * Lasers, segments, and nodes can freely intertwine, cross diagonally, and pass through each other.
     */
    fun checkSelfIntersection(
        activeSegments: List<Segment>,
        candidate: Segment
    ): Boolean {
        // Freely allow intertwining, overlapping, and diagonal criss-crossing
        return false
    }

    /**
     * Distance from point P to line segment AB.
     */
    fun distanceToSegment(p: Point, a: Point, b: Point): Float {
        val lengthSq = (b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y)
        if (lengthSq < 0.0001f) return p.distanceTo(a)

        // Projection factor t clamped between 0 and 1
        val t = max(0f, min(1f, ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / lengthSq))
        val projX = a.x + t * (b.x - a.x)
        val projY = a.y + t * (b.y - a.y)

        val dx = p.x - projX
        val dy = p.y - projY
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Finds the minimum distance from point P to any segment in echoes.
     */
    fun minDistanceToEchoes(point: Point, echoes: List<Segment>): Float {
        if (echoes.isEmpty()) return Float.MAX_VALUE
        var minDst = Float.MAX_VALUE
        for (echo in echoes) {
            val d = distanceToSegment(point, echo.p1, echo.p2)
            if (d < minDst) {
                minDst = d
            }
        }
        return minDst
    }

    fun minDistanceToEchoStrokes(point: Point, echoStrokes: List<EchoStroke>): Float {
        if (echoStrokes.isEmpty()) return Float.MAX_VALUE
        var minDst = Float.MAX_VALUE
        for (stroke in echoStrokes) {
            for (seg in stroke.segments) {
                val d = distanceToSegment(point, seg.p1, seg.p2)
                if (d < minDst) {
                    minDst = d
                }
            }
        }
        return minDst
    }

    fun checkCollisionWithEchoStrokes(
        candidate: Segment,
        echoStrokes: List<EchoStroke>,
        endpointTolerance: Float = 8.0f,
        hitboxScale: Float = 1.0f
    ): Segment? {
        if (echoStrokes.isEmpty()) return null
        val effectiveTolerance = if (hitboxScale < 1.0f) endpointTolerance * 1.75f else endpointTolerance
        for (stroke in echoStrokes) {
            for (echo in stroke.segments) {
                val echoToTest = if (hitboxScale < 1.0f) {
                    val midX = (echo.p1.x + echo.p2.x) * 0.5f
                    val midY = (echo.p1.y + echo.p2.y) * 0.5f
                    val p1Shrunk = Point(midX + (echo.p1.x - midX) * hitboxScale, midY + (echo.p1.y - midY) * hitboxScale)
                    val p2Shrunk = Point(midX + (echo.p2.x - midX) * hitboxScale, midY + (echo.p2.y - midY) * hitboxScale)
                    Segment(p1Shrunk, p2Shrunk, echo.fromNodeId, echo.toNodeId)
                } else {
                    echo
                }
                if (doLinesIntersect(candidate.p1, candidate.p2, echoToTest.p1, echoToTest.p2, effectiveTolerance)) {
                    return echo
                }
            }
        }
        return null
    }
}
