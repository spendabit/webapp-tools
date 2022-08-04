package co.spendabit.html

import org.scalatest.funsuite.AnyFunSuite

class LinkifyTextTests extends AnyFunSuite {

  test("linkifying text") {

    val text =
      """
        |Come check out our awesome website: https://big-fun.com
        |
        |Also, find more here: http://hello.net
        |
        |Come visit www.barnyard.com to learn more.
      """.stripMargin
    val linkified = linkifyURLs(text)

    assert(countLinks(linkified) == 3)
  }

  test("it should still find a link that's on its own line") {
    val text =
      """
        |Here it is:
        |http://myline.com/
      """.stripMargin
    assert(countLinks(linkifyURLs(text)) == 1)
  }

  test("it should retain all lines") {
    val text =
      """
        |This is line 1...
        |...and we have a 2nd line.
        |Plus one more.
      """.stripMargin
    val processed = linkifyURLs(text).toString()

    assert(processed.contains("This is line 1"))
    assert(processed.contains("and we have a 2nd line"))
    assert(processed.contains("Plus one more"))
  }

  private def countLinks(html: xml.NodeSeq) =
    html.toString().split("<a").length - 1
    // XXX: Why doesn't this work?
    // html \\ "a"
}