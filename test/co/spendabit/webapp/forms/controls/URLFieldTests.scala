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
    val f = getField(requireProtocol = false)
    assert(f.validate("my-hostname").isLeft)
    assert(f.validate("my-hostname.a").isLeft)
    assert(f.validate("my-hostname.us").isRight)
  }

  def getField(requireProtocol: Boolean) =
    new URLField(label = "Your website", name = "website", requireProtocol = requireProtocol)
}
