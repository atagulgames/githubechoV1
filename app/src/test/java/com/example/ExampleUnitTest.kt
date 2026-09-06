package com.example

import com.example.data.LevelCatalog
import com.example.game.CollisionEngine
import com.example.model.Point
import com.example.model.Segment
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAll100LevelsAreSolvableAndUnique() {
    val levels = LevelCatalog.create100Levels()
    assertEquals(100, levels.size)

    val titles = HashSet<String>()
    for (entity in levels) {
      val level = LevelCatalog.entityToLevelData(entity)
      val id = level.levelId

      // Title uniqueness check
      assertFalse("Duplicate title at level $id: ${level.title}", titles.contains(level.title))
      titles.add(level.title)

      // Node count check (strictly between 3 and 36 nodes)
      val nodes = level.nodes
      assertTrue("Level $id has too few nodes: ${nodes.size}", nodes.size >= 3)
      assertTrue("Level $id has too many nodes: ${nodes.size}", nodes.size <= 36)

      // Spacing between nodes
      val minAllowedDist = if (nodes.size > 10) 22f else 35f
      for (i in 0 until nodes.size) {
        for (j in i + 1 until nodes.size) {
          val dist = Point(nodes[i].x, nodes[i].y).distanceTo(Point(nodes[j].x, nodes[j].y))
          assertTrue(
            "Level $id nodes ${nodes[i].id} and ${nodes[j].id} too close: dist=$dist (min=$minAllowedDist)",
            dist >= minAllowedDist
          )
        }
      }

      // Check straight-line path (1->2->...->N) self-intersection
      val segments = mutableListOf<Segment>()
      for (i in 0 until nodes.size - 1) {
        val s = Segment(Point(nodes[i].x, nodes[i].y), Point(nodes[i + 1].x, nodes[i + 1].y), nodes[i].id, nodes[i + 1].id)
        for (past in segments.dropLast(1)) {
          val intersect = CollisionEngine.doLinesIntersect(
            s.p1, s.p2, past.p1, past.p2, endpointTolerance = 8f
          )
          assertFalse("Level $id has self-intersecting solution path between ${s.fromNodeId}->${s.toNodeId} and ${past.fromNodeId}->${past.toNodeId}", intersect)
        }
        segments.add(s)
      }
    }
  }
}

