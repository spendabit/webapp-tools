package co.spendabit.webapp.forms.controls

// XXX: This is a hack, for now (since we're passing a blank `label` and `placeholder`).
case class HiddenInput(override val name: String)
        extends GenericInput[String](label = "", name, placeholder = "") {

  def inputType = "hidden"

  def valueAsString(value: String): String = value

  def validate(s: String) = Right(s)
}
