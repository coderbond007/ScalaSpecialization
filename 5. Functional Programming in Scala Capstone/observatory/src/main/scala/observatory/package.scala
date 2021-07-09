import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import java.nio.file.Paths
import scala.io.Source

package object observatory {
    type Temperature = Double // °C, introduced in Week 1
    type Distance = Double // °C, introduced in Week 1
    type Year = Int // Calendar year, introduced in Week 1

    val earthRadius = 6378D // km
    val width = 360
    val height = 180

    implicit val spark: SparkSession = SparkSession
            .builder
            .appName("observatory")
            .master("local[*]")
            .getOrCreate

    //    def resourcePath(resource: String): String = Paths.get(getClass.getResource(resource).toURI).toString
    def resourcePath(resource: String): String = Paths.get(getClass.getResource(resource).toURI).toString

    def getStringListFromResource(resource: String): RDD[String] = {
        val lines = Source
                .fromInputStream(
                    Source
                            .getClass
                            .getResourceAsStream(resource)
                )
                .getLines
                .toSeq

        spark.sparkContext.parallelize(lines)
    }

    def getIndex(lat : Int, lon: Int) : Int = {
        val x = lon + 180
        val y = 90 - lat
        y * width + x
    }
}
