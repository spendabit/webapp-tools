package co.spendabit.webapp.forms.v3.controls

object HiddenInput extends GenericInput[String] {

  def inputType = "hidden"

  def valueAsString(value: String): String = value

  def validate(s: String) = Right(s)
}
