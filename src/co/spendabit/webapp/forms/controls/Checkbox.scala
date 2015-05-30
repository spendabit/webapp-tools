package co.spendabit.webapp.forms.controls

case class Checkbox(override val label: String, override val name: String)
        extends TextEntryControl[Boolean](label, name) {

  override def widgetHTML(value: Option[Boolean] = None): xml.NodeSeq =
      <input type="checkbox" name={ name }
             checked={ if (value == Some(true)) "checked" else "" } />

  // XXX: Should any value be considered invalid?
  // XXX: Should it only be "checked" if the submitted value is "on"?
  def validate(s: String) = Right(if (s == "on") true else false)
}
