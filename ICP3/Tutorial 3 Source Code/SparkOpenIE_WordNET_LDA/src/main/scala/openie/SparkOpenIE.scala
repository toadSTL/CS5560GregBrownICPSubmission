package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.io._
/**
  * Created by Mayanka on 27-Jun-16.
  */
object SparkOpenIE {

  def main(args: Array[String]) {
    // Configuration
    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    // For Windows Users
    System.setProperty("hadoop.home.dir", "D:\\winutils")


    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    var i = 0
    for (i <- 1 to 10)
    {
    val input = sc.textFile("data\\"+i+".txt").map(line => {
      //Getting OpenIE Form of the word using lda.CoreNLP
      //val sentance=line.split(". ")
      val t = CoreNLP.returnTriplets(line)
      t
    })


    val pw = new PrintWriter(new File("data\\triplet" + i + ".txt"))
    pw.write(input.collect().mkString("\n\n***\n\n"))
    pw.close
  }
    //input.foreach(println)
    //println(input.collect().mkString("\n"))
    //open the abstract files, and iterate over them writing into a new file the results.
    //println(CoreNLP.returnTriplets("The dog has a lifespan of upto 10-12 years."))
    //println()



  }

}
