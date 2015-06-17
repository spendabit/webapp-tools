package co.spendabit.test.scalatra

import org.scalatest.FunSuite
import org.scalatra.ScalatraServlet

class AdvancedWebBrowsingTests extends FunSuite with AdvancedWebBrowsing {

  addServlet(classOf[TestServlet], "/*")

  test("AdvancedWebBrowsing.submitForm uses HTTP 'method' specified in form attribute") {

    get("/form-using-get") {
      submitForm(getForm("form[method=get]"), "q" -> "whatever") {
        assert(body == "GET")
      }
    }

    get("/form-using-post") {
      submitForm(getForm("form[method=post]"), "q" -> "whatever") {
        assert(body == "POST")
      }
    }
  }

  test("'submitForm' properly submits values for 'hidden' fields/inputs") {
    get("/form-with-hidden-field") {
      submitForm(getForm("form")) {
        assert(body.contains("hi: don't forget me!"))
      }
    }
  }

  test("'submitForm' properly submits values for 'textarea' elements") {
    get("/form-with-textarea") {
      submitForm(getForm("form"), "comments" -> "time for a refill") {
        assert(body.contains("time for a refill"))
      }
    }
  }

  test("AdvancedWebBrowsing.submitForm ensures the provided form actually has a 'submit' button") {
    class ItSubmitted extends Exception
    get("/form-with-no-submit-button") {
      try {
        submitForm(getForm("form"), "theField" -> "a value") {}
        throw new ItSubmitted
      } catch {
        case _: ItSubmitted =>
          fail("The form had no 'submit' button, so should not have been submittable")
        case _: org.scalatest.exceptions.TestFailedException =>
        // We'll take it.
      }
    }
  }

  test("'submitForm' properly handles forms that use relative path 'action' attribute") {
    get("/site-section/the-form") {
      submitForm(getForm("form"), context = Some("/site-section/the-form"), Seq()) {
        assert(status >= 200 && status < 300)
        assert(body.contains("Submitted!"))
      }
    }
  }

  test("getFollowingRedirects properly handles redirect locations with query-string") {
    getFollowingRedirects("/a") {
      assert(body == "with is c")
    }
  }
}

class TestServlet extends ScalatraServlet {

  get("/form-using-:method") {
    page(<form method={ params("method") } action="/submit">
      <input type="text" name="q" /> <button type="submit">Go</button>
    </form>)
  }

  get("/submit") {
    contentType = "text/plain"
    "GET"
  }

  post("/submit") {
    contentType = "text/plain"
    "POST"
  }

  get("/form-with-hidden-field") {
    page(<form method="post" action="/echo-params">
      <input type="hidden" name="hi" value="don't forget me!" /> <button type="submit">Go</button>
    </form>)
  }

  get("/form-with-textarea") {
    page(<form method="post" action="/echo-params">
      <textarea name="comments"></textarea> <button type="submit">Go</button>
    </form>)
  }

  post("/echo-params") {
    contentType = "text/plain"
    params.map(p => p._1 + ": " + p._2).mkString("\n")
  }

  get("/form-with-no-submit-button") {
    page(
      <form method="post" action="/submit">
        <input type="text" name="theField" />
        <button class="useless">Useless Button!</button>
      </form>)
  }

  get("/site-section/the-form") {
    page(
      <form method="post" action="./submit-to">
        <button type="submit">Submit</button>
      </form>)
  }

  post("/site-section/submit-to") {
    plainText("Submitted!")
  }

  get("/a") {
    redirect("/b?with=c")
  }

  get("/b") {
    contentType = "text/plain"
    "with is " + params.get("with").getOrElse("")
  }

  private def plainText(text: String) = {
    contentType = "text/plain"
    text
  }

  private def page(content: xml.NodeSeq) = {
    contentType = "text/html"
    <html><body>{ content }</body></html>.toString()
  }
}
