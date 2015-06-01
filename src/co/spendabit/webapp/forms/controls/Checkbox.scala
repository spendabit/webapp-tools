package co.spendabit.webapp.forms.controls

import co.spendabit.webapp.forms.util.withAttr

case class Checkbox(override val label: String, name: String)
        extends LabeledControl[Boolean](label) {

  override def widgetHTML(value: Option[Boolean] = None): xml.NodeSeq = {
    val cb = <input type="checkbox" name={ name } />
    if (value == Some(true)) withAttr(cb, "checked", "checked") else cb
  }

  // XXX: Are there any values that should be considered invalid?
  // XXX: Should it only be "checked" if the submitted value is "on"?
  def validate(params: Map[String, Seq[String]]): Either[String, Boolean] =
    Right(if (params.get(name) == Seq("on")) true else false)
}
