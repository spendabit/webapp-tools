package co.spendabit.webapp.scalatra

import javax.mail.internet.InternetAddress
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.servlet.ServletBase

/** Provides an alternative to Scalatra's built-in exception-handling mechanism (i.e.,
  * `renderUncaughtException`) which allows for notification emails to be sent out to email
  * address(es) configured at the application level.
  */
trait ExceptionHandling extends ServletBase {

  /** @return HTML content to be served when errors occur when `isDevelopmentMode` returns true.
    */
  protected def errorPageForDevelopment(exception: Throwable): String

  /** @return HTML content to be served when errors occur when `isDevelopmentMode` returns false.
    */
  protected def errorPageForProduction(exception: Throwable): String

  protected def sessionVariables: Map[String, String]

  protected def recipients(exception: Throwable): Seq[InternetAddress]

  override protected def renderUncaughtException(e: Throwable)
                                                (implicit request: HttpServletRequest,
                                                 response: HttpServletResponse) {
    status = 500
    if (isDevelopmentMode) {
      response.getWriter.print(errorPageForDevelopment(e))
    } else {
      response.getWriter.print(errorPageForProduction(e))

      val hasQueryString = request.queryString != null && request.getQueryString != ""
      val url = request.getRequestURL.toString +
        (if (hasQueryString) "?" + request.queryString else "")
      val postParams = postParameters.flatMap { case (key, vs) =>
        vs.map(v => s"  $key: $v")
      }.toSeq

      val msgBody =
        "Request was a " + request.getMethod + " request for " + url + "\n\n" +
          s"The exception was of type ${e.getClass.getName} and had the following message:\n" +
          "  " + e.getMessage + "\n\n" +
          "Find the stack-trace below:\n" + e.getStackTrace.map(_.toString).mkString("\n") + "\n\n" +
          "Remote IP address: " + request.remoteAddress + "\n\n" +
          (if (postParams.nonEmpty)
            "POST parameters:\n" + postParams.mkString("\n") + "\n\n" else "") +
          "Session variables:\n" +
          sessionVariables.map { case (name, value) =>
            "  " + name + ": " + value.toString
          }.mkString("\n") + "\n\n" +
          "And all request headers:\n" +
          request.headers.map(h => "  " + h._1 + ": " + h._2).mkString("\n") + "\n\n" +
          "Request body:\n" + request.body

      // TODO: Implement email hook!
      // internalEmailNotification("An exception occurred!", msgBody, recipients(e))
    }
  }

  protected def postParameters =
    if (request.getMethod.toLowerCase == "post" &&
      request.getHeader("Content-Type").toLowerCase.
        startsWith("application/x-www-form-urlencoded"))
      org.scalatra.util.MapQueryString.parseString(request.body)
    else
      Map.empty
}
