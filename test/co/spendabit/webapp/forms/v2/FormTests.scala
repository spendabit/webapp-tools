package co.spendabit.webapp.forms.v2

import java.net.URL
import javax.mail.internet.InternetAddress

import co.spendabit.webapp.forms.controls._
import org.scalatest.FunSuite

class FormTests extends FunSuite {

  abstract case class PostWebForm[T](action: String = "/post-here")
          extends BaseWebForm[T] {
    def method = POST
  }

  test("rendering with entered values") {

    val f = new BaseWebForm[(InternetAddress, String)] with WebForm2[InternetAddress, String] {
      def action = "./"
      def method = POST
      def fields = (new EmailField(label = "Email address", name = "theEmail"),
                    new TextInput(label = "Your story", name = "story"))
    }
    val markup = f.html(Map("theEmail" -> Seq("not-valid"), "story" -> Seq("a bold tale")))

    Seq("theEmail" -> "not-valid", "story" -> "a bold tale").foreach { case (n, v) =>
      assert(getAttr(getInput(markup, name = n), "value") === Some(v))
    }
  }

  test("form using all-custom HTML with a radio-button set") {

    val form = new PostWebForm[String] with WebForm1[String] with CustomHtml[String] {

      def fields = new TextInput(name = "desire", label = "Your desire?")

      def customHtml =
        <form method="post" action="/here">
          What is your desire?
          <div><input type="radio" name="desire" value="moon" /> Go to the moon</div>
          <div><input type="radio" name="desire" value="other-planet" /> Go to another planet</div>
          <div><input type="radio" name="desire" value="earth" /> Stay here</div>
          <div><input type="submit" value="Go" /></div>
        </form>
    }

    val markup = form.html(Map("desire" -> Seq("other-planet")))
    val otherPlanet = (markup \\ "input").find(n => getAttr(n, "value") == Some("other-planet")).get
    val checked = getAttr(otherPlanet, "checked")
    assert(checked == Some("checked"))

    // Make sure the other checkboxes weren't checked...
    Seq("moon", "earth").foreach { opt =>
      val box = (markup \\ "input").find(n => getAttr(n, "value") == Some(opt)).get
      assert(getAttr(box, "checked").isEmpty)
    }
  }

  test("cross-field validations") {
    val form = new PostWebForm[(String, String)] with WebForm2[String, String] {
      protected def fields = (new PasswordInput(name = "pass1", label = "Password",
                                minLength = 3),
                              new PasswordInput(name = "pass2", label = "Confirm Password",
                                minLength = 3))
      override protected def crossFieldValidations = Seq(
        { case (p1, p2) => if (p1 != p2) Some("Passwords must match.") else None }
      )
    }

    assert(!form.validate(Map("pass1" -> Seq("something"), "pass2" -> Seq("other.thing"))).isValid)
    assert(form.validate(Map("pass1" -> Seq("same.thing"), "pass2" -> Seq("same.thing"))).isValid)
  }

  test("generated WebFormX code...") {
    val form = new PostWebForm[(String, URL, InternetAddress, String)]
            with WebForm4[String, URL, InternetAddress, String] {
      def fields = (new Textarea(name = "f1", label = "Field 1"),
                    new URLField(name = "f2", label = "Field 2"),
                    new EmailField(name = "f2", label = "Field 3"),
                    new TextInput(name = "f2", label = "Field 4"))
    }
    form.html
  }

  private def getInput(html: xml.NodeSeq, name: String): xml.Node =
    (html \\ "input").find(n => getAttr(n, "name") == Some(name)).
      getOrElse(fail(s"No input with name '$name' found"))

  private def getAttr(n: xml.Node, attr: String): Option[String] =
    n.attribute(attr).map(_.headOption).flatten.map(_.toString())
}