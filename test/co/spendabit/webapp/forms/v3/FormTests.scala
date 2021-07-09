package co.spendabit.webapp.forms.v3

import co.spendabit.webapp.forms.v3.controls.{Control, EmailAddr, Password, Textarea}
import co.spendabit.webapp.forms.{Invalid, Valid}
import org.scalatest.FunSuite

import java.time.LocalDateTime
import scala.language.higherKinds

class FormTests extends FunSuite with FormTestHelpers {

  test("basic form rendering") {

    val f = WebForm3(
      Field(label = "Email address", EmailAddr),
       Field(label = "Password", Password(minLength = 7)),
       Field(label = "Your story", Textarea))
    val markup = html(f)

    assert(containsInputWithName(markup, "email-address"))
    assert(containsInputWithName(markup, "password"))
    assert(containsInputWithName(markup, "your-story", nodeType = "textarea"))

    assert((markup \\ "label").length > 0)
  }

  test("support for 'placeholder' text") {

    case class CustomField[T](override val label: String, placeholder: String,
                              override val control: Control[T])
      extends Field[T](label = label, control)

    // This custom `FormRenderer` will include a 'placeholder' attribute for inputs.
    object CustomRenderer extends DefaultFormRenderer[CustomField] {
      override def labeledControl(field: CustomField[_], control: xml.NodeSeq): xml.NodeSeq =
        super.labeledControl(field, withAttr(control, "placeholder", field.placeholder))
    }

    val f = WebForm3(
        CustomField(label = "Email address", placeholder = "john@example.org", controls.EmailAddr),
        CustomField(label = "Name", placeholder = "John Smith", controls.TextLine),
        CustomField(label = "Comments", placeholder = "Talk to us.", controls.Textarea))
    val markup: xml.NodeSeq = html(f, renderer = CustomRenderer)

    assert((markup \\ "input").length == 2)
    (markup \\ "input").foreach { i =>
      val inputName = getAttr(i, "name").get
      if (inputName == "email-address")
        assert(getAttr(i, "placeholder").contains("john@example.org"))
      else if (inputName == "name")
        assert(getAttr(i, "placeholder").contains("John Smith"))
      else
        fail("Found unexpected input: " + i)
    }
    assert(getAttr((markup \\ "textarea").head, "placeholder").contains("Talk to us."))
  }

  test("rendering with entered values") {

    val f = new WebForm3(
      Field(label = "Email address", controls.EmailAddr),
      Field(label = "Your name", controls.TextLine),
      Field(label = "Your story", controls.Textarea))

    val enteredValues = Map("email-address" -> "not-valid", "your-name" -> "José", "your-story" -> "a bold tale")
    val markup = html(f, enteredValues)

    enteredValues.foreach { case (n, v) =>
      assert(getControlValue(markup, name = n).isDefined)
      assert(getControlValue(markup, name = n).contains(v))
    }
  }

  test("form using all-custom HTML with a radio-button set") {

    val form = WebForm1(
      Field(label = "Your desire?", controls.TextLine))

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

    val form = WebForm1(
      Field(label = "Vegetarian", controls.Checkbox))

    val box = getControl(html(form), "vegetarian")
    assert(getAttr(box, "checked").isEmpty,
      "Checkbox should not have 'checked' attribute by default")

    val boxChecked = getControl(html(form, Map("vegetarian" -> "on")), "vegetarian")
    assert(getAttr(boxChecked, "checked").isDefined,
      "Checkbox should retain state when it is 'on'")

    val validatedWithCheck = form.validate(Map("vegetarian" -> Seq("on")))
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
      override protected def errorOnEmpty: String =
        "Please select your preferred fruit."
    }

    val form = WebForm1(
      Field(label = "Favorite fruit", FruitSelector))

    form.validate(Map("favorite-fruit" -> Seq("banana"))) match {
      case Invalid(_) => fail("Form should have validated")
      case Valid(v) => assert(v == Banana)
    }

    form.validate(Map[String, Seq[String]]()) match {
      case Invalid(errors) => errors.foreach(m => assert(!m.toLowerCase.contains("xx")))
      case Valid(_) => fail("Form should not validate with no value for select field")
    }

    val rendered = html(form, values = Map("favorite-fruit" -> "orange"))
    val opt = (rendered \\ "option").filter(o => getAttr(o, "value").contains("orange")).head
    assert(getAttr(opt, "selected").isDefined)
    assert(getAttr(opt, "selected").contains("selected"))
  }

  test("basic validation") {

    val f = WebForm2(
      Field(label = "Name", controls.TextLine),
      Field(label = "Website", controls.URL()))

    assert(f.validate(Map("name" -> Seq("Fred"), "website" -> Seq("https://test.net"))).isValid)
    assert(!f.validate(Map("name" -> Seq("Fred"), "website" -> Seq("nada"))).isValid)
  }

  test("whitespace is trimmed and whitespace-only is considered empty, for text fields") {

    val f = WebForm1(Field(label = "Zodiac Sign", controls.TextLine))

    assert(!f.validate(Map("zodiac-sign" -> Seq(" "))).isValid)
    assert(f.validate(Map("zodiac-sign" -> Seq("Pisces"))).isValid)

    f.validate(Map("zodiac-sign" -> Seq("\tGemini "))) match {
      case Valid(v) => assert(v == "Gemini")
      case _ => fail("Form did not validate!")
    }

//    assert(getInput(html(f), "temp") != null)
  }

  test("cross-field validations") {
    val form = new WebForm2(
        Field(label = "Password",         controls.Password(minLength = 3)),
        Field(label = "Confirm", controls.Password(minLength = 3))) {
      override protected def crossFieldValidations = Seq(
        { case (p1, p2) => if (p1 != p2) Some("Passwords must match.") else None }
      )
    }

    assert(!form.validate(Map("password" -> Seq("something"), "confirm" -> Seq("other.thing"))).isValid)
    assert(form.validate(Map("password" -> Seq("same.thing"), "confirm" -> Seq("same.thing"))).isValid)
  }

  test("use of 'Optional' field") {

    val f = new WebForm1(Field(label = "Email", controls.Optional(controls.EmailAddr)))

    val noParams = Map[String, Seq[String]]()
    assert(f.validate(noParams).isValid)
    assert(f.validate(Map("email" -> Seq(""))).isValid)
    assert(f.validate(Map("email" -> Seq(" \t"))).isValid)
    assert(f.validate(Map("email" -> Seq("jason@superstar.com"))).isValid)
    assert(!f.validate(Map("email" -> Seq("jason"))).isValid)
  }

  test("use of `HiddenInput`") {

    val f = WebForm2(
        Field(label = "x", controls.HiddenInput),
        Field(label = "Color", controls.TextLine))

    assert(f.validate(Map("x" -> "whatever", "color" -> "Orange")).isValid)

    val html = f.html(action = "/here", method = "post",
      new DefaultFormRenderer[Field], Map("x" -> Seq("hidden value!"))).head
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

    val f = new WebForm1(Field(label = "Your website", controls.URL()))

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

    val renderer = new DefaultFormRenderer[Field]
    val form = WebForm2(
      Field(label = "Name", controls.TextLine),
      Field(label = "Website", controls.URL()))

    val formHTML = form.html(action = "/submit", method = "POST", renderer, params = Map()).head
    assert(getAttr(formHTML, "action").isDefined)
    assert(getAttr(formHTML, "method").map(_.toLowerCase).contains("post"))
    assert((formHTML \\ "button").length > 0)

    val formWithValues = form.html(action = "/go", method = "POST",
      renderer, Map("website" -> Seq("www.sing.com")))
    val websiteInput = getInput(formWithValues, "website")
    assert(getAttr(websiteInput, "value").contains("www.sing.com"))
  }

  // `WebForm1` is a bit of an edge-case, and requires slightly different logic from
  // our "macro" (code generation) as that required for `WebForm2` and on up.
  test("`WebForm1` works properly") {

    val f = new WebForm1(Field(label = "Temperature", controls.TextLine))

    // Previously, an issue with the way we defined `WebForm1` (using a val in place of def) led
    // to a `NullPointerException`.
    try html(f)
    catch { case e: Throwable =>
      fail(s"Rendering the `html` should not lead to exception (${e.getClass.getName})")
    }

    assert(getInput(html(f), "temperature") != null)
  }

  test("code for WebFormX classes (e.g. `WebForm2`, `WebForm3`, etc) is properly generated") {
    val form = new WebForm4(
      Field(label = "Field 1", controls.Textarea),
      Field(label = "Field 2", controls.URL()),
      Field(label = "Field 3", controls.EmailAddr),
      Field(label = "Field 4", controls.TextLine))
    html(form)
  }

  class DefaultFormRenderer[F[_] <: Field[_]] extends FormRenderer[F] {

    def formElem(labeledControls: xml.NodeSeq): xml.Elem =
      <form>
        { labeledControls }
        { submitSection }
      </form>

    def labeledControl(field: F[_], control: xml.NodeSeq): xml.NodeSeq =
      if (isCheckbox(control)) {
        <div class="checkbox"> <label>{ control } { field.label }</label> </div>
      } else {

        val widget =
          if (isFileInput(control))
            control
          else
            withAttr(control, "class", "form-control")

        <div class="form-group">
          <label>{ field.label }</label> { widget }
        </div>
      }

    protected def submitSection: xml.NodeSeq =
      <button type="submit" class="btn btn-primary">{ submitButtonLabel }</button>

    protected def submitButtonLabel: String = "Submit"
  }

  private def html[F[_] <: Field[_], T](form: BaseWebForm[F, T], values: Map[String, String] = Map()): xml.NodeSeq =
    html(form, renderer = new DefaultFormRenderer, values)

  private def html[F[_] <: Field[_], T](form: BaseWebForm[F, T], renderer: FormRenderer[F]): xml.NodeSeq =
    html(form = form, renderer = renderer, values = Map())

  private def html[F[_] <: Field[_], T](form: BaseWebForm[F, T], renderer: FormRenderer[F],
                   values: Map[String, String]): xml.NodeSeq =
    form.html(action = "/post-here", method = "POST", renderer = renderer, params = values)

  protected def randomInt(max: Int): Int =
    scala.util.Random.nextInt(max) + 1

  private implicit def toParams(map: Map[String, String]): Map[String, Seq[String]] =
    map.map(x => x._1 -> Seq(x._2))
}