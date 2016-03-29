package co.spendabit.http

import javax.servlet.http.HttpServletRequest

import org.slf4j.LoggerFactory

package object bots {

  private lazy val log = LoggerFactory.getLogger("co.spendabit.http.bots")

  /** Does the given `request` appear to have originated from a bot (e.g. Googlebot or Twitterbot)?
    * If we are not able to draw any conclusions based on the User-Agent string (e.g., because it's
    * for a browser or a bot not in our "database"), return `None`.
    */
  def looksLikeBotRequest(request: HttpServletRequest): Option[Boolean] =
    Option(request.getHeader("User-Agent")) match {

      case None =>
        log.debug("Request contained no 'User-Agent' header, so assuming it's a bot")
        Some(true)

      case Some(ua) =>
        looksLikeBotUserAgentString(ua)
    }

  def looksLikeBotUserAgentString(ua: String): Option[Boolean] =
    if (botSignatures.exists(ua.contains) || ua.contains("bot/")) {
      log.debug(s"User-Agent string appears to be from a bot: $ua")
      Some(true)
    } else if (ordinaryBrowserSignatures.exists(ua.contains)) {
      log.debug(s"Found signature for known web-browser in User-Agent string: $ua")
      Some(false)
    } else {
      None
    }

  private val ordinaryBrowserSignatures = Seq(
    "BlackBerry", "Chrome", "Firefox", "MSIE", "Opera", "Presto", "Safari", "Trident")

  private val botSignatures = Seq(
    "008", "ABACHOBot", "Accoona-AI-Agent", "AddSugarSpiderBot", "AnyApexBot",
    "Apache-HttpClient", "Arachmo",
    "B-l-i-t-z-B-O-T", "Baiduspider", "BecomeBot", "BeslistBot", "BillyBobBot", "Bimbot",
    "Bingbot", "BlitzBOT", "boitho.com-dc", "boitho.com-robot", "btbot", "CatchBot",
    "Cerberian Drtrs", "Charlotte", "ConveraCrawler", "cosmos", "Covario IDS",
    "Crowsnest", "dataminr.com", "DataparkSearch",
    "DiamondBot", "Discobot", "Dotbot", "Embedly", "EmeraldShield.com WebBot",
    "EsperanzaBot", "Exabot", "FAST Enterprise Crawler", "FAST-WebCrawler", "FDSE robot",
    "FindLinks", "FurlBot", "FyberSpider", "g2crawler", "Gaisbot", "GalaxyBot", "genieBot",
    "Gigabot", "Girafabot", "Googlebot", "Googlebot-Image", "GurujiBot", "HappyFunBot",
    "hl_ftien_spider", "Holmes", "htdig", "iaskspider", "ia_archiver", "iCCrawler", "ichiro",
    "igdeSpyder", "IRLbot", "IssueCrawler", "jack", "Jaxified Bot", "Jyxobot",
    "KoepaBot", "L.webis",
    "LapozzBot", "Larbin", "LDSpider", "LexxeBot", "Linguee Bot", "LinkWalker", "lmspider",
    "lwp-trivial", "mabontland", "magpie-crawler", "Mediapartners-Google", "MetaURI", "MJ12bot",
    "Mnogosearch", "mogimogi", "MojeekBot", "Moreoverbot", "Morning Paper", "msnbot", "MSRBot",
    "MVAClient", "mxbot", "NetResearchServer", "NetSeer Crawler", "NewsGator",
    "NING", "NG-Search",
    "nicebot", "noxtrumbot", "Nusearch Spider", "NutchCVS", "Nymesis", "obot", "oegp",
    "omgilibot", "OmniExplorer_Bot", "OOZBOT", "Orbiter", "PageBitesHyperBot", "Peew",
    "PercolateCrawler",
    "polybot", "Pompos", "PostPost", "Psbot", "PycURL", "python-requests", "Qseero",
    "Radian6", "RAMPyBot", "Readability", "Ruby",
    "RufusBot", "SandCrawler", "SBIder", "ScoutJet", "Scrubby", "SearchSight", "Seekbot",
    "semanticdiscovery", "Sensis Web Crawler", "Bot", "SeznamBot", "Shim-Crawler", "ShopWiki",
    "Shoula robot", "silk", "Sitebot", "Snappy", "sogou spider", "Sosospider", "Speedy Spider",
    "Sqworm", "StackRambler", "suggybot", "SurveyBot", "SynooBot", "Teoma", "TerrawizBot",
    "TheSuBot", "Thumbnail.CZ robot", "TinEye", "truwoGPS", "TurnitinBot", "TweetedTimes Bot",
    "TwengaBot", "Twitterbot", "updated", "Urlfilebot", "Vagabondo", "VoilaBot", "Vortex",
    "voyager", "VYU2",
    "webcollage", "Websquash.com", "wf84", "WoFindeIch Robot", "WomlpeFactory",
    "Xaldon_WebSpider", "yacy", "Yahoo! Slurp", "Yahoo! Slurp China", "YahooSeeker",
    "YahooSeeker-Testing", "YandexBot", "YandexImages", "Yasaklibot", "Yeti", "YodaoBot",
    "yoogliFetchAgent", "YoudaoBot", "Zao", "Zealbot", "zspider", "ZyBorg")
}
