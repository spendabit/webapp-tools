package co.spendabit.webapp.forms.controls

import java.net.{URL, MalformedURLException}

case class URLField(override val label: String = "URL", override val name: String,
                    placeholder: String = "", requireProtocol: Boolean = false)
        extends GenericInput[URL](label, name, placeholder) {

  /** If `requireProtocol` is false, then we use a plain-old 'text' input, so web-browsers will
    * allow users to enter values like "www.my-site.com".
    */
  def inputType = if (requireProtocol) "url" else "text"

  def valueAsString(value: URL): String = value.toString

  def validate(s: String) = {

    val withProtocol =
      if (requireProtocol || s.matches("https?:.*"))
        s
      else
        s"http://$s"

    try {

      val url = new URL(withProtocol)
      val hasTLD = url.getHost.contains('.') && url.getHost.split('.').last.length > 1
      val hasInvalidCharsInHostname = {
        val h = if (s == withProtocol) url.getHost else s.split('/').head
        h.exists(c => !c.isLetterOrDigit && c != '-' && c != '.')
      }

      val hasSpuriousDot = url.getHost.split('.').contains("")

      if (hasTLD && !hasInvalidCharsInHostname && !hasSpuriousDot)
        Right(url)
      else
        throw new MalformedURLException
    }
    catch { case _: MalformedURLException => Left("Please provide a valid URL.") }
  }
}
