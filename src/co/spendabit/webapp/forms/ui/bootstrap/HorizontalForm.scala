package co.spendabit.webapp.forms.ui.bootstrap

import co.spendabit.webapp.forms.ui.FormRenderer
import co.spendabit.webapp.forms.util.getAttr

/** Implements Bootstrap's "horizontal form", as documented here:
  * http://getbootstrap.com/css/#forms-horizontal
  */
class HorizontalForm extends FormRenderer {

  /** If overridden, must be a value greater than zero (0) but less than twelve (12).
    */
  protected val leftColumnWidth: Int = 2

  def formElem(labeledControls: xml.NodeSeq): xml.Elem =
    <form class="form-horizontal" role="form">
      { labeledControls }
      <div class="form-group form-submit">
        <div class="col-sm-offset-3 col-sm-9">
          <button type="submit" class="btn btn-primary">Submit</button>
        </div>
      </div>
    </form>

  def labeledControl(label: String, control: xml.NodeSeq): xml.NodeSeq =
    <div class="form-group">{
      control match {

        case e: xml.Elem if e.label == "input" && getAttr(e, "type") == Some("checkbox") =>
          <div class={ s"col-xs-12 col-sm-offset-$leftColumnWidth col-sm-$rightColumnWidth" }>
            <div class="checkbox"> <label>{ control } { label }</label> </div>
          </div>

        case _ =>
          /* XXX: The client 'BaseWebForm' class will be responsible for adding an 'id'. */
          <label class={ "col-xs-12 col-sm-" + leftColumnWidth + " control-label" }
                 >{ label }</label>
          <div class={ "col-sm-" + rightColumnWidth }>
            { withAttr(control, "class", "form-control") }
          </div>
      }
    }</div>

  def submitSection: xml.NodeSeq =
    <div class="form-group form-submit">
      <div class={ s"col-xs-12 col-sm-offset-$leftColumnWidth col-sm-$rightColumnWidth" }>
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </div>

  private def rightColumnWidth: Int = {
    if (leftColumnWidth < 1 || leftColumnWidth > 11)
      throw new Exception("Invalid value for `leftColumnWidth`")
    12 - leftColumnWidth
  }
}
