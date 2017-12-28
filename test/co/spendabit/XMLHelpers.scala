package co.spendabit

trait XMLHelpers {

  protected def getAttr(n: xml.Node, attr: String): Option[String] =
    n.attribute(attr).flatMap(_.headOption).map(_.toString())
}