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
    */
  protected def submitForm[A](form: Element, params: (String, String)*)(f: => A): A = {

    params.foreach { case (name, _) =>
      // TODO: Support form-fields that aren't <input/> elements.
      // TODO: Support submitting default values (specified via input's "value" attribute).
      assert(form.select(s"input[name=$name], select[name=$name]").length > 0,
        s"Form must have field named '$name'")
    }

    // TODO: Add support for default values in other control/input types (e.g. <select> elements).
    val defaultValues: Seq[(String, String)] =
      form.select("input[value]").filter(_.attr("type") == "text").
        map(i => (i.attr("name"), i.attr("value")))

    // TODO: Support case where 'action' contains a full URL or relative path.
    val uri = form.attr("action")

    val valuesToSubmit = (defaultValues.toMap ++ params.toMap).toSeq
    form.attr("method").toLowerCase match {
      case "get"  => get(uri, valuesToSubmit:_*)(f)
      case "post" => post(uri, valuesToSubmit:_*)(f)
      case m      => fail(s"Form has unsupported method, '$m'")
    }
  }

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

  private def isRedirectResponse = status >= 300 && status < 400

  private def redirectLocation: String = {
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
