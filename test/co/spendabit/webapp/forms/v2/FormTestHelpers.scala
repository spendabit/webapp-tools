package co.spendabit.webapp.forms.v2

import co.spendabit.XMLHelpers

trait FormTestHelpers extends XMLHelpers {

  trait PostWebForm[T] extends BaseWebForm[T] {
    def method = POST
    def action = "/post-here"
  }

  protected def getControlValue(html: xml.NodeSeq, name: String): Option[String] = {
    val control = getControl(html, name)
    control.label match {
      case "input"    => getAttr(control, "value")
      case "textarea" => Some(control.child.text)
      case l          => throw new IllegalArgumentException(s"Unsupported control type: $l")
    }
  }

  protected def containsInputWithName(html: xml.NodeSeq, name: String,
                                    nodeType: String = "input"): Boolean =
    (html \\ nodeType).filter(n => getAttr(n, "name").contains(name)).length == 1

  protected def getInput(html: xml.NodeSeq, name: String): xml.Node =
    getControl(html, name, Seq("input"))

  protected def getControl(html: xml.NodeSeq, name: String,
                           nodeTypes: Seq[String] = Seq("input", "textarea")): xml.Node = {

    val allControls = nodeTypes.flatMap(t => html \\ t)
    allControls.find(n => getAttr(n, "name").contains(name)).
      getOrElse(throw new IllegalArgumentException(s"No form-control with name '$name' found"))
  }
}
