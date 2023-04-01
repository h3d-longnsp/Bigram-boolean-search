package org.long.A4

import org.tartarus.snowball.ext.englishStemmer

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors.toList
import scala.collection.JavaConverters._
import scala.collection.parallel.CollectionConverters._
import scala.io.Source

object Indexer {

  /**
    * Take a `englishStemmer` object and a list of tokens as parameters.
    * Return a `List` of stemmed tokens.
    *
    * @param stemmer An english stemmer object
    * @param tokens A list of strings
    * @return A list of stemmed token
    * @example 
    * <pre>
    * val stemmer = new englishStemmer
    * val tokens = List("running", "ran", "runs")
    * val stemmedTokens = stem(stemmer, tokens)
    * stemmedTokens // List("run", "ran", "run")
    * </pre>
    */
  private def stem(stemmer: englishStemmer, tokens: List[String]): Seq[String] = {
    tokens.map { token =>
      stemmer.setCurrent(token)
      stemmer.stem()
      stemmer.getCurrent()
    }
  }

  /**
    * Take a `String` file path and return a `Tuple3` containing a list of file pairs, 
    * a list of unigram vocabularies and a list of bigram vocabularies. 
    * 
    * The file pairs are created by looping through a list of files in 
    * the given directory and tokenizing the lines of each file. The tokens are 
    * then stemmed, lowercased and numbers are replaced with `[NUM]`.
    * 
    * The vocabularies are created by flattening the list of 
    * file pairs, and in the bigram, sliding the tokens into pairs.
    *
    * @param inputPath A `String` representing a file path to input files.
    * @return A `Tuple3` containing a list of file pairs, a list of unigram vocabulary
    *  and a list of bigram vocabulary.
    * @example 
    * <pre>
    * // input folder has a file named "10000" with content "Tom loves Jerry"
    * val (pairs, unigramVocab, bigramVocab) = buildVocab("/path/to/input/directory")
    * pairs // List(("10000", Seq("tom", "love", "jerry")))
    * unigramVocab // List("tom", "love", "jerry")
    * bigramVocab // List("tom love", "love jerry")
    * </pre>
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

  /**
    * Build an unigram index from a list of pairs and a list of unigram vocabulary.
    * 
    * **Algorithm**
    * Create an empty map `unigramIndex`
    * For each term in `unigramVocab`:
    * * Find all pairs in `pairs` where the second element contains the term.
    * * Extract the first element of each pair and convert it to an integer.
    * * Add an entry to `unigramIndex` with the term as the key and 
    * the list of document IDs as the value, sorted in ascending order.
    *
    * @param pairs A list of pairs, where each pair is a doc ID `String` and a sequence of terms
    * @param unigramVocab A list of terms
    * @return A `Map` of terms to a list of document IDs, sorted in ascending order.
    */
  def buildUnigramIndex(pairs: List[(String, Seq[String])], unigramVocab: List[String]) = {
    val unigramIndex = unigramVocab.map { term =>
      val docIds = pairs.filter(pair => pair._2.contains(term)).map(_._1.toInt)
      (term, docIds.sorted)
    }.toMap

    unigramIndex
  }

  /**
    * Build an bigram index from a list of pairs and a list of bigram vocabulary.
    * 
    * Optimize the `buildBigramIndex` function by pre-process the input data by constructing 
    * a `Map` that maps each term to the set of document IDs that contain that term. 
    * This way, we can avoid calling expensive `sliding` and `distinct` on each sequence in the input data 
    * for each term in `bigramVocab`.
    *
    * @param pairs A list of pairs, where each pair is a doc ID `String` and a sequence of terms
    * @param bigramVocab A list of terms
    * @return A `Map` of terms to a list of document IDs, sorted in ascending order.
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

  /**
    * Load a selected index file
    *
    * @param indexPath The path to index file
    * @return A `Map` of type `String` to `List[Int]`
    */
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

  /**
    * Write the given index to a text file
    *
    * @param filename The path of the file to write to
    * @param index A `Map` of type `String` to `List[Int]`
    */
  def writeOutputToFile(filename: String, index: Map[String, List[Int]]): Unit = {
    val content = index.map { case (term, docIds) => term + ' ' + docIds.sorted.mkString(" ") }.mkString("\n")
    Files.writeString(Paths.get(filename), content)
  }

  def main(args: Array[String]): Unit = {
    // val (pairs, unigramVocabulary, bigramVocabulary) = buildVocab("input/reuters-test")

    // val unigramIndex = buildUnigramIndex(pairs, unigramVocabulary)
    // val bigramIndex = buildBigramIndex(pairs, bigramVocabulary)

    // val sortedUnigramIndex = Utils.sortIndex(unigramIndex)
    // val sortedBigramIndex = Utils.sortIndex(bigramIndex)

    // writeOutputToFile("output/index-unigram2.txt", sortedUnigramIndex)
    // writeOutputToFile("output/index-bigram2.txt", sortedBigramIndex)
    // println("done")
    val engStemmer = new englishStemmer()
    val tokens = List("running","ran","runs")
    println(stem(engStemmer,tokens))
  }
}