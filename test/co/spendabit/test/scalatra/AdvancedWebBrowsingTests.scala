package co.spendabit.test.scalatra

import org.jsoup.Jsoup
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

    info("should accept explicitly specified value")
    get("/form-with-textarea") {
      submitSoleForm("comments" -> "time for a refill") {
        assert(body.contains("time for a refill"))
      }
    }

    info("should use the default value when no value is explicitly specified")
    get("/form-with-textarea", "default" -> "coffee gone") {
      submitSoleForm() {
        assert(body.contains("coffee gone"))
      }
    }
  }

  test("'submitForm' properly supports 'select' controls") {

    info("should accept explicitly specified value")
    get("/form-with-select-control") {
      submitSoleForm("gender" -> "f") {
        assert(body.contains("gender: f"))
      }
    }

    info("should submit the default 'selected' value if no value is explicitly specified")
    get("/form-with-select-control") {
      submitSoleForm() {
        assert(body.contains("gender: ?"))
      }
    }
  }

  /** To behave inline with popular browsers (Firefox and Chrome were explicitly tested), a
    * <select> element that has no child-elements of type <option> should not cause an empty-string
    * value to be sent to the server when submitting the containing form, but it should rather
    * lead to *no* parameter being submitted at all.
    */
  test("select element with no options does not lead to an empty parameter") {
    get("/form-with-empty-select-control") {
      submitSoleForm() {
        assert(body.trim == "")
      }
    }
  }

  test("the text content of the default option element is used when element has no " +
       "'value' attribute") {
    val defaults = defaultValuesForForm(Jsoup.parse(
      "<form><select name=\"position\"><option selected=\"selected\">1</option></select></form>"))
    val p = defaults.find(_._1 == "position")
    assert(p.isDefined)
    assert(p.get._2 == "1")
  }

  test("the value for the first option element is used if no option elements have " +
       "'selected' attribute") {
    val defaults = defaultValuesForForm(Jsoup.parse(
      """<form><select name="roast">
        |  <option value="light">Light Roast</option>
        |  <option value="dark" >Dark Roast</option>
        |</select></form>""".stripMargin))
    val p = defaults.find(_._1 == "roast")
    assert(p.isDefined)
    assert(p.get._2 == "light")
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

  test("'submitForm' properly submits form with 'multipart/form-data' enctype") {
    get("/form-with-multipart-enctype") {
      submitSoleForm() {
        assert(body.contains("multipart/form-data"))
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
    val default = params.getOrElse("default", "")
    page(
      <form method="post" action="/echo-params">
        <textarea name="comments">{ default }</textarea> <button type="submit">Go</button>
      </form>)
  }

  get("/form-with-select-control") {
    page(
      <form method="post" action="/echo-params">
        <select name="gender">
          <option value="m">Boy</option>
          <option value="f">Girl</option>
          <option value="?" selected="selected">Unsure</option>
        </select>
        <button type="submit">Go</button>
      </form>)
  }

  get("/form-with-empty-select-control") {
    page(
      <form method="post" action="/echo-params">
        <select name="no-contest"> </select>
        <button type="submit">Go</button>
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

  get("/form-with-multipart-enctype") {
    page(
      <form method="post" action="/show-content-type" enctype="multipart/form-data">
        <input type="hidden" name="field" value="abc" />
        <button type="submit">Go!</button>
      </form>)
  }

  post("/show-content-type") {
    plainText(request.contentType.getOrElse(""))
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
