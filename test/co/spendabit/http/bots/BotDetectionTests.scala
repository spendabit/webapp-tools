package co.spendabit.http.bots

import org.scalatest.funsuite.AnyFunSuite

class BotDetectionTests extends AnyFunSuite {

  test("detecting various and sundry bot User-Agent strings") {
    val botUAs = Seq(
      "mfibot/1.1 (http://www.mfisoft.ru/analyst/; <admin@mfisoft.ru>; en-RU)",
      "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
      "Wotbox/2.01 (+http://www.wotbox.com/bot/)"
    )
    botUAs.foreach { ua =>
      assert(looksLikeBotUserAgentString(ua).contains(true),
        s"The following User-Agent string should be identified as that of a bot: $ua")
    }
  }
}
