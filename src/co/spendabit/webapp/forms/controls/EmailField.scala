package co.spendabit.webapp.forms.controls

import javax.mail.internet.InternetAddress
import org.apache.commons.validator.routines.EmailValidator

case class EmailField(override val label: String, override val name: String,
                      placeholder: String = "")
        extends GenericInput[InternetAddress](label, name, placeholder) {

  def inputType = "email"

  def valueAsString(value: InternetAddress): String = value.toString

  def validate(s: String) =
    if (EmailValidator.getInstance.isValid(s))
      Right(new InternetAddress(s))
    else
      Left("Please provide a valid email address.")
}
