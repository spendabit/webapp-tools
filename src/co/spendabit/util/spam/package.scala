package co.spendabit.util

import java.net.URL

import javax.mail.internet.InternetAddress

package object spam {

  def looksLikeSpam(from: InternetAddress, message: String): Boolean =
    spamScore(from, message) > 2.0

  def spamScore(from: InternetAddress, message: String): Double = {

    log.debug("Analyzing following content for likely spam: " + message)

    val msg = Message(from, body = message)
    log.debug(s"  Using ${strategies.length} strategies to assess likelihood of spam...")
    val strikes: Double = strategies.map { s =>
      val points = s.points(msg)
      log.debug(s"    Strategy '${s.name}': " + points)
      points
    }.sum
    log.debug("  Total strikes: " + strikes)

    strikes
  }

  private val strategies = Seq(

    Strategy("has sender with no capital letters in name", { m =>
      val from = m.from
      val noCapsInName = from.getPersonal != null && from.getPersonal != "" &&
        from.getPersonal.count(_.isUpper) == 0
      if (noCapsInName) 0.25 else 0
    }),

    Strategy("has sender name with more than five words", { m =>
      val moreThanFive = m.from.getPersonal.split(' ').length > 5
      if (moreThanFive) 0.3 else 0
    }),

    Strategy("has sender with CamelCase name", { m =>
      val hasCamelCase = m.from.getPersonal.split(' ').exists(_.count(c => c.isUpper) > 1)
      if (hasCamelCase) 0.2 else 0.0
    }),

    Strategy("has sender name that does not appear in email-address handle", { m =>
      val emailHandle = m.from.getAddress.split('@').head.toLowerCase
      if (m.nameParts.length >= 2 && !m.nameParts.exists(n => emailHandle.contains(n.toLowerCase)))
        0.2
      else
        0.0
    }),

    Strategy("uses address from Gmail, Hotmail, Yahoo, or other popular free email service", { m =>
      val host = m.from.getAddress.split('@').last
      val triggerHosts = Seq("aol.com", "gmail.com", "hotmail.com", "yahoo.com", "gmx.de")
      if (triggerHosts.contains(host)) 0.15 else 0.0
    }),

    Strategy("uses 'no reply' email address", { m =>
      val hasNoReply = m.from.getAddress.contains("no") && m.from.getAddress.contains("reply")
      if (hasNoReply) 0.4 else 0.0
    }),

    Strategy("has URL for sender", { m =>
      if (m.from.getPersonal.matches("^http(s?)://[a-zA-Z0-9]+\\.[a-z]+/.*$")) 0.8 else 0
    }),

    Strategy("has word 'buy' in name", { m =>
      if (m.from.getPersonal != null && m.from.getPersonal.contains("buy")) 0.75 else 0
    }),

    Strategy("uses the word 'i' in lower-case", { m =>
      if (m.words.contains("i")) 0.25 else 0
    }),

    Strategy("has no capital letters in message", { m =>
      if (m.body.count(_.isUpper) == 0)
        0.6
      else
        0
    }),

    Strategy("has ALL-CAPS words", { m =>
      val allCapsWords = m.words.
        filter(w => w.count(c => !c.isLower) == w.length && w.length >= 5)
      math.min(allCapsWords.length / 2.5, 1.5)
    }),

    Strategy("has single line", { m =>
      if (m.lines.length == 1)
        0.6
      else
        0
    }),

    Strategy("has lines with excess capitalized words (e.g., 'Title Cased Sentences')", { m =>
      val linesWithExcessCapitalizedWords = m.lines.count { l =>
        val wordsOnLine = l.split(' ').map(_.trim).filterNot(_.isEmpty)
        wordsOnLine.count(_.head.isUpper) > wordsOnLine.length / 2.0 && wordsOnLine.length > 3
      }
      linesWithExcessCapitalizedWords.toDouble / m.lines.length
    }),

    // The idea behind this strategy is to try to catch nonsense text, frequently used by
    // Cialis pushers.
    // XXX: Keep this strategy? If so, properly build it out...
    Strategy("has multiple prepositions in a row", { m =>
      if (m.words.containsSlice(Seq("at", "of")))
        0.5
      else
        0
    }),

    Strategy("has improper use of 'an'", { m =>
      m.words.zipWithIndex.filter(_._1 == "an").count { case (_, offset) =>
        val wordAfter = m.words.drop(offset + 1).headOption
        wordAfter.exists(w => !Seq('a', 'e', 'h', 'i', 'o', 'u').contains(w.head))
      } * 0.2
    }),

    Strategy("has hard-coded trigger-words", { m =>
      def countTriggerWords(words: Seq[String]) =
        triggerWords.map(tw => words.count(_.toLowerCase == tw)).sum
      countTriggerWords(m.from.getPersonal.split(' ')) * 1.5 + (countTriggerWords(m.words) / 2.0)
    }),

    Strategy("has hard-coded trigger-sequences", { m =>
      triggerMarkup.count(m.body.contains) / 2.0
    }),

    Strategy("has sloppy (double) spaces", { m =>
      if (m.lines.exists(_.trim.contains("  "))) 0.5 else 0
    }),

    Strategy("lacks punctuation", { m =>
      val lacksPunctuation = !m.tokens.exists { w =>
        Seq('.', '!', '?').contains(w.lastOption.getOrElse(' '))
      }
      if (lacksPunctuation) 0.6 else 0.0
    }),

    Strategy("uses lots of exclamation points", { m =>
      pointsForExcess(count = m.body.count(_ == '!'), excessAfter = 1, pointsPerIncident = 0.2)
    }),

    Strategy("has flaky commas", { m =>
      if (flakyCommasRegex.findFirstMatchIn(m.body).isDefined)
        0.6
      else
        0.0
    }),

    Strategy("has more commas than other punctuation", { m =>
      if (m.tokens.count(_.last == ',') > m.tokens.count(t => Seq('.', '!', ':').contains(t.last)))
        0.5
      else
        0
    }),

    // TODO: Rework this strategy to analyze lines *within* paragraphs, rather than based on
    // TODO: what characters lines end with.
    Strategy("has bizarre tendency to use line-breaks after periods, " +
             "but not related to line length", { m =>
      val linesEndingWithDot = m.lines.map(_.trim).
        filter { l => l.nonEmpty && l.split(' ').length >= 5 &&
          (l.endsWith(".") || l.last.isLetterOrDigit) }
      val lengths = linesEndingWithDot.map(_.length)

      if (linesEndingWithDot.length < 2) {
        0
      } else {
        val lineLengthVariance = (lengths.max - lengths.min) / lengths.min.toDouble
        if (lineLengthVariance > 0.5)
          0.2
        else
          0
      }
    }),

    Strategy("has dollar signs", { m =>
      m.body.count(_ == '$')
    }),

    Strategy("has lots of percent-values", { m =>
      pointsForExcess(m.tokens.count(_.matches("^\\+?[0-9]+%$")),
        excessAfter = 0, pointsPerIncident = 0.2)
    }),

    Strategy("has URLs", { m =>
      val numURLs = urlRegex.findAllMatchIn(m.body).length
      math.min(math.max(0, numURLs - 0.5), 2)
    }),

    Strategy("has 'short-link' URL", { m =>
      val numShorties = m.urls.count { u =>
        u.getHost == "bit.ly" ||
          (u.getHost.matches(".*\\.[a-z]{2}$") && u.getPath.length <= 7 &&
            u.getPath.exists(_.isLetter) && u.getPath.exists(_.isDigit))
      }
      if (numShorties > 0)
        0.5
      else
        0
    }),

    Strategy("has URLs with multi-part paths and/or queries", { m =>
      val foundMultiPartPath = m.urls.exists(_.getPath.count(_ == '/') > 1)
      val foundQuery = m.urls.exists(_.getQuery != null)
      (if (foundMultiPartPath) 0.1 else 0.0) + (if (foundQuery) 0.1 else 0.0)
    }),

    Strategy("has URL with word 'sex' in hostname", { m =>
      m.urls.count(_.getHost.contains("sex")) * 0.3
    }),

    Strategy("has Google Drive URL", { m =>
      if (m.body.contains("//drive.google.com/"))
        0.6
      else
        0
    }),

    Strategy("ends with URL", { m =>
      if (m.tokens.takeRight(1).exists(w => w.startsWith("http:") || w.startsWith("https:")))
        0.2
      else
        0
    }),

    // This would include, for example, a message that starts with "Hi there", without a
    // comma at the end of the line.
    Strategy("has salutation or closing with no punctuation", { m =>
      val candidates = m.salutation ++ m.closing
      val numWithoutPunctuation = candidates.count { s =>
        s.last != ',' && s.last != '.'
      }
      if (candidates.nonEmpty && numWithoutPunctuation == candidates.size) 0.41 else 0
    }),

    Strategy("has salutation including 'sirs'", { m =>
      if (m.salutation.map(_.toLowerCase.stripSuffix(",")).contains("dear sirs"))
        0.75
      else
        0
    }),

    Strategy("has name in signature that doesn't match the sender", { m =>
      val nameParts = Option(m.from.getPersonal).getOrElse("").split(' ').toSeq
      if (m.signature.isDefined && nameParts.nonEmpty &&
          !nameParts.exists(n => m.signature.exists(_.contains(n))))
        0.5
      else
        0
    }),

    Strategy("ends message with colon followed by email address", { m =>
      m.tokens.takeRight(2) match {
        case Seq(secondTo, last) =>
          if (secondTo.endsWith(":") && last.contains("@"))
            0.35
          else
            0
        case _ => 0
      }
    }),

    Strategy("includes email address in message body that does not match the sender", { m =>
      val emailAddrs = "[+-_.a-zA-Z0-9]+@[-.a-zA-Z0-9]+".r.findAllMatchIn(m.body).map(_.group(0))
      if (emailAddrs.exists(_ != m.from.getAddress)) 0.35 else 0
    })
  )

  case class Message(from: InternetAddress, body: String) {

    lazy val nameParts: Array[String] = from.getPersonal.split(' ').map(_.trim)

    lazy val lines: Seq[String] = body.split("\n")

    /** The `tokens` are like all the words in the message, but with symbols and other non-space
      * characters retained.
      */
    lazy val tokens: Seq[String] = lines.flatMap(_.split(' ').map(_.trim)).filterNot(_ == "")

    /** And the `words` are the `tokens` with punctuation removed, etc.
      */
    lazy val words: Seq[String] = tokens.map(w => stripPunctuation(w.trim)).
      filterNot(w => w == "" || !w.exists(_.isLetterOrDigit))

    lazy val urls: Seq[URL] = tokens.filter(_.matches("^http(s)?://[-a-zA-Z0-9]+\\.[a-z]+/.*")).
      map(u => new URL(u))

    lazy val salutation: Option[String] = lines.headOption.filter(l =>
      l.startsWith("Hi") || l.startsWith("Hello") || l.startsWith("Dear"))

    /** By "closing" we mean the line (if any) that contains "sincerely" or "regards" or "thanks".
      */
    lazy val closing: Option[String] = lines.reverse.find { l =>
      val words = l.toLowerCase.split(' ').map(_.stripSuffix("s"))
      words.length <= 3 && closingWords.exists(cw => words.contains(cw))
    }

    private val closingWords = Seq("thank", "regard", "best")

    /** `signature` is supposed to contain the name with which the email is signed.
      */
    lazy val signature: Option[String] =
      lines.filterNot(_.trim == "").dropWhile(l => !closing.contains(l)).drop(1).headOption
  }

  private def pointsForExcess(count: Int, excessAfter: Int, pointsPerIncident: Double) =
    math.min(math.max(0, count - excessAfter) * pointsPerIncident, 1.0)

  private def stripPunctuation(word: String) = word.reverse.dropWhile(!_.isLetterOrDigit).reverse

  private case class Strategy(name: String, points: Message => Double)

  private lazy val triggerMarkup = Seq("|", "==>", "<a ", "[url", "start your free trial")

  private lazy val triggerWords = Seq("cialis", "sildenafil", "viagra", "tadalafil",
    "invest", "investing", "investment")

  private lazy val urlRegex = "\\s+(http(s)?://|www\\.)?[-.a-zA-Z0-9]{3,}\\.[a-z]{2,3}".r

  private lazy val flakyCommasRegex = ",[a-zA-Z]".r

  private lazy val log = org.log4s.getLogger
}
