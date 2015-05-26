package co.spendabit.webapp.forms.controls

case class PasswordInput(override val label: String, override val name: String,
                         minLength: Int)
        extends GenericInput[String](label, name) {

  def inputType = "password"

  def valueAsString(value: String): String = value

  def validate(s: String) =
    if (s.trim.length > minLength)
      Right(s)
    else if (s.trim.length == 0)
      Left(s"Please provide a value for $label.")
    else
      Left(s"$label must be at least $minLength characters long.")
}
