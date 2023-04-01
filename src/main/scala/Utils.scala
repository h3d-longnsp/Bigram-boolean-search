package org.long.A4

import scala.collection.immutable.ListMap

object Utils {
    /**
      * Merge two index of type Map[String, List[Int]], then sort the result.
      *
      * @param map1 First map to merge
      * @param map2 Second map to merge
      * @return A Map[String, List[Int]] which is the result of merging map1 and 
      * map2, sorted in ascending order.
      */
    def mergeTwoIndex(index1: Map[String, List[Int]],  index2: Map[String, List[Int]]): Map[String, List[Int]] = {
        sortIndex(index1 ++ index2)
    }

    /**
      * Sort an index of type Map[String, List[Int]]
      *
      * @param index A map to sort
      * @return a sorted ListMap based on the keys of the Map.
      */
    def sortIndex(index: Map[String, List[Int]]) = {
        ListMap(index.toSeq.sortBy(_._1):_*)
    }
}
