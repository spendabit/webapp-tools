package co.spendabit.webapp.forms.v3.controls

object TextLine extends GenericInput[String] {

  def inputType = "text"

  def valueAsString(value: String): String = value

  def validate(s: String): Either[String, String] =
    if (s.trim.nonEmpty)
      Right(s.trim)
    else
      Left(s"Please provide a value.")
}
