package co.spendabit.webapp.forms.v3

import scala.language.higherKinds

class DefaultFormRenderer[F[_] <: Field[_]] extends FormRenderer[F] {

  def formElem(labeledControls: xml.NodeSeq): xml.Elem =
    <form>
      { labeledControls }
      { submitSection }
    </form>

  def labeledControl(field: F[_], control: xml.NodeSeq): xml.NodeSeq =
    if (isCheckbox(control)) {
      <div class="checkbox"> <label>{ control } { field.label }</label> </div>
    } else {

      val widget =
        if (isFileInput(control))
          control
        else
          withAttr(control, "class", "form-control")

      <div class="form-group">
        <label>{ field.label }</label> { widget }
      </div>
    }

  protected def submitSection: xml.NodeSeq =
    <button type="submit" class="btn btn-primary">{ submitButtonLabel }</button>

  protected def submitButtonLabel: String = "Submit"
}
