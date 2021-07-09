package observatory

import org.apache.spark.rdd.RDD

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {

    /**
      * @param temperatures Known temperatures
      * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
      *         returns the predicted temperature at this location
      */
    def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
        val grid: Grid = Grid.fromIterable(temperatures)
        grid.asFunction()
    }

    /**
      * @param temperatures Sequence of known temperatures over the years (each element of the collection
      *                     is a collection of pairs of location and temperature)
      * @return A function that, given a latitude and a longitude, returns the average temperature at this location
      */
    def average(temperatures: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
        // Generate a grid for each year
        val gridPairs: Iterable[(Grid, Int)] = for {
            temps <- temperatures
        } yield (Grid.fromIterable(temps), 1)

        val reduced = gridPairs.reduce(mergeArrayPairs)

        val meanGrid: Grid = reduced match {
            case (grid, count) => grid.map(_ / count)
        }

        meanGrid.asFunction()
    }

    /**
      * @param temperatures Known temperatures
      * @param normals      A grid containing the “normal” temperatures
      * @return A grid containing the deviations compared to the normal temperatures
      */
    def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
        val grid = makeGrid(temperatures)
        gridLocation: GridLocation => grid(gridLocation) - normals(gridLocation)
    }

    /** -----------------------------------------------------------------------
      * Spark implementation used by observation.Main
      */

    /**
      * Spark implementation of the averaging function
      */
    def averageGridRDD(temperatures: RDD[Grid]): Grid = {
        println(s"OBSERVATORY: Averaging grid")
        val reduced: (Grid, Int) = temperatures.map((_, 1)).reduce(
            (p1: (Grid, Int), p2: (Grid, Int)) => mergeArrayPairs(p1, p2)
        )
        reduced match {
            case (grid, count) => grid.scale(1.0 / count)
        }
    }

    /** -----------------------------------------------------------------------
      * Utility functions
      */

    def mergeArrayPairs(p1: (Grid, Int), p2: (Grid, Int)): (Grid, Int) = {
        (p1, p2) match {
            case ((g1, c1), (g2, c2)) => (g1.add(g2), c1 + c2)
        }
    }
}

/**
  * Grid abstracts a 2-D array of doubles for a global grid at 1-degree resolution.
  * Methods are provided for point-wise arithmetic.  Only those methods required for the application are implemented.
  */
class Grid extends Serializable {
    val buffer: Array[Temperature] = new Array[Temperature](width * height)

    /**
      * Convert to a function between (lat, lon) and value.  This is required by the grader.
      */
    def asFunction(): GridLocation => Temperature = {
        gridLocation: GridLocation => {
            buffer(getIndex(gridLocation.lat, gridLocation.lon))
        }
    }

    // Add grids
    def add(grid: Grid): Grid = {
        val newGrid = new Grid()
        for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) + grid.buffer(i)}
        newGrid
    }

    // diff grids
    def diff(grid: Grid): Grid = {
        val newGrid = new Grid()
        for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) - grid.buffer(i)}
        newGrid
    }

    // scale grids
    def scale(factor: Temperature): Grid = {
        val newGrid = new Grid()
        for (i <- 0 until width * height) { newGrid.buffer(i) = this.buffer(i) * factor }
        newGrid
    }

    // map a function over all points
    def map(f: Temperature => Temperature): Grid = {
        val newGrid = new Grid()
        for (i <- 0 until width * height) { newGrid.buffer(i) = f(this.buffer(i)) }
        newGrid
    }

}

object Grid {
    /**
      * Generate a grid using spatial interpolation from an iterable of locations and values.
      */
    def fromIterable(temperatures: Iterable[(Location, Temperature)]): Grid = {
        val grid = new Grid()

        val coord = for {
            i <- Range(90, -90, -1)
            j <- Range(-180, 180, 1)
        } yield (i, j)
        coord
                .foreach(cur => grid.buffer(getIndex(cur._1, cur._2)) = Visualization.idw(temperatures, Location(cur._1, cur._2), Visualization.P))
//        for (y <- 0 until grid.height) {
//            for (x <- 0 until grid.width) {
//                val loc = Location(y - (grid.height / 2), x - (grid.width / 2))
//                grid.buffer(y * width + x) = Visualization.idw(temperatures, loc, Visualization.P)
//            }
//        }
        grid
    }


}

