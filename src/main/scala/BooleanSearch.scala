object BooleanSearch {
  /**
   * Search for a term in an index.
   * 
   * @param index a map of inverted index (term -> List[docId])
   * @param term a term to search for
   * @return a list of docIds
   */
  def search(index: Map[String, List[Int]], term: String): List[Int] = {
    index.getOrElse(term, List.empty).sorted
  }

  /**
   * Search for
   *
   * @param index
   * @param term1
   * @param term2
   * @return
   */
  def searchAnd(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.intersect(q2)
  }

  def searchOr(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    (q1 ++ q2).distinct.sorted
  }

  def searchAndNot(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.diff(q2)
  }

  /**
    * TODO: Add document
    *
    * @param index
    * @param term1
    * @param term2
    * @return
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