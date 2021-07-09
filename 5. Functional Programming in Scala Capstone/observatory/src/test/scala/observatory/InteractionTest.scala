package observatory

import observatory.Interaction.tileLocation

trait InteractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("interactive visualization", 3) _
  "tileLocation test" should "" in {
    val tile = Tile(0, 0, 0)
    val computed = tileLocation(tile)
    val expected = Location(85.0511, -180)

    assert(computed === expected)
  }
}
