package co.spendabit.webapp.forms.controls

case class TextInput(override val label: String, override val name: String)
        extends GenericInput[String](label, name) {

  def inputType = "text"

  def valueAsString(value: String): String = value

  def validate(s: String) =
    if (s.trim.length > 0)
      Right(s)
    else
      Left(s"Please provide a value for $label")
}
