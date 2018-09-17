package co.spendabit.webapp.forms.ui.bootstrap

import co.spendabit.webapp.forms.ui.FormRenderer

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
    if (isCheckbox(control)) {
      <div class="checkbox"> <label>{ control } { label }</label> </div>
    } else {

      val widget =
        if (isFileInput(control))
          control
        else
          withAttr(control, "class", "form-control")

      <div class="form-group">
        <label>{ label }</label> { widget }
      </div>
    }

  protected def submitSection: xml.NodeSeq =
    <button type="submit" class="btn btn-primary">{ submitButtonLabel }</button>

  protected def submitButtonLabel: String = "Submit"
}
