package co.spendabit.webapp.ui

import java.net.URL

trait WebFriendlyHtmlPage {

  protected def lang: String

  protected def title: String

  /** A full URL works better for the favicon <link/> tags, as some services don't seem to
    * properly deal with tags that have only a file-path in the 'href' attribute.
    */
  protected def faviconURL: URL

  protected def bodyContent: xml.NodeSeq

  protected def extraHeadContent = xml.NodeSeq.Empty

  def xhtml =
    <html lang={ lang }>

      <head>
        <title>{ title }</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta charset="utf-8" />
        <link rel="icon"          href={ faviconURL.toString } type="image/png" />
        <link rel="shortcut icon" href={ faviconURL.toString } type="image/png" />
        <meta name="viewport"
              content="width=device-width, initial-scale=1.0, maximum-scale=1, user-scalable=no" />
        { extraHeadContent }
      </head>

      <body>{ bodyContent }</body>

    </html>

  override def toString = "<!DOCTYPE html>\n" + xhtml.toString()
}
