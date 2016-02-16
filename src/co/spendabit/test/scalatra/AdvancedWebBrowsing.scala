package co.spendabit.test.scalatra

import java.net.URL
import scala.language.implicitConversions

import co.spendabit.html.jsoup
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.scalatra.test.scalatest.ScalatraSuite

/** A testing "harness" (class) that takes it a step beyond the basic webapp testing methods
  * provided by `ScalatraSuite`, providing methods for clicking links, submitting forms, etc.
  */
trait AdvancedWebBrowsing extends ScalatraSuite with jsoup.ImplicitConversions {

  protected def selectElems(selector: String): Seq[Element] =
    Jsoup.parse(response.body).select(selector)

  /** Find the <form> element matching the given `selector`, failing the test-case if no
    * such form is found.
    */
  protected def getForm(selector: String): Element = {
    selectElems(selector).headOption match {
      case None => fail(s"Found no form matching following selector: $selector")
      case Some(f) =>
        if (f.nodeName() != "form") fail("The selected element is not a 'form' element")
        f
    }
  }

  /** Submit the given `form` represented as a Jsoup `Element`, using the given `params`.
    * @param context A relative path indicating the URI at which this form was
    *                served (e.g. /path-to/page-with-form.html), necessary in cases where the
    *                form has an 'action' that's a relative path (e.g. ./submit-here.html).
    */
  protected def submitForm[A](form: Element, context: Option[String],
                              params: Seq[(String, String)])(f: => A): A = {

    assert(form.select("input[type=submit], button[type=submit]").length > 0,
      "The provided form has no submit buttons")

    params.foreach { case (name, _) =>
      // TODO: For radio-button fields, ensure specified value is one of the provided options.
      // TODO: For select fields, ensure specified value is one of the provided options.
      // TODO: For checkbox fields, ensure the specified value is "on" (or null?).
      assert(
        form.select(s"input[name=$name], textarea[name=$name], select[name=$name]").length > 0,
        s"Form must have field named '$name'")
    }

    // TODO: Support case where 'action' contains a full URL or absolute path.
    val action = form.attr("action")
    val uri =
      if (action.toLowerCase.startsWith("http://") || action.toLowerCase.startsWith("https://"))
        fail("Forms with URL in 'action' attribute not presently supported")
      else if (action.startsWith("./"))
        context.map(p => new java.io.File(p).getParent + "/" + action.stripPrefix("./")).
          getOrElse(fail("Cannot submit form with 'action' having relative path when no " +
                         "'context' was provided"))
      else
        action

    val valuesToSubmit = (defaultValuesForForm(form).toMap ++ params.toMap).toSeq
    form.attr("method").toLowerCase match {
      case "get"  => get(uri, valuesToSubmit:_*)(f)
      case "post" =>
        if (form.attr("enctype") == "multipart/form-data")
          post(uri, params = valuesToSubmit, files = Seq())(f)
        else
          post(uri, valuesToSubmit:_*)(f)
      case m      => fail(s"Form has unsupported method, '$m'")
    }
  }

  // TODO: Add support for default values in other control/input types.
  protected def defaultValuesForForm(form: Element): Seq[(String, String)] =
    form.select("input[value]").filter(i => Seq("text", "hidden").contains(i.attr("type"))).
      map(i => (i.attr("name"), i.attr("value"))) ++
    form.select("textarea").map(ta => (ta.attr("name"), ta.text)) ++
    form.select("select").flatMap { s =>
      // TODO: Support using the first <option>'s value when no 'selected' attribute is set!
      s.select("option[selected]").headOption.map(o => (s.attr("name"), o.attr("value")))
    }

  /** Submit the given `form` represented as a Jsoup `Element`, using the given `params`.
    */
  protected def submitForm[A](form: Element, params: (String, String)*)(f: => A): A =
    submitForm(form, None, params)(f)

  /** A simple wrapper around `submitForm` that may be used when one, and *only one*, form is
    * present on the current page.
    */
  protected def submitSoleForm[A](params: (String, String)*)(f: => A): A =
    submitForm(getForm("form"), context = None, params)(f)

  /** Make a GET request, making subsequent requests for any 300-level HTTP responses.
    */
  protected def getFollowingRedirects[A](path: String, params: (String, String)*)(f: => A): A = {
    get(path, params:_*) {
      if (isRedirectResponse)
        getFollowingRedirects(redirectLocation)(f)
      else
        f
    }
  }

  /** Make a POST request, making subsequent requests for any 300-level HTTP responses.
    */
  protected def postFollowingRedirects[A](path: String, params: (String, String)*)(f: => A): A = {
    post(path, params) {
      if (isRedirectResponse)
        getFollowingRedirects(redirectLocation)(f)
      else
        f
    }
  }

  protected def isRedirectResponse = status >= 300 && status < 400

  protected def redirectLocation: String = {
    val location = header.getOrElse("Location",
      throw new Exception(s"$status response with no Location header?"))
    if (location.startsWith("/"))
      location
    else {
      val newURL = new URL(location)
      if (newURL.getHost != "localhost")
        throw new Exception("Location header was not pointed at localhost!")
      newURL.getPath + "?" + newURL.getQuery // XXX: Fragment?
    }
  }
}
