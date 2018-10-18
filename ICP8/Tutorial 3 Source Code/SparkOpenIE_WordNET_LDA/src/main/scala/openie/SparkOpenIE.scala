package openie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import java.io._
import scala.io.Source
import scala.collection.mutable.ListBuffer
/**
  * Created by Mayanka on 27-Jun-16.
  */
object SparkOpenIE {

  def main(args: Array[String]) {
    // Configuration
    System.setProperty("hadoop.home.dir", "D:\\winutils")

    val sparkConf = new SparkConf().setAppName("SparkWordCount").setMaster("local[*]")

    val sc = new SparkContext(sparkConf)

    // For Windows Users



    // Turn off Info Logger for Console
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val input = sc.wholeTextFiles("data")

    val sentances = input.flatMap(doc => {
      val sen = doc._2.split("\\. ")
      sen
    })

    val triplets = input.flatMap(abs => {
      val t = CoreNLP.returnTriplets(abs._2)
      //println(t)
      //val c = t.replaceAll("\\[\\(|\\)\\]", "")
      val a = t.split("\\), \\(|\\)\\]\\[\\(")
      a
    }).map(trip => {
      val c = trip.replaceAll("\\)\\]", "").replaceAll("\\[\\(", "")
      val t = c.split(",")
      (t(0),t(1),t(2))
      //t
    })

    //println(sentances.collect().mkString("\n"))

   // val ret = sentances.flatMap(line => {
  //    val t = CoreNLP.returnTriplets(line)
      //println(t)
 //     val c = t.replaceAll("\\[\\(|\\)\\]", "")
//
     // val a = c.split("\\), \\(")
    //  //a.foreach(println)
    //  a
    //})

    //val triplets = ret.map(triplet => {
    //  val arr = triplet.split("")
    //})

    val subjects = triplets.map(t => t._1).distinct()
    val predicates = triplets.map(t => t._2).distinct()
    val objects = triplets.map(t => t._3).distinct()

    val predOut = predicates.map(s => prepString(s))

    val subOut = subjects.map(s => prepString(s))

    val objOut = objects.map(s => prepString(s))

    val tripletsOut = triplets.map( s => {
      val ret = prepString(s._1) + "," + prepString(s._2) + "," + prepString(s._3)
      ret
    })

    val pwPred = new PrintWriter(new File("output\\Predicates.txt"))
    pwPred.write(predOut.collect().mkString("\n"))
    pwPred.close

    val pwSub = new PrintWriter(new File("output\\Subjects.txt"))
    pwSub.write(subOut.collect().mkString("\n"))
    pwSub.close

    val pwObj = new PrintWriter(new File("output\\Objects.txt"))
    pwObj.write(objOut.collect().mkString("\n"))
    pwObj.close

    val pwTrip = new PrintWriter(new File("output\\Triplets.txt"))
    pwTrip.write(tripletsOut.collect().mkString("\n"))
    pwTrip.close

    if (args.length < 2) {
      System.out.println("\n$ java RESTClientGet [Bioconcept] [Inputfile] [Format]")
      System.out.println("\nBioconcept: We support five kinds of bioconcepts, i.e., Gene, Disease, Chemical, Species, Mutation. When 'BioConcept' is used, all five are included.\n\tInputfile: a file with a pmid list\n\tFormat: PubTator (tab-delimited text file), BioC (xml), and JSON\n\n")
    }
    else {
      val Bioconcept = args(0)
      val Inputfile = args(1)
      var Format = "PubTator"
      if (args.length > 2) {
        Format = args(2)
      }
      val medWords = ListBuffer.empty[(String, String)]

      for (line <- Source.fromFile(Inputfile).getLines) {
        val data = scala.io.Source.fromURL("https://www.ncbi.nlm.nih.gov/CBBresearch/Lu/Demo/RESTful/tmTool.cgi/" + Bioconcept + "/" + line + "/" + Format + "/").getLines()

        val lines = data.flatMap(line => {
          line.split("\n")
        }).drop(2)
        //lines.foreach{l =>print(l+"\n")}

        val words = lines.flatMap(word => {
          word.split("\t").drop(3).dropRight(1)
        }).toArray
        //words.foreach{w =>print(w+"\n")}
        for (i <- 0 until words.length by 2) {
          if (i < words.length - 1) {
            val work = (words(i), words(i + 1))
            medWords += work
          }
        }
      }
      val mw = sc.parallelize(medWords.toList).distinct()

      val disW = mw.filter(w => w._2.equals("Disease")).map(w => w._1).collect.toSet
      val specW = mw.filter(w => w._2.equals("Species")).map(w => w._1).collect.toSet
      val geneW = mw.filter(w => w._2.equals("Gene")).map(w => w._1).collect.toSet
      val chemW = mw.filter(w => w._2.equals("Chemical")).map(w => w._1).collect.toSet
      val mutW = mw.filter(w => w._2.equals("Mutation")).map(w => w._1).collect.toSet

      val subDis = subjects.filter(s => {
        disW.contains(s)
      }).map(s => "Disease,"+prepString(s))
      val subSpec = subjects.filter(s => {
        specW.contains(s)
      }).map(s => "Species,"+prepString(s))
      val subGene = subjects.filter(s => {
        geneW.contains(s)
      }).map(s => "Gene,"+prepString(s))
      val subChem = subjects.filter(s => {
        chemW.contains(s)
      }).map(s => "Chemical,"+prepString(s))
      val subMut = subjects.filter(s => {
        mutW.contains(s)
      }).map(s => "Mutation,"+prepString(s))
      val subOth = subjects.filter(s => (!disW.contains(s))&&(!specW.contains(s))&&(!geneW.contains(s))&&(!chemW.contains(s))&&(!mutW.contains(s))).map(s => "Other,"+prepString(s))

      val pwSubDis = new PrintWriter(new File("output\\SubjectDisease.txt"))
      pwSubDis.write(subDis.collect().mkString("\n"))
      pwSubDis.close

      val pwSubSpec = new PrintWriter(new File("output\\SubjectSpecies.txt"))
      pwSubSpec.write(subSpec.collect().mkString("\n"))
      pwSubSpec.close

      val pwSubChem = new PrintWriter(new File("output\\SubjectChemical.txt"))
      pwSubChem.write(subChem.collect().mkString("\n"))
      pwSubChem.close

      val pwSubGene = new PrintWriter(new File("output\\SubjectGene.txt"))
      pwSubGene.write(subGene.collect().mkString("\n"))
      pwSubGene.close

      val pwSubMut = new PrintWriter(new File("output\\SubjectMutation.txt"))
      pwSubMut.write(subMut.collect().mkString("\n"))
      pwSubMut.close

      val pwSubOth = new PrintWriter(new File("output\\SubjectOther.txt"))
      pwSubOth.write(subOth.collect().mkString("\n"))
      pwSubOth.close

      val objDis = objects.filter(s => {
        disW.contains(s)
      }).map(s => "Disease,"+prepString(s))
      val objSpec = objects.filter(s => {
        specW.contains(s)
      }).map(s => "Species,"+prepString(s))
      val objGene = objects.filter(s => {
        geneW.contains(s)
      }).map(s => "Gene,"+prepString(s))
      val objChem = objects.filter(s => {
        chemW.contains(s)
      }).map(s => "Chemical,"+prepString(s))
      val objMut = objects.filter(s => {
        mutW.contains(s)
      }).map(s => "Mutation,"+prepString(s))
      val objOth = objects.filter(s => (!disW.contains(s))&&(!specW.contains(s))&&(!geneW.contains(s))&&(!chemW.contains(s))&&(!mutW.contains(s))).map(s => "Other,"+prepString(s))

      val pwObjDis = new PrintWriter(new File("output\\objectDisease.txt"))
      pwObjDis.write(objDis.collect().mkString("\n"))
      pwObjDis.close

      val pwObjSpec = new PrintWriter(new File("output\\objectSpecies.txt"))
      pwObjSpec.write(objSpec.collect().mkString("\n"))
      pwObjSpec.close

      val pwObjChem = new PrintWriter(new File("output\\objectChemical.txt"))
      pwObjChem.write(objChem.collect().mkString("\n"))
      pwObjChem.close

      val pwObjGene = new PrintWriter(new File("output\\objectGene.txt"))
      pwObjGene.write(objGene.collect().mkString("\n"))
      pwObjGene.close

      val pwObjMut = new PrintWriter(new File("output\\objectMutation.txt"))
      pwObjMut.write(objMut.collect().mkString("\n"))
      pwObjMut.close

      val pwObjOth = new PrintWriter(new File("output\\objectOther.txt"))
      pwObjOth.write(objOth.collect().mkString("\n"))
      pwObjOth.close

    }



    //      //\w{1,2}\b --> do we really want to remove all words with 1 or 2 characters?
  }

  def prepString(s: String): String = {
    var temp = s
    if(s.contains(" ")) {
      val words = s.split(" ")
      for(i <- 1 until words.length)
      {
        words(i) = words(i).capitalize
      }
      temp = words.mkString("").replaceAll("[.]", "")
    }
    temp
  }


}
