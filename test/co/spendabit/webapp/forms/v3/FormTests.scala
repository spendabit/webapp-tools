package co.spendabit.webapp.forms.v3

import java.net.URL
import java.time.LocalDateTime

import co.spendabit.webapp.forms.ui.{FormRenderer, bootstrap}
import co.spendabit.webapp.forms.ui.bootstrap.BasicForm
import co.spendabit.webapp.forms.v3.controls.{Control, EmailAddr, Password, Textarea}
import co.spendabit.webapp.forms.{Invalid, Valid}
import javax.mail.internet.InternetAddress
import org.scalatest.FunSuite

class FormTests extends FunSuite with FormTestHelpers {

  test("basic form rendering") {

    val f = new WebForm3[InternetAddress, String, String]
              /*with PostWebForm[(InternetAddress, String, String)]*/ {
      def fields =
        (Field(label = "Email address", EmailAddr),
         Field(label = "Password", Password(minLength = 7)),
         Field(label = "Your story", Textarea))
    }
    val markup = html(f)

    assert(containsInputWithName(markup, "theEmail"))
    assert(containsInputWithName(markup, "pass"))
    assert(containsInputWithName(markup, "story", nodeType = "textarea"))

    assert((markup \\ "label").length > 0)
  }

  test("support for 'placeholder' text") {

    case class F[T](override val label: String,
                    placeholder: String,
                    override val control: Control[T])
      extends Field[T](label = label, control)

    val f = new WebForm3[InternetAddress, String, String] {
      def fields = (
        F(label = "Email address", placeholder = "john@example.org", controls.EmailAddr),
        F(label = "Name", placeholder = "John Smith", controls.TextLine),
        F(label = "Comments", placeholder = "Talk to us.", controls.Textarea))
    }
    val markup = html(f)

    (markup \\ "input").foreach { i =>
      if (getAttr(i, "name").get == "e")
        assert(getAttr(i, "placeholder").contains("john@example.org"))
      else
        assert(getAttr(i, "placeholder").contains("John Smith"))
    }
    assert(getAttr((markup \\ "textarea").head, "placeholder").contains("Talk to us."))
  }

  test("rendering with entered values") {

    val f = new WebForm3[InternetAddress, String, String] {
      def fields = (
        Field(label = "Email address", controls.EmailAddr),
        Field(label = "Your name", controls.TextLine),
        Field(label = "Your story", controls.Textarea))
    }

    val enteredValues = Map("theEmail" -> "not-valid", "nombre" -> "José", "story" -> "a bold tale")
    val markup = html(f, enteredValues)

    enteredValues.foreach { case (n, v) =>
      assert(getControlValue(markup, name = n).isDefined)
      assert(getControlValue(markup, name = n).contains(v))
    }
  }

  test("form using all-custom HTML with a radio-button set") {

    val form = new WebForm1[String] {
      def fields = Field(label = "Your desire?", controls.TextLine)
    }

    val formMarkup =
      <form method="post" action="/here">
        What is your desire?
        <div><input type="radio" name="desire" value="moon" /> Go to the moon</div>
        <div><input type="radio" name="desire" value="other-planet" /> Go to another planet</div>
        <div><input type="radio" name="desire" value="earth" /> Stay here</div>
        <div><input type="submit" value="Go" /></div>
      </form>

    val markup = form.fillFields(formMarkup, Map("desire" -> Seq("other-planet")))
    val otherPlanet = (markup \\ "input").
      find(n => getAttr(n, "value").contains("other-planet")).get
    val checked = getAttr(otherPlanet, "checked")
    assert(checked.contains("checked"))

    // Make sure the other checkboxes weren't checked...
    Seq("moon", "earth").foreach { opt =>
      val box = (markup \\ "input").find(n => getAttr(n, "value").contains(opt)).get
      assert(getAttr(box, "checked").isEmpty)
    }
  }

  test("functionality of checkbox field") {

    val form = new WebForm1[Boolean] {
      def fields = Field(label = "Check here if you like Moesha.", controls.Checkbox)
    }

    val box = getControl(html(form), "moesha")
    assert(getAttr(box, "checked").isEmpty,
      "Checkbox should not have 'checked' attribute by default")

    val boxChecked = getControl(html(form, Map("moesha" -> "on")), "moesha")
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

    object FruitSelector extends controls.SelectField[Fruit](
            options = Seq(Apple, Orange, Banana)) {
      protected def optionValue(opt: Fruit): String = opt.name.toLowerCase
      protected def optionLabel(opt: Fruit): String = opt.name
    }

    val form = new WebForm1[Fruit] {
      def fields = Field(label = "Favorite fruit", FruitSelector)
    }

    form.validate(Map("fruit" -> Seq("banana"))) match {
      case Invalid(_) => fail("Form should have validated")
      case Valid(v) => assert(v == Banana)
    }

    val rendered = html(form, values = Map("fruit" -> "orange"))
    val opt = (rendered \\ "option").filter(o => getAttr(o, "value").contains("orange")).head
    assert(getAttr(opt, "selected").isDefined)
    assert(getAttr(opt, "selected").contains("selected"))
  }

  test("basic validation") {

    val f = new WebForm2[String, URL] {
      def fields = (
        Field(label = "Your name", controls.TextLine),
        Field(label = "Your website", controls.URL()))
    }

    assert(f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("https://test.net"))).isValid)
    assert(!f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("nada"))).isValid)
  }

  test("whitespace is trimmed and whitespace-only is considered empty, for text fields") {

    val f = new WebForm1[String] {
      def fields = Field(label = "Zodiac Sign", controls.TextLine)
    }

    assert(!f.validate(Map("sign" -> Seq(" "))).isValid)
    assert(f.validate(Map("sign" -> Seq("Pisces"))).isValid)

    f.validate(Map("sign" -> Seq("\tGemini "))) match {
      case Valid(v) => assert(v == "Gemini")
      case _ => fail("Form did not validate!")
    }
  }

  test("cross-field validations") {
    val form = new WebForm2[String, String] {
      protected def fields = (
        Field(label = "Password",         controls.Password(minLength = 3)),
        Field(label = "Confirm Password", controls.Password(minLength = 3)))
      override protected def crossFieldValidations = Seq(
        { case (p1, p2) => if (p1 != p2) Some("Passwords must match.") else None }
      )
    }

    assert(!form.validate(Map("pass1" -> Seq("something"), "pass2" -> Seq("other.thing"))).isValid)
    assert(form.validate(Map("pass1" -> Seq("same.thing"), "pass2" -> Seq("same.thing"))).isValid)
  }

  test("use of 'Optional' field") {

    val f = new WebForm1[Option[InternetAddress]] {
      def fields = Field(label = "Email", controls.Optional(controls.EmailAddr))
    }

    val noParams = Map[String, Seq[String]]()
    assert(f.validate(noParams).isValid)
    assert(f.validate(Map("email" -> Seq(""))).isValid)
    assert(f.validate(Map("email" -> Seq(" \t"))).isValid)
    assert(f.validate(Map("email" -> Seq("jason@superstar.com"))).isValid)
    assert(!f.validate(Map("email" -> Seq("jason"))).isValid)
  }

  test("use of `HiddenInput`") {

    val f = new WebForm2[String, String] {
      def fields = (
        Field(label = "x", controls.HiddenInput),
        Field(label = "Fav color", controls.TextLine))
    }

    assert(f.validate(Map("x" -> "whatever", "color" -> "Orange")).isValid)

    val renderer = new bootstrap.HorizontalForm
    val html = f.html(action = "/here", method = "post",
      renderer, Map("x" -> Seq("hidden value!"))).head
    assert((html \\ "label").length == 1)
  }

  test("validation of `EmailAddr` control") {
    val f = controls.EmailAddr
    pending
    f.validate("john@árbolito.com").right.map { e =>
      // If the address is accepted, then it should be converted using "Punycode".
      assert(e.getAddress == "john@xn--rbolito-gwa.com")
    }
  }

  test("validate using `URLField`") {

    val f = new WebForm1[URL] {
      def fields = Field(label = "Your website", controls.URL())
    }

    val invalid = Seq(
      "my-site", "//impossible. com", "www.//Theimprovement. Com", "jokes .org",
      "://somewhere.co.uk", "great.co-")
    invalid.foreach { u =>
      assert(!f.validate(Map("website" -> Seq(u))).isValid,
        "Following should be considered invalid: " + u)
    }
  }

  test("date/time validation using `DateTimeInput`") {

    val dt = LocalDateTime.now()
    val dateStr = Seq(
      dt.getYear.toString,
      (if (dt.getMonthValue < 10) "0" else "") + dt.getMonthValue,
      randomInt(17) + 10).mkString("-")
    val timeStr = (randomInt(13) + 10) + ":" + (randomInt(45) + 10)

    val input = controls.DateTimeInput
    assert(input.validate(dateStr + " " + timeStr).isRight)
  }

  test("rendering using `FormRenderer` instance") {

    val renderer = new bootstrap.HorizontalForm
    val form = new WebForm2[String, URL] {
      def fields = (
        Field(label = "Name", controls.TextLine),
        Field(label = "Website", controls.URL()))
    }

    val formHTML = html(form, renderer).head
    assert(getAttr(formHTML, "action").isDefined)
    assert(getAttr(formHTML, "method").map(_.toLowerCase).contains("post"))
    assert((formHTML \\ "button").length > 0)

    val formWithValues = form.html(action = "/go", method = "POST",
      renderer, Map("website" -> Seq("www.sing.com")))
    val websiteInput = getInput(formWithValues, "website")
    assert(getAttr(websiteInput, "value").contains("www.sing.com"))
  }

  // `WebForm1` is a bit of an edge-case, and does not use the same "macro" (code generation)
  // as `WebForm2` and on up.
  test("`WebForm1` works properly") {

    val f = new WebForm1[String] {
      val fields = Field(label = "Temperature", controls.TextLine)
    }

    // Previously, an issue with the way we defined `WebForm1` (using a val in place of def) led
    // to a `NullPointerException`.
    try html(f)
    catch { case e: Throwable =>
      fail(s"Rendering the `html` should not lead to exception (${e.getClass.getName})")
    }

    assert(getInput(html(f), "temp") != null)
  }

  test("code for WebFormX classes (e.g. `WebForm2`, `WebForm3`, etc) is properly generated") {
    val form = new WebForm4[String, URL, InternetAddress, String] {
      def fields = (
        Field(label = "Field 1", controls.Textarea),
        Field(label = "Field 2", controls.URL()),
        Field(label = "Field 3", controls.EmailAddr),
        Field(label = "Field 4", controls.TextLine))
    }
    html(form)
  }

  private def html(form: BaseWebForm[_], values: Map[String, String] = Map()): xml.NodeSeq =
    html(form, renderer = new BasicForm, values)

  private def html(form: BaseWebForm[_], renderer: FormRenderer): xml.NodeSeq =
    html(form, renderer, values = Map())

  private def html(form: BaseWebForm[_], renderer: FormRenderer,
                   values: Map[String, String]): xml.NodeSeq =
    form.html(action = "/post-here", method = "POST", renderer, values)

  protected def randomInt(max: Int) =
    scala.util.Random.nextInt(max) + 1

  private implicit def toParams(map: Map[String, String]): Map[String, Seq[String]] =
    map.map(x => x._1 -> Seq(x._2))
}