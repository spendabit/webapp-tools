package co.spendabit.webapp.forms.v3.controls

import javax.mail.internet.InternetAddress
import org.apache.commons.validator.routines.EmailValidator

object EmailAddr extends GenericInput[InternetAddress] {

  def inputType = "email"

  def valueAsString(value: InternetAddress): String = value.toString

  def validate(s: String): Either[String, InternetAddress] =
    if (EmailValidator.getInstance.isValid(s))
      Right(new InternetAddress(s))
    else
      Left("Please provide a valid email address.")
}
