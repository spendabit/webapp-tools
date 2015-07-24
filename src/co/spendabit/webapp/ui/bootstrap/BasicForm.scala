package co.spendabit.webapp.ui.bootstrap

import co.spendabit.webapp.forms.ui.FormRenderer
import co.spendabit.webapp.forms.util.getAttr

/** Implements a form using Bootstrap, as seen in the "basic example", here:
  * http://getbootstrap.com/css/#forms-example
  */
class BasicForm extends FormRenderer {

  def formElem(labeledControls: xml.NodeSeq): xml.Elem =
    <form>
      { labeledControls }
      { submitSection }
    </form>

  def labeledControl(label: String, control: xml.NodeSeq): xml.NodeSeq =
    if (isCheckbox(control))
      <div class="checkbox"> <label>{ control } { label }</label> </div>
    else
      <div class="form-group">
        <label>{ label }</label> { withAttr(control, "class", "form-control") }
      </div>

  // TODO: Move this to a base-class where it can be shared by all `FormRenderer` classes?
  private def isCheckbox(control: xml.NodeSeq): Boolean =
    control match {
      case e: xml.Elem if e.label == "input" && getAttr(e, "type") == Some("checkbox") => true
      case _ => false
    }

  protected def submitSection: xml.NodeSeq =
    <button type="submit" class="btn btn-primary">{ submitButtonLabel }</button>

  protected def submitButtonLabel: String = "Submit"
}
