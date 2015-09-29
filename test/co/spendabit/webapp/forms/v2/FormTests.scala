package co.spendabit.webapp.forms.v2

import java.net.URL
import javax.mail.internet.InternetAddress

import co.spendabit.webapp.forms.controls._
import co.spendabit.webapp.forms.ui.bootstrap
import org.scalatest.FunSuite

class FormTests extends FunSuite {

  trait PostWebForm[T] extends BaseWebForm[T] {
    def method = POST
    def action = "/post-here"
  }

  test("basic form rendering") {

    val f = new WebForm3[InternetAddress, String, String]
              with PostWebForm[(InternetAddress, String, String)] {
      def fields =
        (new EmailField(label = "Email address", name = "theEmail"),
         new PasswordInput(label = "Password", name = "pass", minLength = 7),
         new Textarea(label = "Your story", name = "story"))
    }
    val markup = f.html

    assert(containsInputWithName(markup, "theEmail"))
    assert(containsInputWithName(markup, "pass"))
    assert(containsInputWithName(markup, "story", nodeType = "textarea"))

    assert((markup \\ "label").length > 0)
  }

  test("rendering with entered values") {

    val f = new WebForm3[InternetAddress, String, String] {
      def action = "./"
      def method = POST
      def fields = (new EmailField(label = "Email address", name = "theEmail"),
                    new TextInput(label = "Your name", name = "nombre"),
                    new Textarea(label = "Your story", name = "story"))
    }

    val enteredValues = Map("theEmail" -> "not-valid", "nombre" -> "José", "story" -> "a bold tale")
    val markup = f.html(enteredValues.map(x => (x._1, Seq(x._2))))

    enteredValues.foreach { case (n, v) =>
      assert(getControlValue(markup, name = n) === Some(v))
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

  test("functionality of checkbox field") {

    val form = new PostWebForm[Boolean] with WebForm1[Boolean] {
      def fields = Checkbox(label = "Check here if you like Moesha.", name = "moesha")
    }

    val box = getControl(form.html, "moesha")
    assert(getAttr(box, "checked").isEmpty,
      "Checkbox should not have 'checked' attribute by default")

    val boxChecked = getControl(form.html(Map("moesha" -> Seq("on"))), "moesha")
    assert(getAttr(boxChecked, "checked").isDefined,
      "Checkbox should retain state when it is 'on'")

    val validatedWithCheck = form.validate(Map("moesha" -> Seq("on")))
    assert(validatedWithCheck.isValid,
      "Checkbox should validate successfully if it's checked (\"on\")")
    assert(validatedWithCheck.asInstanceOf[Valid[Boolean]].values,
      "Checkbox should yield `true` value if it was checked")

    val validatedWithoutCheck = form.validate(Map.empty[String, Seq[String]])
    assert(validatedWithoutCheck.isValid,
      "Checkbox should validate successfully if it's not checked")
    assert(!validatedWithoutCheck.asInstanceOf[Valid[Boolean]].values,
      "Checkbox should yield `false` value if it was not checked")
  }

  test("functionality of select field") {

    sealed trait Fruit { def name: String }
    object Apple  extends Fruit { val name = "Apple" }
    object Orange extends Fruit { val name = "Orange" }
    object Banana extends Fruit { val name = "Banana" }

    object FruitSelector extends SelectField[Fruit]("Favorite fruit", name = "fruit",
                                                    options = Seq(Apple, Orange, Banana)) {
      protected def optionValue(opt: Fruit): String = opt.name.toLowerCase
      protected def optionLabel(opt: Fruit): String = opt.name
    }

    val form = new PostWebForm[Fruit] with WebForm1[Fruit] {
      def fields = FruitSelector
    }

    form.validate(Map("fruit" -> Seq("banana"))) match {
      case Invalid(_) => fail("Form should have validated")
      case Valid(v) => assert(v == Banana)
    }

    val rendered = form.html(params = Map("fruit" -> Seq("orange")))
    val opt = (rendered \\ "option").filter(o => getAttr(o, "value") == Some("orange")).head
    assert(getAttr(opt, "selected").isDefined)
    assert(getAttr(opt, "selected") == Some("selected"))
  }

  test("basic validation") {

    val f = new WebForm2[String, URL] with PostWebForm[(String, URL)] {
      def fields = (new TextInput(label = "Your name", name = "n"),
                    new URLField(label = "Your website", name = "website"))
    }

    assert(f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("https://test.net"))).isValid)
    assert(!f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("nada"))).isValid)
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

  test("use of 'Optional' field") {

    val f = new PostWebForm[Option[InternetAddress]] with WebForm1[Option[InternetAddress]] {
      def fields = Optional(EmailField(label = "Email", name = "email"))
    }

    val noParams = Map[String, Seq[String]]()
    assert(f.validate(noParams).isValid)
    assert(f.validate(Map("email" -> Seq(""))).isValid)
    assert(f.validate(Map("email" -> Seq(" \t"))).isValid)
    assert(f.validate(Map("email" -> Seq("jason@superstar.com"))).isValid)
    assert(!f.validate(Map("email" -> Seq("jason"))).isValid)
  }

  test("form is given proper 'enctype'") {

    val formWithNoFileInput = new PostWebForm[String] with WebForm1[String] {
      def fields = TextInput(label = "Enter a value", name = "the-value")
    }
    val f = formWithNoFileInput.html.asInstanceOf[xml.Elem]
    assert(f.label == "form")
    getAttr(f, "enctype").foreach { enc => assert(enc != "multipart/form-data") }

    pending
    // TODO: Make sure it uses 'enctype=multipart/form-data' if there's no 'file' input.
  }

  test("rendering using `FormRenderer` instance") {

    val renderer = new bootstrap.HorizontalForm
    val form = new PostWebForm[(String, URL)] with WebForm2[String, URL] {
      def fields = (new TextInput(name = "name", label = "Name"),
                    new URLField(name = "website", label = "Website"))
    }

    val formHTML = form.html(renderer).head
    assert(getAttr(formHTML, "action").isDefined)
    assert(getAttr(formHTML, "method").map(_.toLowerCase) == Some("post"))
    assert((formHTML \\ "button").length > 0)

    val formWithValues = form.html(renderer, Map("website" -> Seq("www.sing.com")))
    val websiteInput = getInput(formWithValues, "website")
    assert(getAttr(websiteInput, "value") == Some("www.sing.com"))
  }

//  implicit def toMapOfStringToSeqString(m: Map[String, String]): Map[String, Seq[String]] =
//    m.map(x => (x._1, Seq(x._2)))

  test("code for WebFormX classes (e.g. `WebForm1`, `WebForm2`, etc) is properly generated") {
    val form = new PostWebForm[(String, URL, InternetAddress, String)]
            with WebForm4[String, URL, InternetAddress, String] {
      def fields = (new Textarea(name = "f1", label = "Field 1"),
                    new URLField(name = "f2", label = "Field 2"),
                    new EmailField(name = "f2", label = "Field 3"),
                    new TextInput(name = "f2", label = "Field 4"))
    }
    form.html
  }

  private def getControlValue(html: xml.NodeSeq, name: String): Option[String] = {
    val control = getControl(html, name)
    control.label match {
      case "input"    => getAttr(control, "value")
      case "textarea" => Some(control.child.text)
      case l          => fail(s"Unsupported control type: $l")
    }
  }

  private def containsInputWithName(html: xml.NodeSeq, name: String,
                                    nodeType: String = "input"): Boolean =
    (html \\ nodeType).filter(n => getAttr(n, "name") == Some(name)).length == 1

  private def getInput(html: xml.NodeSeq, name: String): xml.Node =
    getControl(html, name, Seq("input"))

  private def getControl(html: xml.NodeSeq, name: String,
                         nodeTypes: Seq[String] = Seq("input", "textarea")): xml.Node = {

    val allControls = nodeTypes.map(t => html \\ t).flatten
    allControls.find(n => getAttr(n, "name") == Some(name)).
      getOrElse(fail(s"No form-control with name '$name' found"))
  }

  private def getAttr(n: xml.Node, attr: String): Option[String] =
    n.attribute(attr).map(_.headOption).flatten.map(_.toString())
}