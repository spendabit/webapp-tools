package co.spendabit.html.jsoup

import org.jsoup.nodes.{Attribute, Attributes, Element}
import org.jsoup.select.Elements

trait ImplicitConversions {

  implicit def elemsToList(elems: Elements): List[Element] =
    scala.collection.JavaConversions.asScalaBuffer(elems).toList

  implicit def attributesToList(attrs: Attributes): List[Attribute] =
    scala.collection.JavaConversions.asScalaBuffer(attrs.asList()).toList
}
