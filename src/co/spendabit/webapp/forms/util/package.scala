package co.spendabit.webapp.forms

import scala.xml.UnprefixedAttribute

package object util {

  /** Given a set of parameters (`params`), that have presumably been derived from values
    * submitted via form-submission HTTP request, populate all corresponding form-fields found
    * in the given `html`.
    *
    * Less abstractly, as an example, if `params` were Map("email" -> Seq("joe@test.com")) and
    * `html` contained an <input name="email" />, it would be returned (in the same place in the
    * HTML/XML it was originally in) with its 'value' attribute set to "joe@test.com".
    */
  def populateFormFields(html: xml.NodeSeq, params: Map[String, Seq[String]]): xml.NodeSeq = {
    params match {
      case Seq() => html
      case ps    =>
        ps.foldLeft(html) { case (htmlSoFar, (fieldName, vs)) =>
          vs.headOption.map(v => setValue(htmlSoFar, fieldName, v)).getOrElse(htmlSoFar)
        }
    }
  }

  def setValue(formHTML: xml.NodeSeq, fieldName: String, value: String): xml.NodeSeq = {
    for(n <- formHTML) yield setValue(n, fieldName, value)
  }

  def setValue(formHTML: xml.Node, fieldName: String, value: String): xml.Node =
    formHTML match {
      case e: xml.Elem if e.label == "input" =>
        val n = getAttr(e, "name")
        val t = getAttr(e, "type").getOrElse("text")
        if (n == Some(fieldName)) {
          // TODO: Add support for other <input/> types!
          if (t == "radio") {
            if (getAttr(e, "value") == Some(value))
              withAttr(e, "checked", "checked")
            else
              e
          } else {
            withAttr(e, "value", value)
          }
        } else e
      case e: xml.Elem if e.label == "textarea" && getAttr(e, "name") == Some(fieldName) =>
        e.copy(child = Seq(xml.Text(value)))
      case e: xml.Elem if e.label == "select" && getAttr(e, "name") == Some(fieldName) =>
        e.copy(child = withOptionSelected(e.child, value))
      case e: xml.Elem =>
        e.copy(child = e.child.map(c => setValue(c, fieldName, value)))
      case n =>
        n
    }

  private def withOptionSelected(elems: Seq[xml.Node], value: String): Seq[xml.Node] =
    elems.map {
      case opt: xml.Elem if opt.label == "option" =>
        if (getAttr(opt, "value") == Some(value)) withAttr(opt, "selected", "selected") else opt
      case e: xml.Elem =>
        e.copy(child = withOptionSelected(e.child, value))
      case n =>
        n
    }

  // XXX: Remove any existing attributes for `key`?
  def withAttr(e: xml.Elem, key: String, value: String) =
    e.copy(attributes = e.attributes.append(new UnprefixedAttribute(key, value, xml.Null)))

  def withAttrs(e: xml.Elem, attrs: (String, String)*) =
    attrs.foldLeft(e) { case (elem, attr) => withAttr(elem, attr._1, attr._2) }

  def getAttr(e: xml.Elem, attr: String): Option[String] =
    e.attribute(attr).map(_.headOption.map(_.toString())).flatten
}
