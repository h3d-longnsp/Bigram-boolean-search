package org.long.A4

import scala.math._

object RankedSearch {
  // Document class to store document information
  case class Document(id: String, text: Seq[String])

  // Calculate term frequency for a document
  def termFrequency(term: String, document: Document): Double = {
    document.text.count(_ == term.toLowerCase).toDouble
  }

  // Calculate inverse document frequency for a term
  def inverseDocumentFrequency(
      term: String,
      index: Map[String, Seq[Int]],
      numDocuments: Int
  ): Double = {
    val docsWithTerm = index.getOrElse(term.toLowerCase, Set.empty[String]).size
    log10(numDocuments.toDouble / (1.0 + docsWithTerm))
  }

  // Calculate tf-idf weight for a term in a document
  def tfIdf(
      term: String,
      document: Document,
      index: Map[String, Seq[Int]],
      numDocuments: Int
  ): Double = {
    termFrequency(term, document) * inverseDocumentFrequency(
      term,
      index,
      numDocuments
    )
  }

  // Calculate tf-idf vector for a document
  def tfIdfVector(
      document: Document,
      index: Map[String, Seq[Int]],
      numDocuments: Int
  ): Map[String, Double] = {
    val terms = document.text.distinct
    terms.map(term => (term, tfIdf(term, document, index, numDocuments))).toMap
  }

  // Calculate tf-idf vector for a query
  def queryVector(
      query: String,
      index: Map[String, Seq[Int]],
      numDocuments: Int
  ): Map[String, Double] = {
    val terms = query.split("\\W+").distinct
    terms
      .map(term => (term, inverseDocumentFrequency(term, index, numDocuments)))
      .toMap
  }

  // Compute cosine similarity between two vectors
  def cosineSimilarity(
      vector1: Map[String, Double],
      vector2: Map[String, Double]
  ): Double = {
    val dotProduct = vector1.keys
      .filter(vector2.contains)
      .map(term => vector1(term) * vector2(term))
      .sum
    val magnitude1 = sqrt(vector1.values.map(x => x * x).sum)
    val magnitude2 = sqrt(vector2.values.map(x => x * x).sum)
    dotProduct / (magnitude1 * magnitude2)
  }

  // Perform ranked retrieval based on query and documents
  def rankDocuments(
      query: String,
      documents: Seq[Document],
      index: Map[String, Seq[Int]],
      numDocuments: Int,
      numResults: Int
  ): Seq[(Document, Double)] = {
    val queryVec = queryVector(query, index, numDocuments)
    val docVecs =
      documents.map(doc => (doc, tfIdfVector(doc, index, numDocuments)))

    docVecs
      .map { case (doc, docVec) =>
        (doc, cosineSimilarity(queryVec, docVec))
      }
      .sortBy(-_._2)
      .take(numResults)
  }

  def main(args: Array[String]): Unit = {
    val (pairs, unigramVocabulary, bigramVocabulary) =
      Indexer.buildVocab("input/reuters-test")

    val documents = pairs.map { case (id, tokens) => Document(id, tokens) }

    val index = Indexer.buildUnigramIndex(pairs, unigramVocabulary)

    val query = "space"
    val results = rankDocuments(query, documents, index, documents.length, 2)
    results.foreach { case (doc, score) =>
      println(s"${doc.id}: $score")
    }

    println("done")
  }
}
