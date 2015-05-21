package co.spendabit.webapp.forms

import co.spendabit.webapp.forms.controls._
import org.scalatest.FunSuite

class FormTests extends FunSuite {

  test("rendering") {

    val f = WebForm3(new EmailField(label = "Email address", name = "theEmail"),
                     new PasswordInput(label = "Password", name = "pass", minLength = 7),
                     new Textarea(label = "Your story", name = "story"))
    val markup = f.html(action = "/submit-here", method = "post", values = None)

    assert(containsInputWithName(markup, "theEmail"))
    assert(containsInputWithName(markup, "pass"))
    assert(containsInputWithName(markup, "story", nodeType = "textarea"))

    assert((markup \\ "label").length > 0)
  }

  test("basic validation") {

    val f = WebForm2(new TextInput(label = "Your name", name = "n"),
                     new URLField(label = "Your website", name = "website"))

    assert(f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("https://test.net"))).isValid)
    assert(!f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("nada"))).isValid)
  }

  private def containsInputWithName(html: xml.NodeSeq, name: String,
                                    nodeType: String = "input"): Boolean =
    (html \\ nodeType).filter(n => getAttr(n, "name") == Some(name)).length == 1

  private def getAttr(n: xml.Node, attr: String): Option[String] =
    n.attribute(attr).map(_.headOption).flatten.map(_.toString())
}