package co.spendabit.html.jsoup

import org.jsoup.nodes.{Attribute, Attributes, Element}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

trait ImplicitConversions {

  implicit def elemsToList(elems: Elements): Seq[Element] =
    elems.asScala.toSeq

  implicit def attributesToList(attrs: Attributes): Seq[Attribute] =
    attrs.asScala.toSeq
}
