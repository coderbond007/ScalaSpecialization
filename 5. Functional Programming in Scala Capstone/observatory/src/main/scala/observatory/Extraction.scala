
package observatory

import observatory.spark.implicits._
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructType}
import org.apache.spark.sql.{DataFrame, Dataset}

import java.time.LocalDate

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

    def convertSeqToDataFrame(lines: Dataset[String], structType: StructType): DataFrame = {
        val dataframe = spark
                .read
                .schema(structType)
                .csv(lines)
        dataframe
    }

    def locateTemperaturesSpark(year: Year, stationsFile: String, temperaturesFile: String): Dataset[((Int, Int, Int), Location, Temperature)] = {
        val stations = convertSeqToDataFrame(getStringListFromResource(stationsFile).toDS(),
            new StructType()
                    .add("stn", IntegerType, nullable = true)
                    .add("wban", IntegerType, nullable = true)
                    .add("latitude", DoubleType, nullable = true)
                    .add("longitude", DoubleType, nullable = true)
        )
        val temperatures = convertSeqToDataFrame(getStringListFromResource(temperaturesFile).toDS(),
            new StructType()
                    .add("stn", IntegerType, nullable = true)
                    .add("wban", IntegerType, nullable = true)
                    .add("month", IntegerType, nullable = true)
                    .add("day", IntegerType, nullable = true)
                    .add("temperature", DoubleType, nullable = true)
        )
        locateTemperaturesSpark(year, stations, temperatures)
    }

    def locateTemperaturesSpark(year: Year, stations: DataFrame, temperatures: DataFrame): Dataset[((Int, Int, Int), Location, Temperature)] = {
        val stationsFiltered = stations
                .select(
                    'stn, // 0
                    'wban, // 1
                    'latitude, // 2
                    'longitude // 3
                )
                .where('latitude.isNotNull && 'longitude.isNotNull && 'latitude =!= 0 && 'longitude =!= 0)
        val temperaturesFiltered = temperatures
                .select(
                    'stn, // 4
                    'wban, // 5
                    lit(year).alias("year"), // 6
                    'month, // 7
                    'day, // 8
                    'temperature // 9
                )

        stationsFiltered
                .join(temperaturesFiltered, Seq("stn", "wban"))
                .map(cur => (
                        (
                                cur(4).toString.toInt,
                                cur(5).toString.toInt,
                                cur(6).toString.toInt
                        ),
                        Location(
                            cur(2).toString.toDouble,
                            cur(3).toString.toDouble
                        ),
                        (cur(7).toString.toDouble - 32) / 1.8)
                )
    }

    /**
      * @param year             Year number
      * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
      * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
      * @return A sequence containing triplets (date, location, temperature)
      */
    def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
        locateTemperaturesSpark(year, stationsFile, temperaturesFile)
                .collect
                .map(cur => (LocalDate.of(cur._1._1, cur._1._2, cur._1._3), cur._2, cur._3))
                .toSeq
    }

    /**
      * @param records A sequence containing triplets (date, location, temperature)
      * @return A sequence containing, for each location, the average temperature over the year.
      */
    def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
        records
                .par
                .groupBy(_._2)
                .mapValues(x => x.map(_._3).sum / x.size)
                .toList
    }
}