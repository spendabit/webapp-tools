package co.spendabit.webapp.forms.controls

case class PasswordInput(override val label: String, override val name: String,
                         minLength: Int, placeholder: String)
        extends GenericInput[String](label, name, placeholder) {

  def inputType = "password"

  def valueAsString(value: String): String = value

  def validate(s: String) =
    if (s.trim.length >= minLength)
      Right(s)
    else if (s.trim.length == 0)
      Left(s"Please provide a password.")
    else
      Left(s"The password must be at least $minLength characters long.")
}
