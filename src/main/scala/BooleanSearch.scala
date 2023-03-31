object BooleanSearch {
  /**
   * Search an index map for a term and return a list of results.
   * 
   * @param index A map of inverted index (term -> List[docId])
   * @param term The term to search for
   * @return A list of results, sorted in ascending order. If not found, an empty list is returned.
   */
  def search(index: Map[String, List[Int]], term: String): List[Int] = {
    index.getOrElse(term, List.empty).sorted
  }

  /**
   * Search an index map for two terms and return a list of the intersecting results.
   *
   * @param index A map of strings to a list of integers
   * @param term1 The first term to search for
   * @param term2 The second term to search for
   * @return A list of the intersecting results from the two searches, sorted in ascending order. 
   * If not found, an empty list is returned.
   */
  def searchAnd(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.intersect(q2).sorted
  }
  
  /**
    * Search an index map for two terms and return a list of the combined results.
    *
    * @param index A map of strings to a list of integers
    * @param term1 The first term to search for
    * @param term2 The second term to search for
    * @return A list of the combined results from the two searches, sorted in ascending order. 
    * If not found, an empty list is returned.
    */
  def searchOr(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    (q1 ++ q2).distinct.sorted
  }

  /**
    * Search an index map for two terms and return a list of integers 
    * which are in the list associated with term1 but 
    * not in the list associated with term2.
    *
    * @param index A map of strings to a list of integers
    * @param term1 The first term to search for
    * @param term2 The second term to search for
    * @return A list of the results from term1 but not from term2, sorted in ascending order. 
    * If not found, an empty list is returned.
    */
  def searchAndNot(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.diff(q2).sorted
  }

  /**
    * Search an index map for two terms and return a list of integers 
    * which are in the list associated with term1 or 
    * not in the list associated with term2.
    *
    * @param index A map of strings to a list of integers
    * @param term1 The first term to search for
    * @param term2 The second term to search for
    * @return A list of the results from term1 or not from term2, sorted in ascending order. 
    * If not found, an empty list is returned.
    */  
  def searchOrNot(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)
    val a = index.values.flatten.toSet.toList

    (q1 ++ a.diff(q2)).distinct.sorted
  }

  def main(args: Array[String]): Unit = {
    val unigramIndex = Indexer.loadIndex("output/index-unigram1.txt")
    val bigramIndex = Indexer.loadIndex("output/index-bigram1.txt")

    val combineIndex = unigramIndex ++ bigramIndex
    println(search(combineIndex, "ago"))
    println(search(combineIndex, "the tokyo"))
  }
}