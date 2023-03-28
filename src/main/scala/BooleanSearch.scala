object BooleanSearch {
  /**
   * Search for a term in an index.
   * 
   * @param index a map of inverted index (term -> List[docId])
   * @param term a term to search for
   * @return a list of docIds
   */
  private def search(index: Map[String, List[Int]], term: String): List[Int] = {
    index.getOrElse(term, List.empty)
  }

  /**
   * Search for
   *
   * @param index
   * @param term1
   * @param term2
   * @return
   */
  private def searchAnd(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.intersect(q2)
  }

  private def searchOr(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    (q1 ++ q2).distinct.sorted
  }

  private def searchAndNot(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)

    q1.diff(q2)
  }

  private def searchOrNot(index: Map[String, List[Int]], term1: String, term2: String): List[Int] = {
    val q1 = search(index, term1)
    val q2 = search(index, term2)
    val a = index.values.flatten.toSet.toList

    (q1 ++ a.diff(q2)).distinct.sorted
  }

  def main(args: Array[String]): Unit = {
    val index = Indexer.load("output/index.txt")

    // val index = Map(
    //   "tom" -> List(1, 4, 8, 10),
    //   "jerry" -> List(1, 4, 7),
    //   "dog" -> List(1, 7, 11, 13),
    //   "love" -> List(2, 4, 7)
    // )

    println(search(index, "accion"))

  }
}