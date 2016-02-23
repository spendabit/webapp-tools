package co.spendabit.html

import org.scalatest.FunSuite

class LinkifyTextTests extends FunSuite {

  test("linkifying text") {

    val text =
      """
        |Come check out our awesome website: https://big-fun.com
        |
        |Also, find more here: http://hello.net
        |
        |Come visit www.barnyard.com to learn more.
      """.stripMargin
    val linkified = co.spendabit.html.linkifyURLs(text)

    assert(countLinks(linkified) == 3)
  }

  private def countLinks(html: xml.NodeSeq) =
    html.toString().split("<a").length - 1
    // XXX: Why doesn't this work?
    // html \\ "a"
}