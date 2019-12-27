package com.libao.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;

public class App 
{
    public static void main( String[] args )
    {
        SparkSession spark = SparkSession
                .builder()
                //.appName("JavaSparkSQLDemo")
                .getOrCreate();
        Dataset<Row> df = spark.read().json("file:///usr/hdp/2.6.5.3003-25/spark2/examples/src/main/resources/people.json");

        df.show();

        df.printSchema();

        df.select("name").show();


        df.select(col("name"), col("age").plus(1)).show();

        df.filter(col("age").gt(21)).show();

        df.groupBy("age").count().show();
    }
}
