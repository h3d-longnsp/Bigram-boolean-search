import scala.io.Source
import java.nio.file.Paths
import java.nio.file.Files
import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import java.util.stream.Collectors.toList
import org.tartarus.snowball.ext.englishStemmer

object Indexer {
  def loadIndex(indexPath: String): Map[String, List[Int]] = {
    // Define a regex pattern to match the key and the list of integers
    val regexIndex = "^(.+?)\\s+(\\d+.*)$".r    
    val lines = Source.fromFile(indexPath, "UTF-8").getLines()
    val index = lines.collect {
      case regexIndex(key, values) => key -> values.split(" ").map(_.toInt).toList
    }.toMap
    index
  }  

  private def stem(stemmer: englishStemmer, tokens: List[String]): Seq[String] = {
    tokens.map { token =>
      stemmer.setCurrent(token)
      stemmer.stem()
      stemmer.getCurrent()
    }
  }

  /**
    * TODO: Add document
    *
    * @param inputPath
    * @return
    */
  def buildVocab(inputPath: String) = {
    val regexNumber = "(?<!\\S)\\S*\\d+\\S*(?!\\S)".r
    val stemmer = new englishStemmer()
    val files = Files.list(Paths.get(inputPath)).collect(toList()).asScala.map(_.getFileName().toString())
    val pairs = files.map { file =>
      val lines = Source.fromFile(inputPath + "/" + file, "ISO-8859-1").getLines().toList
      val tokens = lines.flatMap(line => line.split("\\W+"))
        .map(_.trim())
        .filter(_.nonEmpty)
        .map(_.toLowerCase())
        .map(token => regexNumber.replaceAllIn(token, "[NUM]"))
      (file, stem(stemmer, tokens))
    }.toList
    val unigramVocab = pairs.map(_._2).flatMap(identity).toSet.toList
    val bigramVocab = pairs.flatMap(_._2).sliding(2).toList.distinct.map(_.mkString(" "))

    (pairs, unigramVocab, bigramVocab)
  }

  def buildUnigramIndex(pairs: List[(String, Seq[String])], unigramVocab: List[String]) = {
    val unigramIndex = unigramVocab.map { term =>
      val docIds = pairs.filter(pair => pair._2.contains(term)).map(_._1.toInt)
      (term, docIds)
    }.toMap

    unigramIndex
  }

  /**
    * TODO: Parallelizing this part 
    *
    * @param pairs
    * @param bigramVocab
    * @return
    */
  def buildBigramIndex(pairs: List[(String, Seq[String])], bigramVocab: List[String]) = {
    val bigramIndex = bigramVocab.map { term =>
      val docIds = pairs.filter(pair => pair._2.sliding(2).toList.distinct.map(_.mkString(" ")).contains(term)).map(_._1.toInt)
      (term, docIds)
    }.toMap

    bigramIndex
  }

  def sortIndex(index: Map[String, List[Int]]) = {
    ListMap(index.toSeq.sortBy(_._1):_*)
  }

  def writeOutputToFile(filename: String = "", index: Map[String, List[Int]]): Unit = {
    val content = index.map { case (term, docIds) => term + ' ' + docIds.mkString(" ") }.mkString("\n")
    Files.writeString(Paths.get(filename), content)
  }


  def main(args: Array[String]): Unit = {
    val (pairs, unigramVocabulary, bigramVocabulary) = buildVocab("input/test")

    val unigramIndex = buildUnigramIndex(pairs, unigramVocabulary)
    val bigramIndex = buildBigramIndex(pairs, bigramVocabulary)

    val sortedUnigramIndex = sortIndex(unigramIndex)
    val sortedBigramIndex = sortIndex(bigramIndex)

    writeOutputToFile("output/index-unigram1.txt", sortedUnigramIndex)
    writeOutputToFile("output/index-bigram1.txt", sortedBigramIndex)
    println("done")
  }
}