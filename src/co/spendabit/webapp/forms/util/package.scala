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
            val newAttr = new UnprefixedAttribute("checked", "checked", xml.Null)
            if (getAttr(e, "value") == Some(value))
              e.copy(attributes = e.attributes.append(newAttr))
            else
              e
          } else {
            val newAttr = new UnprefixedAttribute("value", value, xml.Null)
            e.copy(attributes = e.attributes.append(newAttr))
          }
        } else e
      case e: xml.Elem =>
        e.copy(child = e.child.map(c => setValue(c, fieldName, value)))
      case n =>
        n
    }

  private def getAttr(e: xml.Elem, attr: String): Option[String] =
    e.attribute(attr).map(_.headOption.map(_.toString())).flatten
}
