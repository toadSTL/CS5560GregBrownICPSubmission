package openie

import java.io.PrintStream

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Mayanka on 27-Jun-16.
  */
object SparkOpenIE {

  def main(args: Array[String]) {

    // For Windows Users
    System.setProperty("hadoop.home.dir", "D:\\winutils")


    // Configuration
    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)



    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val input = sc.wholeTextFiles("data").map(line => {
      //Getting OpenIE Form of the word using lda.CoreNLP
      var line2=line.replaceAll("[,]"," ")
     // line2=line2.replace("[]","")
      val t=CoreNLP.returnTriplets(line2)
      t
    }).collect()
    val out= new PrintStream("TripletList.csv")
    input.foreach(f=>{
      out.println(f)
    })
out.close()

/*
    println(CoreNLP.returnTriplets("The dog has a lifespan of upto 10-12 years."))
   println(input.collect().mkString("\n"))



    // StopWords
    val stopwords=sc.textFile("data/stopwords.txt").collect()
    val stopwordBroadCast=sc.broadcast(stopwords)

    val input2= sc.textFile("data/sentenceSample").map(f=>{
      val afterStopWordRemoval=f.split(" ").filter(!stopwordBroadCast.value.contains(_))
    })*/

  }

}
