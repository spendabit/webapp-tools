package co.spendabit

package object csv {

  case class Cell(columnIndex: Int, columnLabel: Option[String], value: Option[String])

  case class Row(cells: Seq[Cell]) {

    def get(matches: Cell => Boolean): Option[String] = cells.find(matches).flatMap(_.value)
    
    def get(i: Int):    Option[String] = get(_.columnIndex == i)
    def get(l: String): Option[String] = get(_.columnLabel.contains(l))

    def getOrElse[B1 >: String](i: Int,    default: => B1): B1 = get(i).getOrElse(default)
    def getOrElse[B1 >: String](l: String, default: => B1): B1 = get(l).getOrElse(default)
  }

  abstract class CharacterSeparatedValuesReader(lines: Iterator[String]) extends Iterator[Row] {

    protected def delimiter: Char

    // TODO: Support quoted headers!
    val headers: Seq[String] = lines.next().split(delimiter)

    def next(): Row = Row(
      takeCells(lines.next()).map(Some(_)).
        zipAll(headers.map(Some(_)), None, None).zipWithIndex.map {
          case ((value, label), i) => Cell(i, label, value) })

    def hasNext: Boolean = lines.hasNext

    // XXX: How to make this tail-recursive?
    // @tailrec
    private def takeCells(restOfLine: String): Seq[String] =
      if (restOfLine == "") {
        Seq()
      } else {

        val (content, offsetToNext): (String, Int) =
          if (restOfLine.startsWith("\"")) {
            val rest = restOfLine.tail
            var escapeQuote = false
            val quoted = rest.zipWithIndex.takeWhile { case (c, i) =>
              if (c == '"' && escapeQuote) {
                escapeQuote = false
                true
              } else if (c == '"' && i + 1 < rest.length && rest.charAt(i + 1) == '"') {
                escapeQuote = true
                true
              } else {
                c != '"'
              }
            }.map(_._1).mkString
            (quoted.replace("\"\"", "\""), quoted.length + 2)
          } else {
            val c = restOfLine.takeWhile(_ != delimiter)
            (c, c.length)
          }

        val rest =
          if (offsetToNext < restOfLine.length)
            restOfLine.substring(offsetToNext).stripPrefix(delimiter.toString)
          else
            ""

        content +: takeCells(rest)
      }
  }

  // XXX: Need to figure out where to find this "helger" CSVWriter, without pulling in a bunch
  // XXX: of unrelated junk.
  /* def toCSV[T](objects: Iterable[T], columns: (String, T => String)*): String = {

    import com.helger.commons.csv.CSVWriter
    import scala.collection.JavaConverters._

    val header = columns.map(_._1)
    val rows = objects.map(o => columns.map(_._2(o)))

    val sw = new StringWriter()
    new CSVWriter(sw).writeAll((header +: rows.toSeq).map(_.asJava).asJava)
    sw.toString
  } */
}
