package co.spendabit.webapp.forms.v3.controls

case class Password(minLength: Int) extends GenericInput[String] {

  def inputType = "password"

  def valueAsString(value: String): String = value

  def validate(s: String): Either[String, String] =
    if (s.trim.length >= minLength)
      Right(s)
    else if (s.trim.length == 0)
      Left(s"Please provide a password.")
    else
      Left(s"The password must be at least $minLength characters long.")
}
