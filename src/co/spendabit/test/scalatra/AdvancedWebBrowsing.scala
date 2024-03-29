package co.spendabit.test.scalatra

import java.net.URL
import scala.annotation.tailrec
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

  protected def bodyAsXML: xml.NodeSeq =
    if (body.trim.isEmpty)
      fail("'body' contains no content!")
    else
      xml.parsing.XhtmlParser(scala.io.Source.fromString(body))

  protected def clickLinkHavingText[T](text: String): Unit =
    clickLinkHavingText(text, f = {})

  protected def clickLinkHavingText[T](text: String, f: => T): Unit =
    clickLinkHavingText(text, followRedirects = true, f)

  protected def clickLinkHavingText[T](text: String, followRedirects: Boolean,
                                       f: => T): Unit = {
    linksHavingText(text) match {
      case Seq() => fail(s"No links found having text '$text'")
      case Seq(e) => clickLink(e, followRedirects)(f)
      case _ => fail(s"Multiple links found having text '$text'")
    }
  }

  protected def clickLink(a: xml.Node, followRedirects: Boolean = true)(f: => Unit): Unit = a match {
    case e: xml.Elem if e.label == "a" =>
      val path = e.attribute("href").get.head.text
      if (path.matches("^[a-zA-Z]+:")) fail("Link is to remote location: " + e)
      if (followRedirects) getFollowingRedirects(path)(f) else get(path)(f)
    case e => fail("Expected to get 'a' element, but got this: " + e)
  }

  protected def linksHavingText(text: String): xml.NodeSeq =
    bodyAsXML \\ "a" filter(_.text.contains(text))

  /** Find the <form> element matching the given `selector`, failing the test-case if no
    * such form is found.
    */
  protected def getForm(selector: String): Element = {
    selectElems(selector) match {
      case Seq() => fail(s"Found no form matching following selector: $selector")
      case Seq(f) =>
        if (f.nodeName() != "form") fail("The selected element is not a 'form' element")
        f
      case forms if forms.length > 1 =>
        fail(s"Multiple forms found matching following selector: $selector")
    }
  }

  /** Submit the given `form` represented as a Jsoup `Element`, using the given `params`.
    * @param context A relative path indicating the URI at which this form was
    *                served (e.g. /path-to/page-with-form.html), necessary in cases where the
    *                form has an 'action' that's a relative path (e.g. ./submit-here.html).
    */
  protected def submitForm[A](form: Element, context: Option[String],
                              params: Params)(f: => A): A = {

    val submitButton =
      form.select("input[type=submit], button[type=submit]").toSeq match {
        case Seq() =>
          throw new IllegalArgumentException("The provided form has no submit buttons")
        case Seq(b) => b
        case _ =>
          throw new IllegalArgumentException("The provided form has multiple submit buttons; " +
            "not sure which to \"click\"")
      }

    submitFormVia(submitButton, context, params)(f)
  }

  protected def submitFormVia[A](button: Element, context: Option[String],
                                 params: Params)(f: => A): A = {

    if (!Seq("input", "button").contains(button.tagName) || button.attr("type") != "submit")
      throw new IllegalArgumentException("The given element is not a submit button: " + button)

    val form: Element = firstMatchingAncestor(button)(_.tagName == "form").
      getOrElse(throw new IllegalArgumentException(
        "The provided `button` is not contained within a form"))

    val availableFields = form.select(s"input, textarea, select").map(_.attr("name"))
    params.foreach { case (name, _) =>
      // TODO: For radio-button fields, ensure specified value is one of the provided options.
      // TODO: For select fields, ensure specified value is one of the provided options.
      // TODO: For checkbox fields, ensure the specified value is "on" (or null?).
      assert(availableFields.contains(name),
        s"Form must have field named '$name' but only following fields found: " +
          availableFields.mkString(", "))
    }

    // TODO: Support case where 'action' contains a full URL or absolute path.
    val action = form.attr("action")
    val uri =
      if (action.toLowerCase.startsWith("http://") || action.toLowerCase.startsWith("https://"))
        fail("Forms with URL in 'action' attribute not presently supported")
      else if (action.startsWith("./"))
        context.map { p =>
          val prefix = if (p.endsWith("/")) p else new java.io.File(p).getParent + "/"
          prefix + action.stripPrefix("./")
        }.getOrElse(fail("Cannot submit form with 'action' having relative path when no " +
                         "'context' was provided"))
      else
        action

    val submitButtonVal: Option[(String, String)] =
      Option(button.attr("name")).filterNot(_ == "").map(n =>
        n -> Option(button.attr("value")).getOrElse(""))
    val valuesToSubmit = (defaultValuesForForm(form).toMap ++ params.toMap).toSeq ++
      submitButtonVal
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

  @tailrec
  private def firstMatchingAncestor(elem: Element)(qualifier: Element => Boolean): Option[Element] =
    elem.parent match {
      case null => None
      case parent: Element =>
        if (qualifier(parent))
          Some(parent)
        else
          firstMatchingAncestor(parent)(qualifier)
    }

  // TODO: Add support for default values in other control/input types.
  protected def defaultValuesForForm(form: Element): Params =
    form.select("input[value]").
      filter { i: Element => Seq("text", "hidden").contains(i.attr("type")) }.
      map(i => (i.attr("name"), i.attr("value"))) ++
    form.select("textarea").map(ta => (ta.attr("name"), ta.text)) ++
    form.select("select").flatMap { s =>
      s.select("option").sortBy(o => if (o.hasAttr("selected")) 1 else 2).
        headOption.map(o => (s.attr("name"), if (o.hasAttr("value")) o.attr("value") else o.text))
    }

  /** Submit the given `form` represented as a Jsoup `Element`, using the given `params`.
    */
  protected def submitForm[A](form: Element, params: (String, String)*)(f: => A): A =
    submitForm(form, None, params)(f)

  /** Submit the form that contains `button` using said `button` (by "clicking" that button).
    */
  protected def submitFormVia[A](button: Element, params: Params)(f: => A): A =
    submitFormVia(button, context = None, params)(f)

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

  protected def isRedirectResponse: Boolean =
    status >= 300 && status < 400

  protected def redirectLocation: String = {
    val location = header.getOrElse("Location",
      throw new Exception(s"$status response with no Location header?"))
    if (location.startsWith("/"))
      location
    else {
      val newURL = new URL(location)
      if (newURL.getHost != "localhost")
        throw new Exception("Location header was not pointed at localhost!")
      val q = if (newURL.getQuery == null) "" else "?" + newURL.getQuery
      // XXX: Should we include the fragment?
      newURL.getPath + q
    }
  }

  type Params = Seq[(String, String)]
}
