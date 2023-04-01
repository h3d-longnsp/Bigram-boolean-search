package org.long.A4

import scala.io.Source
import java.nio.file.Paths
import java.nio.file.Files
import scala.collection.JavaConverters._
import java.util.stream.Collectors.toList
import org.tartarus.snowball.ext.englishStemmer
import scala.collection.parallel.CollectionConverters._

object Indexer {
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
      (term, docIds.sorted)
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
    // Construct a map that maps each term to the set of document IDs that contain that term
    val termToDocIds = pairs.flatMap { case (docId, sequence) =>
      sequence.sliding(2).map(_.mkString(" ")).distinct.map(term => (term, docId.toInt))
    }.groupBy(_._1).mapValues(_.map(_._2).toList.sorted)

    // Use the pre-processed data to construct the bigram index
    val bigramIndex = bigramVocab.par.map { term =>
      val docIds = termToDocIds.getOrElse(term, Nil)
      (term, docIds)
    }.toMap

    bigramIndex.seq
  }

  def loadIndex(indexPath: String): Map[String, List[Int]] = {
    // Define a regex pattern to match the key and the list of integers
    val regexIndex = "^(.+?)\\s+(\\d+.*)$".r
    // Wrap the Source.fromFile call in a BufferedSource to reduce the number of system calls made to read the file
    val lines = Source.fromFile(indexPath, "UTF-8").getLines().buffered
    val index = lines.collect {
      case regexIndex(key, values) => key -> values.split(" ").map(_.toInt).toList
    }.toMap
    index
  }    

  def writeOutputToFile(filename: String = "", index: Map[String, List[Int]]): Unit = {
    val content = index.map { case (term, docIds) => term + ' ' + docIds.sorted.mkString(" ") }.mkString("\n")
    Files.writeString(Paths.get(filename), content)
  }

  def main(args: Array[String]): Unit = {
    val (pairs, unigramVocabulary, bigramVocabulary) = buildVocab("input/reuters-test")

    val unigramIndex = buildUnigramIndex(pairs, unigramVocabulary)
    val bigramIndex = buildBigramIndex(pairs, bigramVocabulary)

    val sortedUnigramIndex = Utils.sortIndex(unigramIndex)
    val sortedBigramIndex = Utils.sortIndex(bigramIndex)

    writeOutputToFile("output/index-unigram2.txt", sortedUnigramIndex)
    writeOutputToFile("output/index-bigram2.txt", sortedBigramIndex)
    println("done")
  }
}