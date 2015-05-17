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

  private def page(content: xml.NodeSeq) = {
    contentType = "text/html"
    <html><body>{ content }</body></html>.toString()
  }
}
