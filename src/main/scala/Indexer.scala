import scala.io.Source
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Collectors.toList
import scala.collection.JavaConverters._
import org.tartarus.snowball.ext.englishStemmer

object Indexer {
  private def stem(stemmer: englishStemmer, tokens: List[String]): Seq[String] = {
    tokens.map { token =>
      stemmer.setCurrent(token)
      stemmer.stem()
      stemmer.getCurrent()
    }
  }

  private def build(path: String, outputPath: String = "") = {
    val regexNumber = "(?<!\\S)\\S*\\d+\\S*(?!\\S)".r
    val stemmer = new englishStemmer()
    val files = Files.list(Paths.get(path)).collect(toList()).asScala.map(_.getFileName().toString())
    val pairs = files.map { file =>
      val lines = Source.fromFile(path + "/" + file, "ISO-8859-1").getLines().toList
      val tokens = lines.flatMap(line => line.split("\\W+"))
        .map(_.trim())
        .filter(_.nonEmpty)
        .map(_.toLowerCase())
        .map(token => regexNumber.replaceAllIn(token, "[NUM]"))
      (file, stem(stemmer, tokens))
    }.toList
    val vocabulary = pairs.map(_._2).flatMap(identity).toSet.toList
    val index = vocabulary.map { term =>
      val docIds = pairs.filter(pair => pair._2.contains(term)).map(_._1.toInt)
      (term, docIds)
    }.toMap
    
    val sortedIndex = index.toSeq.sortBy(_._1).toMap

    if (outputPath.nonEmpty) {
    // save sortedIndex to outputPath
      val content = sortedIndex.map { case (term, docIds) => term + ' ' + docIds.mkString(" ") }.mkString("\n")
      Files.writeString(Paths.get(outputPath), content)
    }
    sortedIndex
  }

  def load(indexPath: String): Map[String, List[Int]] = {
    val lines = Source.fromFile(indexPath, "UTF-8").getLines()
    val index = lines.foldLeft(Map[String, List[Int]]()) { (map, line) =>
      val parts = line.split(" ")
      val key = parts.head
      val values = parts.tail.map(_.toInt).toList
      map + (key -> values)
    }
    index
  }

  def main(args: Array[String]): Unit = {
    val index = build("input/reuters-test/", "output/index.txt")
    println("done")
  }
}