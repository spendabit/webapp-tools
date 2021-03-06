package co.spendabit.webapp.forms.controls

case class TextInput(override val label: String, override val name: String,
                     placeholder: String = "")
        extends GenericInput[String](label, name, placeholder) {

  def inputType = "text"

  def valueAsString(value: String): String = value

  def validate(s: String) =
    if (s.trim.length > 0)
      Right(s.trim)
    else
      Left(s"Please provide a value for $label.")
}
