package co.spendabit.webapp.forms.v3.controls

import scala.xml.Elem

object Textarea extends TextEntryControl[String] {

  def html(value: Option[String] = None): Elem =
    <textarea>{ value.getOrElse("") }</textarea>

  def validate(s: String): Either[String, String] =
    if (s.trim.length > 0)
      Right(s)
    else
      // TODO: Fix this!
      Left(s"Please provide a value for XXX.")
}
