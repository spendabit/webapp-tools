package co.spendabit.webapp.forms.v2

import javax.mail.internet.InternetAddress

import co.spendabit.webapp.forms.controls._
//import co.spendabit.webapp.forms.v2.{BaseWebForm, WebForm2}
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

  private def getInput(html: xml.NodeSeq, name: String): xml.Node =
    (html \\ "input").find(n => getAttr(n, "name") == Some(name)).
      getOrElse(fail(s"No input with name '$name' found"))

  private def getAttr(n: xml.Node, attr: String): Option[String] =
    n.attribute(attr).map(_.headOption).flatten.map(_.toString())
}