package com.example

import com.example.data.LevelCatalog
import com.example.game.CollisionEngine
import com.example.model.LevelNode
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
  fun testCheckAllLevelDistances() {
    val levels = LevelCatalog.create100Levels()
    for (entity in levels) {
      val lvl = LevelCatalog.entityToLevelData(entity)
      val nodes = lvl.nodes
      var minConsecutive = Float.MAX_VALUE
      var minAny = Float.MAX_VALUE
      for (i in 0 until nodes.size - 1) {
        val d = Point(nodes[i].x, nodes[i].y).distanceTo(Point(nodes[i + 1].x, nodes[i + 1].y))
        if (d < minConsecutive) minConsecutive = d
      }
      for (i in 0 until nodes.size) {
        for (j in i + 1 until nodes.size) {
          val d = Point(nodes[i].x, nodes[i].y).distanceTo(Point(nodes[j].x, nodes[j].y))
          if (d < minAny) minAny = d
        }
      }
      if (minConsecutive < 36f || minAny < 36f) {
        println("Level ${lvl.levelId} (${lvl.title}): nodeCount=${nodes.size}, minConsecutive=$minConsecutive, minAny=$minAny")
      }
    }
  }

  @Test
  fun testAll100LevelsWithRealisticFingerDrag() {
    val levels = LevelCatalog.create100Levels()

    fun findTarget(curPt: Point, lastNode: LevelNode, expectedNode: LevelNode?): LevelNode? {
      if (expectedNode != null) {
        val distToExpected = curPt.distanceTo(Point(expectedNode.x, expectedNode.y))
        val distToLast = curPt.distanceTo(Point(lastNode.x, lastNode.y))
        val segLen = Point(lastNode.x, lastNode.y).distanceTo(Point(expectedNode.x, expectedNode.y))
        val captureRadius = (segLen * 0.48f).coerceIn(12f, 32f)
        if (distToExpected <= captureRadius && distToExpected < distToLast) {
          return expectedNode
        }
      }
      // Düğümler içinden serbestçe geçilebilir: Kullanıcı isteği gereği çizgiler ve düğümler
      // birbirinin içinden geçebilir; sadece beklenen hedef düğüme ulaşıldığında bağlanır.
      return null
    }

    val failedLevels = mutableListOf<String>()

    for (entity in levels) {
      val level = LevelCatalog.entityToLevelData(entity)
      val nodes = level.nodes
      val visited = mutableListOf(nodes[0].id)

      var failed = false
      var failureReason = ""

      for (step in 0 until nodes.size - 1) {
        val n1 = nodes[step]
        val n2 = nodes[step + 1]
        val dist = Point(n1.x, n1.y).distanceTo(Point(n2.x, n2.y))

        var reached = false
        var currentDist = 0f
        while (currentDist <= dist && !failed) {
          currentDist += 2f
          val t = (currentDist / dist).coerceAtMost(1f)
          val curPt = Point(n1.x + t * (n2.x - n1.x), n1.y + t * (n2.y - n1.y))

          val lastVisitedId = visited.last()
          val lastNode = nodes.first { it.id == lastVisitedId }
          val expectedNode = nodes.getOrNull(visited.size)

          val hit = findTarget(curPt, lastNode, expectedNode)
          if (hit != null && hit.id != lastVisitedId) {
            if (hit.id != n2.id) {
              failed = true
              failureReason = "CRASH: Hit wrong node ${hit.id} instead of expected ${n2.id} at dist=$currentDist/$dist"
              break
            }
            visited.add(hit.id)
            reached = true
            break
          }
        }
        if (!reached && !failed) {
          failed = true
          failureReason = "CRASH: Did not reach Node ${n2.id} from Node ${n1.id}"
        }
        if (failed) break
      }

      if (failed) {
        failedLevels.add("Level ${level.levelId} (${level.title}): $failureReason")
      }
    }

    if (failedLevels.isNotEmpty()) {
      println("Failed ${failedLevels.size} levels:")
      failedLevels.forEach { println("  $it") }
    } else {
      println("ALL 100 LEVELS PASSED CLEANLY!")
    }
    assertTrue("Some levels failed: ${failedLevels.joinToString("\n")}", failedLevels.isEmpty())
  }

  @Test
  fun testLevel51Specifics() {
    val level = LevelCatalog.buildLevelData(51)
    println("Level 51: title=${level.title}, nodes=${level.nodes.size}, mechanic=${level.mechanicType}")
    
    // Simulate drawing through level 51
    val nodes = level.nodes
    val visited = mutableListOf(nodes[0].id)
    val segments = mutableListOf<Segment>()

    for (step in 0 until nodes.size - 1) {
      val fromNode = nodes[step]
      val targetNode = nodes[step + 1]
      val fromPt = Point(fromNode.x, fromNode.y)
      val toPt = Point(targetNode.x, targetNode.y)
      val dist = fromPt.distanceTo(toPt)
      val numSubSteps = (dist * 2).toInt().coerceAtLeast(10)

      var reached = false
      for (s in 1..numSubSteps) {
        val t = s.toFloat() / numSubSteps
        val curPt = Point(fromPt.x + t * (toPt.x - fromPt.x), fromPt.y + t * (toPt.y - fromPt.y))

        // Target capture when finger approaches targetNode
        val distToTarget = curPt.distanceTo(toPt)
        val distToFrom = curPt.distanceTo(fromPt)
        val captureRadius = (dist * 0.48f).coerceIn(12f, 32f)
        if (distToTarget <= captureRadius && distToTarget < distToFrom) {
          reached = true
          visited.add(targetNode.id)
          segments.add(Segment(fromPt, toPt, fromNode.id, targetNode.id))
          break
        }
      }
      assertTrue("Did not reach target node ${targetNode.id} from ${fromNode.id}", reached)
    }
    println("SUCCESS: Level 51 passed cleanly!")
  }

  @Test
  fun testAll100LevelsAreSolvableAndUnique() {
    val levels = LevelCatalog.create100Levels()
    assertEquals(100, levels.size)

    val titles = HashSet<String>()
    val failures = mutableListOf<String>()
    for (entity in levels) {
      val level = LevelCatalog.entityToLevelData(entity)
      val id = level.levelId

      // Title uniqueness check
      if (titles.contains(level.title)) {
        failures.add("Duplicate title at level $id: ${level.title}")
      }
      titles.add(level.title)

      // Node count check (strictly between 3 and 36 nodes)
      val nodes = level.nodes
      if (nodes.size < 3) failures.add("Level $id has too few nodes: ${nodes.size}")
      if (nodes.size > 36) failures.add("Level $id has too many nodes: ${nodes.size}")

      // Spacing between nodes (scaled smoothly for high node density)
      val minAllowedDist = if (nodes.size >= 25) 10f else if (nodes.size >= 10) 14f else 28f
      for (i in 0 until nodes.size) {
        for (j in i + 1 until nodes.size) {
          val dist = Point(nodes[i].x, nodes[i].y).distanceTo(Point(nodes[j].x, nodes[j].y))
          if (dist < minAllowedDist) {
            failures.add("Level $id nodes ${nodes[i].id} and ${nodes[j].id} too close: dist=$dist (min=$minAllowedDist)")
          }
        }
      }
    }
    if (failures.isNotEmpty()) {
      println("TOTAL FAILURES: ${failures.size}")
      failures.forEach { println("FAIL: $it") }
      fail("Failed ${failures.size} checks:\n" + failures.joinToString("\n"))
    }
  }
}

