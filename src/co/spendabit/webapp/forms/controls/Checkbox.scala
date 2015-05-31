package co.spendabit.webapp.forms.controls

case class Checkbox(override val label: String, name: String)
        extends LabeledControl[Boolean](label) {

  override def widgetHTML(value: Option[Boolean] = None): xml.NodeSeq =
      <input type="checkbox" name={ name }
             checked={ if (value == Some(true)) "checked" else "" } />

  // XXX: Should any value be considered invalid?
  // XXX: Should it only be "checked" if the submitted value is "on"?
  def validate(params: Map[String, Seq[String]]): Either[String, Boolean] =
    Right(if (params.get(name) == Seq("on")) true else false)
}
