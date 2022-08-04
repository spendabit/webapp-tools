package co.spendabit.webapp.forms.v2

import java.net.URL
import java.time.LocalDateTime

import javax.mail.internet.InternetAddress
import co.spendabit.webapp.forms.{Invalid, Valid}
import co.spendabit.webapp.forms.controls._
import co.spendabit.webapp.forms.ui.bootstrap
import org.scalatest.funsuite.AnyFunSuite

class FormTests extends AnyFunSuite with FormTestHelpers {

  test("basic form rendering") {

    val f = new WebForm3[InternetAddress, String, String]
              with PostWebForm[(InternetAddress, String, String)] {
      def fields =
        (EmailField(label = "Email address", name = "theEmail"),
         PasswordInput(label = "Password", name = "pass", minLength = 7),
         Textarea(label = "Your story", name = "story"))
    }
    val markup = f.html

    assert(containsInputWithName(markup, "theEmail"))
    assert(containsInputWithName(markup, "pass"))
    assert(containsInputWithName(markup, "story", nodeType = "textarea"))

    assert((markup \\ "label").length > 0)
  }

  test("support for 'placeholder' text") {

    val f = new WebForm3[InternetAddress, String, String]
      with PostWebForm[(InternetAddress, String, String)] {
      def fields =
        (EmailField(label = "Email address", name = "e", placeholder = "john@example.org"),
          TextInput(label = "Name", name = "n", placeholder = "John Smith"),
          Textarea(label = "Comments", name = "comments", placeholder = "Talk to us."))
    }
    val markup = f.html

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
      def action = "./"
      def method = POST
      def fields = (EmailField(label = "Email address", name = "theEmail"),
                    TextInput(label = "Your name", name = "nombre"),
                    Textarea(label = "Your story", name = "story"))
    }

    val enteredValues = Map("theEmail" -> "not-valid", "nombre" -> "José", "story" -> "a bold tale")
    val markup = f.html(enteredValues.map(x => (x._1, Seq(x._2))))

    enteredValues.foreach { case (n, v) =>
      assert(getControlValue(markup, name = n) === Some(v))
    }
  }

  test("form using all-custom HTML with a radio-button set") {

    val form = new PostWebForm[String] with WebForm1[String] with CustomHtml[String] {

      def fields = TextInput(name = "desire", label = "Your desire?")

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
    val opt = (rendered \\ "option").filter(o => getAttr(o, "value").contains("orange")).head
    assert(getAttr(opt, "selected").isDefined)
    assert(getAttr(opt, "selected").contains("selected"))
  }

  test("basic validation") {

    val f = new WebForm2[String, URL] with PostWebForm[(String, URL)] {
      def fields = (TextInput(label = "Your name", name = "n"),
                    URLField(label = "Your website", name = "website"))
    }

    assert(f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("https://test.net"))).isValid)
    assert(!f.validate(Map("n" -> Seq("Fred"), "website" -> Seq("nada"))).isValid)
  }

  test("whitespace is trimmed and whitespace-only is considered empty, for text fields") {

    val f = new WebForm1[String] {
      def action = "./"
      def method = POST
      def fields = TextInput(label = "Celestial Sign", name = "sign")
    }

    assert(!f.validate(Map("sign" -> Seq(" "))).isValid)
    assert(f.validate(Map("sign" -> Seq("Pisces"))).isValid)

    f.validate(Map("sign" -> Seq("\tGemini "))) match {
      case Valid(v) => assert(v == "Gemini")
      case _ => fail("Form did not validate!")
    }
  }

  test("cross-field validations") {
    val form = new PostWebForm[(String, String)] with WebForm2[String, String] {
      protected def fields = (PasswordInput(name = "pass1", label = "Password",
                                minLength = 3),
                              PasswordInput(name = "pass2", label = "Confirm Password",
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

  test("use of `HiddenInput`") {

    val f = new PostWebForm[(String, String)] with WebForm2[String, String] {
      def fields = (
        HiddenInput("x"),
        TextInput(label = "Fav color", name = "color"))
    }

    assert(f.validate(Map("x" -> "whatever", "color" -> "Orange")).isValid)

    val renderer = new bootstrap.HorizontalForm
    val html = f.html(renderer, Map("x" -> Seq("hidden value!"))).head
    assert((html \\ "label").length == 1)
  }

  test("validation using `EmailField`") {
    val f = EmailField(label = "E-mail", name = "e")
    pending
    f.validate("john@árbolito.com").right.map { e =>
      // If the address is accepted, then it should be converted using "Punycode".
      assert(e.getAddress == "john@xn--rbolito-gwa.com")
    }
  }

  test("validate using `URLField`") {

    val f = new WebForm1[URL] with PostWebForm[URL] {
      def fields = URLField(label = "Your website", name = "website")
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

    val input = DateTimeInput("Send at", name = "dt")
    assert(input.validate(Map("dt" -> Seq(dateStr + " " + timeStr))).isRight)
  }

  test("rendering using `FormRenderer` instance") {

    val renderer = new bootstrap.HorizontalForm
    val form = new PostWebForm[(String, URL)] with WebForm2[String, URL] {
      def fields = (TextInput(name = "name", label = "Name"),
                    URLField(name = "website", label = "Website"))
    }

    val formHTML = form.html(renderer).head
    assert(getAttr(formHTML, "action").isDefined)
    assert(getAttr(formHTML, "method").map(_.toLowerCase).contains("post"))
    assert((formHTML \\ "button").length > 0)

    val formWithValues = form.html(renderer, Map("website" -> Seq("www.sing.com")))
    val websiteInput = getInput(formWithValues, "website")
    assert(getAttr(websiteInput, "value").contains("www.sing.com"))
  }

//  implicit def toMapOfStringToSeqString(m: Map[String, String]): Map[String, Seq[String]] =
//    m.map(x => (x._1, Seq(x._2)))

  // `WebForm1` is a bit of an edge-case, and does not use the same "macro" (code generation)
  // as `WebForm2` and on up.
  test("`WebForm1` works properly") {

    val f = new PostWebForm[String] with WebForm1[String] {
      val fields = TextInput(label = "Temperature", name = "temp")
    }

    // Previously, an issue with the way we defined `WebForm1` (using a val in place of def) led
    // to a `NullPointerException`.
    try f.html
    catch { case e: Throwable =>
      fail(s"Rendering the `html` should not lead to exception (${e.getClass.getName})")
    }

    assert(getInput(f.html, "temp") != null)
  }

  test("code for WebFormX classes (e.g. `WebForm2`, `WebForm3`, etc) is properly generated") {
    val form = new PostWebForm[(String, URL, InternetAddress, String)]
            with WebForm4[String, URL, InternetAddress, String] {
      def fields = (Textarea(name = "f1", label = "Field 1"),
                    URLField(name = "f2", label = "Field 2"),
                    EmailField(name = "f2", label = "Field 3"),
                    TextInput(name = "f2", label = "Field 4"))
    }
    form.html
  }

  protected def randomInt(max: Int) =
    scala.util.Random.nextInt(max) + 1

  private implicit def toParams(map: Map[String, String]): Map[String, Seq[String]] =
    map.map(x => x._1 -> Seq(x._2))
}