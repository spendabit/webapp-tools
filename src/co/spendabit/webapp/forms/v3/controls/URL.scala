package co.spendabit.webapp.forms.v3.controls

import java.net

case class URL(requireProtocol: Boolean = false) extends GenericInput[net.URL] {

  /** If `requireProtocol` is false, then we use a plain-old 'text' input, so web-browsers will
    * allow users to enter values like "www.my-site.com" (i.e., without the "http://" prefix).
    */
  def inputType: String = if (requireProtocol) "url" else "text"

  def valueAsString(value: net.URL): String = value.toString

  def validate(s: String): Either[String, net.URL] = {

    val withProtocol =
      if (requireProtocol || s.matches("https?:.*"))
        s
      else
        s"http://$s"

    try {

      val url = new java.net.URL(withProtocol)
      val hasTLD = url.getHost.contains('.') && url.getHost.split('.').last.length > 1
      val hasInvalidCharsInHostname = {
        val h = if (s == withProtocol) url.getHost else s.split('/').head
        h.exists(c => !c.isLetterOrDigit && c != '-' && c != '.')
      }

      val hasSpuriousDot = url.getHost.split('.').contains("")
      val lastCharValid = url.getHost.length > 0 && !Seq('.', '-').contains(url.getHost.last)

      if (hasTLD && !hasInvalidCharsInHostname && !hasSpuriousDot && lastCharValid)
        Right(url)
      else
        throw new net.MalformedURLException
    }
    catch { case _: net.MalformedURLException => Left("Please provide a valid URL.") }
  }
}

//object URL {
//  def apply() = URL(requireProtocol = false)
//}
