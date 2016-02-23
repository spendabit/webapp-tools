package co.spendabit

package object html {

  def linkifyURLs(content: String): xml.NodeSeq = {
    val words = content.split(' ').map(_.trim).filter(_ != "").map { w =>
      if (w.startsWith("http://") || w.startsWith("https://") || w.startsWith("www."))
        <a href={ (if (w.startsWith("www.")) "http://" else "") + w } target="_blank">{ w }</a>
      else
        <xml:group>{ w }</xml:group>
    }
    words.foldLeft(xml.NodeSeq.Empty) { case (a, b) => a :+ <xml:group> </xml:group> :+ b }
  }
}