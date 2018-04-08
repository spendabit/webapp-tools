package co.spendabit.webapp.forms.controls

import org.scalatest.FunSuite

class URLFieldTests extends FunSuite {

  test("protocol does not need to be provided if 'requireProtocol' flag is false") {
    val result = getField(requireProtocol = false).validate("www.my-website.com")
    assert(result.isRight)
    val url = result.right.get
    assert(url.getProtocol.startsWith("http"))
    assert(url.getHost == "www.my-website.com")
  }

  test("protocol MUST be provided if 'requireProtocol' flag is true") {
    assert(getField(requireProtocol = true).validate("www.my-website.com").isLeft)
  }

  test("a hostname with a TLD is required") {
    assert(!validates("my-hostname"))
    assert(!validates("my-hostname.a"))
    assert(validates("my-hostname.us"))
  }

  test("URL with a path is accepted") {
    assert(validates("http://welcome.net/path/to/a_file.html"))
  }

  test("an email address is not accepted") {
    assert(!validates("rosanne@hotmail.com"))
    assert(!validates("jpopper@go-big.co.uk/something"))
  }

  test("does not allow two dots in a row") {
    assert(!validates("https://bogus..com"))
    assert(!validates("http://.whoa.net"))
  }

  private def validates(value: String, requireProtocol: Boolean = false) =
    getField(requireProtocol).validate(value).isRight

  private def getField(requireProtocol: Boolean) =
    URLField(label = "Your website", name = "website", requireProtocol = requireProtocol)
}
