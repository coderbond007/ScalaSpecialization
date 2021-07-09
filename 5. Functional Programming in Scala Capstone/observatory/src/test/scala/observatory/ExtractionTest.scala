package observatory

trait ExtractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  // Implement tests for the methods of the `Extraction` object

  "this test" should "load data in csv format" in {
    for(year <- 1975 to 1975){
      Extraction.locateTemperatures(year, "/stations.csv", "/" + year + ".csv")
    }

    print("gg")
  }
}
