package co.spendabit.webapp.forms.controls

case class Textarea(override val label: String, override val name: String)
        extends TextEntryControl[String](label, name) {

  def widgetHTML(value: Option[String] = None) =
    <textarea class="form-control" name={ name }>{ value.getOrElse("") }</textarea>

  def validate(s: String) =
    if (s.trim.length > 0)
      Right(s)
    else
      Left(s"Please provide a value for $label.")
}
