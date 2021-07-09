package observatory

import org.scalatest.flatspec.AnyFlatSpec

class CapstoneSuite
  extends ExtractionTest
    with VisualizationTest
    with InteractionTest
    with ManipulationTest
    with Visualization2Test
    with Interaction2Test

trait MilestoneSuite extends AnyFlatSpec{
//    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  def namedMilestoneTest(milestoneName: String, level: Int)(block: => Unit): Unit =
    if (Grading.milestone >= level) {
      block
    } else {
      fail(s"Milestone $level ($milestoneName) is disabled. To enable it, set the 'Grading.milestone' value to '$level'.")
    }

}

