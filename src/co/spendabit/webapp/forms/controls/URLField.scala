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
      if (url.getHost.contains('.') && url.getHost.split('.').last.length > 1)
        Right(url)
      else
        throw new MalformedURLException
    }
    catch { case _: MalformedURLException => Left("Please provide a valid URL.") }
  }
}
