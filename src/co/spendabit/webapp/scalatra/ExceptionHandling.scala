package co.spendabit.webapp.scalatra

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.servlet.ServletBase

/** Provides an alternative to Scalatra's built-in exception-handling mechanism (i.e.,
  * `renderUncaughtException`) which allows for notification emails to be sent out to email
  * address(es) configured at the application level.
  */
trait ExceptionHandling extends ServletBase {

  /** `sendExceptionNotification` is responsible for actually firing off an email from the
    * application, and it will be invoked when an exception goes uncaught in production. The
    * `details` parameter will be a string containing the stack-trace and other contextual
    * details (the URI that was requested, session info, etc).
    *
    * This abstract method is used so as to make this interface (`ExceptionHandling`) agnostic
    * with regard to the email (or other) notification back-end.
    */
  protected def sendExceptionNotification(details: String)

  /** @return HTML content to be served when errors occur when `isDevelopmentMode` returns true.
    */
  protected def errorPageForDevelopment(exception: Throwable): String

  /** @return HTML content to be served when errors occur when `isDevelopmentMode` returns false.
    */
  protected def errorPageForProduction(exception: Throwable): String

  /** @return All session variables.
    */
  protected def sessionVariables: Map[String, String]

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
          request.headers.names.map(h => "  " + h + ": " + request.header(h).getOrElse("N/A")).
            mkString("\n") + "\n\n" +
          "Request body:\n" + request.body

      sendExceptionNotification(msgBody)
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
