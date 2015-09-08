package co.spendabit.webapp.forms.ui

import co.spendabit.webapp.forms.util
import co.spendabit.webapp.forms.util._

abstract class FormRenderer {

  def formElem(labeledControls: xml.NodeSeq): xml.Elem

  def labeledControl(label: String, control: xml.NodeSeq): xml.NodeSeq

  protected def withAttr(n: xml.NodeSeq, key: String, value: String): xml.NodeSeq =
    n match {
      case e: xml.Elem => util.withAttr(e, key, value)
//      case Seq(e: xml.Elem) => withAttr(e, key, value)
      case ns => ns
    }

  protected def isCheckbox(control: xml.NodeSeq): Boolean =
    control match {
      case e: xml.Elem if e.label == "input" && getAttr(e, "type") == Some("checkbox") => true
      case _ => false
    }
}
