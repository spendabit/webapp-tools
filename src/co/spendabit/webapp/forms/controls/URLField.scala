package co.spendabit.webapp.forms.controls

import java.net.URL

case class URLField(override val label: String = "URL", override val name: String)
        extends GenericInput[URL](label, name) {

  def inputType = "url"

  def valueAsString(value: URL): String = value.toString

//  def widgetHTML(id: String, value: Option[URL] = None) =
//    widgetHTML(id, value.map(u => u.toString))

  def validate(s: String) =
    try Right(new java.net.URL(s))
    catch { case _: Exception => Left("Please provide a valid URL.") }
}
