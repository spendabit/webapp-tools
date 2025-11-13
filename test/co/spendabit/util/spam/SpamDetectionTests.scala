package co.spendabit.util.spam

import co.spendabit.util.spam

import javax.mail.internet.InternetAddress
import org.scalatest.funsuite.{AnyFunSuite => FunSuite}

class SpamDetectionTests extends FunSuite {

  test("detecting spam inquiries") {

    assertDetectedAsSpam(
      """
        |TURN $1 INTO $5, $10 â€¦ EVEN $50 ALL DAY LONG
        |Free Video Shows How To Make 5-50X ROI
        |In The Untapped Crypto Market With
        |This CRYPTOCURRENCY SOFTWARE ==> https://jvz4.com/c/1014397/294890
      """.stripMargin.trim)

    assertDetectedAsSpam(
      """
        |LEARN How to earn $500 per month from ClixSense !!
        |â€œMETHOD TESTED BY MEâ€ You can see there are number of ways in ClixSense to make money.
        |
        |I will explain you each of the method so that you can make a big income from ClixSense.
        |Your income will be slow for the first months and after that you will be able to make better income.
        |
        |Watch this video: http://bit.ly/Learn_ClixSense
      """.stripMargin.trim)

    assertDetectedAsSpam(
      """
        |How To Make $100 A Day For Beginners Using Google Email Alerts
        |I use this new way of creating wealth online to help others create a life they desire.
        |The results are outstanding.
        |Click here  ==> https://forms.aweber.com/form/83/1060297783.htm
      """.stripMargin)

    assertDetectedAsSpam(
      """
        |Make Your WordPress Sites up to 10x Faster In 30 Seconds With The Cache That Beats All Competitors !!
        |Speed Up Your WP Sites Before The July 2018 Google Speed Slap...
        |
        |Speed is a Traffic Killer... More Than 50% of Visitors Leave... If a Site Takes 3+ Seconds To Load
        |+ 79% of Visitors Who Have Trouble loading a Page Won't Return
        |
        |** GET ACCESS NOW ==> https://jvz4.com/c/1014397/301007
        |
        |
        |** GET FREE BONUSES **
        |- FREE BONUS 1 - WP Optimiser
        |  WP Optimiser is a combination of quick fixes & diagnostics that give you advice on how to fix your site speed issues - more importantly it covers all 6 Primary site optimisation factors - at best our competitors cover just 2 factors & charge more.
        |
        |- FREE BONUS 2 - WP Easy Pages
        |  OK it's no secret that WordPress is natively slow - the WP framework makes this worse - which is why it's not best fit for building landing pages
        |
        |- FREE BONUS 3 - Conversion Boost 2.0
        |  Creating split test pages & personalized pages is a pain in WordPress you literally have to duplicate & edit each page one by one. This puts your site at risk of getting a duplicate content penalty
    """.stripMargin.trim)

    assertDetectedAsSpam("For a limited time we have lowered the rate on targeted website " +
      "traffic. We have visitors from virtually every country on Earth. Each visitor is " +
      "targeted by both country and keywords that you submit when you start your free trial " +
      "period. If you need more visitors or product sales try our service free for seven days " +
      "and we will send you 500 free visitors during the trial. There are no contracts and if " +
      "you cancel during the trial period you will not be charged anything! Start your trial " +
      "today: http://insl.co/11" +
      "\n\r\n \n\n\t\n\n   \n" +
      "Unsubscribe here: http://stpicks.com/2ruse")

    assertDetectedAsSpam(
      """
        |Are you Tired of Not Getting Results From Your Videos?
        |
        |Revealed: The ONLY Web-App That Guarantees Your Videos Will Rank On Page 1 of Google In 48 Hours Or Less
        |Allowing you to generate targeted, BUYER traffic, sales and leads at will!
        |
        |GET NOW ==>>  http://bit.ly/Pro_X_Ranke
      """.stripMargin)

    assertDetectedAsSpam(
      """
        |Precious Sirs,
        |
        |This is Olivia from CHINA, Kidfun Inflatables firm, www.kidfuntoys.com
        |
        |We provide all sort of Blow up items
        |
        |Blow up bouncer.
        |Inflatable bouncy castle.
        |Inflatable damp & dry slide.
        |Inflatable camping tent.
        |Blow up sporting activity video games.
        |Water video games.
        |Zorb ball/Bubber sphere
        |
        |Please contact us for more products details if interesting
        |
        |B/R,
        |Olivia
        |Kidfun Inflatables Co.,Limited
        |Email:sales@kidfuntoys.com
        |SKYPE:oliveguanmuch
        |T +0086 20 34141973
        |M +Â 0086 15812416234
        |www.kidfuntoys.com
      """.stripMargin.trim)

    assertDetectedAsSpam(from = "FastLoanS <tihonovafeonilla92@mail.ru>",
      "Get a Brand New iPhone Xs Now! ==>> http://iPhoneXs.fastloans.icu")

    assertDetectedAsSpam(
      from = "Eliza Flores <flores.eliza@gmail.com>",
      """
        |Good to meet you!
        |
        |I am Matthew from Hong Kong and we are a manufacturer of hysteresis brakes as well as hysteresis dynamometers for motor examination, specifically for little to tool power motors. It is widely made use of in motor and electric manufacturing facilities/ lab to do the basic motor test to determine the torque, rate as well as power as it is practical and very easy to be used, and dependable and very accurate. Most notably, the cost is extremely competitive!
        |
        |http://www.validmagnetics.com/component/jshopping/hysteresis-dynamometer.html
        |
        |Hysteresis Dynamometer, Motor examination - Validmagnetics Ltd.
        |www.validmagnetics.com.
        |Hysteresis Dynamometer Overview Hysteresis Brake Dynamometer is designed for screening with low to middle power devices, specifically high-speed electric motor testing. Due to the fact that the source of torque created is not coming from rate, it is able to offer a complete dimension of a motor from free-running to rotor-locked.
        |
        |I wish to ask if you or your consumers are seeking hysteresis brakes/ dynamometer or have passion in trading these items?
        |
        |In terms of innovation and high quality owned, we are confident that we are the leading one in Asia.
        |
        |I am looking forward to listening to any comment from you.
        |
        |Best regards,
        |Matthew
        |Valid Magnetics Limited Room 604,
        |Leader Industrial Centre, Fo Tan,
        |Hong Kong
        |Telephone: 852-62908202
        |Email: info@validmagnetics.com
        |Website: www.validmagnetics.com
      """.stripMargin)

    assertDetectedAsSpam(
      from = "Rafaeldaype <dcbtcsystem@gmail.com>",
      """
        |Short-term investments in a global international project based on the blockchain platform!
        |The project is fully automated and decentralized!
        |
        |All bitcoins are stored on the accounts (bitcoin wallets) of the participants themselves and are transferred to each other using the Bitcoin cryptocurrency.
        |
        |+10% in 2 days!
        |+1500% per month
        |
        |3 levels of referral bonuses.
        |
        |5%
        |3%
        |1%
        |
        |Referral bonuses are paid the next day!
        |Official site:  https://www.crypto-mmm.site
        |
        |
        |Blockchain project based on mathematical algorithms of the Mavrodi brothers.
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "Nancy Werfel <nancy.werfel@outlook.com>",
      """
        |Hi
        |
        |I wanted to order a item from your webshop.
        |but I can not find the product anymore on your site,
        |it looks like this on this site https://bit.ly/ProductItem823
        |I hope you will sell the product again soon,
        |mail me if you are going to sell it again, I'll wait
        |
        |Greetings
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "Catherine Paul <paul.catherine@googlemail.com>",
      """
        |www.vicunapolohk.com - Bags at VICUNA POLO | Designer Brands Up to 75% Off | VicunaPoloHK
        |Discount designer handbags for men sale | Shop men high end handbag styles in quality materials. Buy luxury bag brands at outlet prices at VicunaPoloHK.
        |<a href="https://www.vicunapolohk.com/">bag brands</a>
        |
        |https://www.vicunapolohk.com/blog.html
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "Eric <info@fresh222.com>",
      """
        |Hello! I just want to contact you about the newest Jewelry Quick Inventoryt system  like on this VIDEO : https://www.youtube.com/watch?v=bDvTtwbh5QQ&t=102s
        |This is a very simple and inexpensive system. If you are interested, please write to this address: info@fresh222.us
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "Camilla Squire <lionleather101@gmail.com>",
      """
        |Hello,
        |
        |We are Juggling ball supplier, Vendor of many Juggling Brands. Whether you are interested in?
        |
        |Here you can find catalog of our juggling ball in Google drive link given below.
        |
        |https://drive.google.com/open?id=0B5meiXXKee54Y1RYaU9OczRlUjQ
        |
        |Price list is available on request.
        |
        |For quick communication you also can whatsaap   0092-3006122353
        |
        |PS: If you are already our customer then please ignore this email. Thanks!
        |
        |With Best Regards
        |Hassan
        |
        |
        |---
        |
        |Lion Leather
        |Sialkot Pakistan
        |Mob: 0092-3006122353
        |Whats Ap: 00923006122353  Viber: 00923006122353
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "Alexander <alexander@m2.getnippy.net>",
      """
        |Hi there
        |
        |Do you want to dramatically increase your leads & sales?
        |
        |Fixing a slow website has one of the highest ROI's.
        |
        |Watch the free training here: https://getnippy.net/training/
        |
        |Cheers
        |
        |Hi there
        |
        |Do you want to dramatically increase your leads & sales?
        |
        |Fixing a slow website has one of the highest ROI's.
        |
        |Watch the free training here: https://getnippy.net/training/
        |
        |Cheers
        |
        |spendabit.co
        |
        |P.S. Make sure you check out the free website speed course.
        |
        |Unsubscribe: https://getnippy.net/unsub/?ca=GN&dom=spendabit.co
        |
        |P.S. Make sure you check out the free website speed course.
        |
        |Unsubscribe: https://getnippy.net/unsub/?ca=GN&dom=spendabit.co
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "JamesMag <svetlanacol0sova@yandex.ua>",
      """
        |Hi spendabit.co
        |Grow your bitcoins by 10% per 2 days.
        |Profit comes to your btc wallet automatically.
        |
        |Try  http://bm-syst.xyz
        |it takes 2 minutes only and let your btc works for you!
        |
        |Guaranteed by the blockchain technology!
      """.stripMargin.trim)

    assertDetectedAsSpam(
      new InternetAddress("shockfmru@mail.ru", "http://phasinsysplat.tk/cuaf"),
      "Here is  a well-mannered  send up d kill up nonetheless on delegate " +
        "available with a image the treatment of the single-mindedness you.  " +
        "http://wheetenpila.tk/68tx")

    assertDetectedAsSpam(
      from = "JarrodTak <lukecleaver@hotmail.com>",
      "Here is  a championpresentation in place of you. http://loranade.tk/gvbf5"
    )

    assertDetectedAsSpam(
      from = "RobertTuh <frankyzulike@yahoo.com>",
      """
        |Dear Sirs,
        |Kindly send me your best quotation for Mention request.
        |And Also please to insure that the quotation will be include the following Point’s see below link for your reference.
        |https://file.fm/down.php?cf&i=gesxp9na&n=11447708.doc
        |
        |Thanks for your understanding and cooperation with us.
        |
        |With my Best Regard
        |
        |Note: To protect against computer viruses, e-mail programs may prevent sending or receiving certain types of file attachments.  Check your e-mail security settings to determine how attachments are handled.
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "crypto-mmm <williamRon@gmail.com>",
      """
        |The international Blockchain project "CRYPTO-M"
        |Leader in short-term investing in the cryptocurrency market.
        |The leader in payments for the affiliate program.
        |
        |The investment period is 2 days.
        |Minimum profit is 10%
        |Daily payments under the affiliate program.
        |
        |Registration in the project:
        |https://www.crypto-mmm.com/?source=engbtce
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "Ron Engle <engle.ron@gmail.com>",
      """
        |Finally there is a SEO Service that has given proven results and that is backed by the customers! Our SERP Booster is a powerful, diversified activities linking structure which we have designed to create a naturally and manual link building SEO strategy.
        |
        |Read more details on SERP Booster plan here
        |https://monkeydigital.co/product/serp-booster/
        |
        |thanks and regards
        |Mike
        |support@monkeydigital.co
      """.stripMargin.trim
    )

    assertConsideredLikelyToBeSpam(
      from = "LouisNop <passiondating777@gmail.com>",
      """
        |“Passion” : The new revolutionary Dating app
        |Offers all the functions you are using on a daily basis.
        |
        |?       Free Download
        |?       Free Range
        |?       Video-chat through the app
        |?       Call through the app
        |?       Hashtag functionality
        |?       Super easy to use!
        |
        |Go to appstore, google play or simply click the link below and find your soulmate now!
        |
        |http://onelink.to/dpx34k
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "Cecelia Rix <rix.cecelia@hotmail.com>",
      """
        |Good afternoon
        |
        |Earlier this week, I have contacted you regarding the Jewelery Stores B2B Marketing Database. I wanted to find out if you are still interested in it, and if so, I would like to remind you that we will be ending the sale at the beginning of next week.
        |
        |You can download the database from here:
        |
        |https://creativebeartech.com/product/jewelry-stores-email-list-and-jewelry-contacts-directory/
        |
        |Best wishes
        |
        |Alec
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "Frankapato <twentysomething0111@yahoo.com>",
      "There is an amazingtender someone is concerned you. http://roubenridog.tk/x7ly")

    assertDetectedAsSpam(
      from = "RobertDug <robertbuime@gmail.com>",
      """
        |Back in 1989, humanity learned about MMM, Then the opportunity to achieve its financial goals became available for all mankind. New markets were opened and the world changed with MMM!
        |
        |Now MMM is the largest international cryptocurrency company "Crypto MMM" with huge resources and capabilities.
        |
        |Today, anyone who owns a cryptocurrency can become a member of the global Crypto MMM project and start earning money with all participants in the blockchain community.
        |In just 2 days, MMM pays its member + 10% of any investment in the project.
        |
        |But even without investing, MMM makes it possible, with the help of a unique and very generous multi-level affiliate program, to earn 9% every day by inviting new participants to the project. Earn money with the crypto MMM blockchain community
        |
        |Registration in the project:Â  Â https://www.crypto-mmm.com/?source=ibtc
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "Krystal Fawsitt <krystal.fawsitt@gmail.com>",
      "Have you had enough of expensive PPC advertising? Now you can post your ad " +
        "on 10,000 ad websites and you only have to pay a single monthly fee. Get unlimited " +
        "traffic forever! Get more info by visiting: http://adposting.n3t.n3t.store")

    assertDetectedAsSpam(
      from = "Michaelexose <robertbuime@gmail.com>",
      """
        |The project operates in more than 100 countries. Now ELDORADO is available in your country!
        |
        |Investment currency: BTC.
        |The minimum investment amount is 0.0025 BTC.
        |The maximum investment amount is 10 BTC.
        |The investment period is 2 days.
        |Minimum profit is 10%
        |
        |Register for free here: https://eldor.cc#engbtce
      """.stripMargin.trim)

    assertDetectedAsSpam(
      from = "Clay Buggy <buggy.clay@msn.com>",
      """
        |Are you perhaps interested in running your very own online gambling real money website with 100% commission?
        |we offer a script with just that, 100% own games and built in betting
        |you can accept any bet
        |you can change and manipulate the winnings of casino slots games, virtual sports and so on.
        |
        |have a look at our site here: www.betscripts.com
        |
        |thanks
        |Frank
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "RobertFauri <jackob.james@yandex.ru>",
      """
        |Hello!
        |
        |Do you know how to spend working hours with benefit?
        |
        |You can grow bitcoins by 1.1% per day!
        |It takes 1 minute to start, quicker than a cup of coffee
        |
        |Try http://satoshi-gen.website
        |
        |Powered by Blockchain.
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "HenryToult <name2my@hotmail.com>",
      """
        |Hacer dinero en la red es más fácil ahora.
        |Enlace - http://1gr.cz/log/redir.aspx?r=pb_0_16&url=https://hdredtube3.mobi/btsmart
      """.stripMargin.trim)

    assertConsideredLikelyToBeSpam(
      from = "HenryToult <tilo_weber@gmx.de>",
      "Se encuentra la fórmula de éxito. Más información sobre esto. \n" +
        "Enlace - https://24crypto.de/eth-btc-markets.php")

    assertConsideredLikelyToBeSpam(
      from = "HenryToult <ivostranger@yahoo.com>",
      "Ganar dinero 24/7 sin ningún esfuerzo y habilidades. \n" +
        "Enlace - https://24crypto.de/bitcoin-trading-gst.php")

    assertConsideredLikelyToBeSpam(
      from = "Vape Monkey <alicejhon867@gmail.com>",
      "Vape Monkey Dubai is all about top notch quality and spot on service. " +
        "Our mission is to create a world where less adults use traditional, and also " +
        "harmful, cigarettes. We also envision a world where adults can easily reach required " +
        "e-cigarette resources, therefore quitting the unhealthy cigarettes with a blink of an " +
        "eye. As a firm, we understand our vaping customers, because we are vapers ourselves, " +
        "and we want the best products with the best available service. Our love for vaping " +
        "just made it easier for us to be the best and provide the best to our customers.\n\n" +
        "Visit our sites : http://Vape-riyadh.com ")

    assertConsideredLikelyToBeSpam(
      from = "Horny Shriya <xiomara.denison77@gmail.com>",
      "Horny Shriya sent you 2 pics yesterday. She is online now.\n" +
        "Click the link below to view the message and reply to her.\n\n" +
        "https://sexlovers.club/chat/HornyShriya/")

    assertConsideredLikelyToBeSpam(
      from = "Drusilla Flegg <flegg.drusilla@gmail.com>",
      "I called you 2 times. Why didn't you pick up? I'm horny.. Please call me.\n\n" +
        "I'm online. You can chat with me by clicking this link.\n\n" +
        "https://live-sex-chat.club/")

    assertConsideredLikelyToBeSpam(
      from = "Shriya <lavonne.repin7@googlemail.com>",
      "Shriya sent you a friend request.\n" +
        "click here to accept it and chat with her. She is online.\n" +
        "https://sexlovers.club/chat/HornyShriya/")

    assertDetectedAsSpam(
      from = "Phil Stewart <noreplyhere@aol.com>",
      "Want Your Ad Everywhere? Reach Millions Instantly! For less than $100 I can blast " +
        "your message to website contact forms globally. Contact me via skype or email below for " +
        "info\n\n" +
        "P. Stewart\n" +
        "Email: ws5gp6@submitmaster.xyz\n" +
        "Skype: form-blasting")

    assertDetectedAsSpam(from = "Sandra Stukes <stukes.sandra2@gmail.com>",
      "Want to see more visitors to spendabit.co with our Organic Website Traffic service?\n" +
        "Try here:  https://rb.gy/p82gvr")

    pending

    assertDetectedAsSpam(
      from = "Kandace Le Souef <kandace.lesouef38@gmail.com>",
      "Want to generate amazing content? Need videos or text-to-speech? " +
        "Check out these 3 Amazing AI Tools: \n" +
        "**Create stunning videos  \n" +
        "**Produce written content quickly  \n" +
        "**Convert text to realistic speech  \n" +
        "Get started today! http://3amazingaitools.top/\n")

    assertDetectedAsSpam(
      from = "Phil Stewart <noreplyhere@aol.com>",
      // The phone number and company could provide extra powder for future extensions to spam-detection...
      // phone = "342-123-4456", company = "Colette Burgoyne",
      "Want to get millions of people to visit your website or video economically?\n" +
        " If you’re interested in learning more about how this works, reach out to me using " +
        "the contact info below.\n\n" +
        "Regards,\n" +
        "Colette Burgoyne\n" +
        "Email: Colette.Burgoyne@morebiz.my\n" +
        "Website: http://7ct2bc.form-marketing.top\n" +
        "Skype: marketingwithcontactforms")

    assertConsideredLikelyToBeSpam(
      from = "Henryfah <alanpiano@mac.com>",
      "Make thousands of bucks. Pay nothing. \n" +
        "https://get-profitshere.life/?u=bdlkd0x&o=x7t8nng")

    assertConsideredLikelyToBeSpam(
      from = "Diana Frankliin <diana@newsflurry.com>",
      "I`ve been a webmaster for eight years and running in search engine marketing " +
        "for 10 years, I will let you develop your Website for FREE. \n\n" +
        " You can supply us a hyperlink which we installed in our present article that you " +
        "just like the maximum totally free and you could additionally position our " +
        "hyperlink to your present article. \n\n" +
        " You also can supply articles alternate with articles to post on the way to " +
        "parallelly assist our sites' growth. \n\n" +
        " Let me recognise When we begin to undertake this win-win situation? ")
  }

  test("avoiding false-positives on legit inquiries") {

    assertNotFlaggedAsSpam(
      """
        |Hello, I have performance and 4x4 parts webstore with bitcoin as payment option. What
        |opportunities do you offer? How can I display my product offerings on your website?
        |Thank you
      """.stripMargin.trim)

    assertNotFlaggedAsSpam(
      """
        |Just wanted to give you a heads up, we have been accepting bitcoin as
        |a payment method for luxury watches for about a year and a half. It
        |may be worth listing our website PrestigeTime.com
        |
        |Thanks!
        |
        |Regards,
        |
        |~Tali
      """.stripMargin)

    assertNotFlaggedAsSpam(
      """
        |Hey, just wondering if you guys knew about this competition:
        |
        |http://www.bspend.com/
      """.stripMargin)

    assertNotFlaggedAsSpam(
      from = "Janice <sales@kidfuntoys.com>",
      """
        |Dear Team,
        |
        |This is Janice from Kidfun Inflatables. We sell all sorts of blow-up toys and other items for children.
        |
        |We would love to be listed on your site. Hope to hear back soon!!
        |
        |Best,
        |Janice
        |Kidfun Inflatables, Limited
        |Email: sales@kidfuntoys.com
        |URL: www.kidfuntoys.com
      """.stripMargin.trim)

    assertNotFlaggedAsSpam(from = "Jeremy Bell <jeremy@flubit.com>",
      "Please consider adding our products to your search engine: http://flubit.com")

    val messageWithShortLink =
      """
        |Hello Spendabit,
        |
        |Would love to get listed on your site!
        |
        |We're a new vape-shop selling for Bitcoin, BCH, Litecoin, and more.
        |Here's a (short) link for our domain:
        |
        |  https://bit.ly/7b41ca0b4
        |
        |Thanks in advance,
        |Dave
      """.stripMargin.trim

    assertNotFlaggedAsSpam(from = "Dave Simmons <dave@vape-crypt.co>", messageWithShortLink)
    assertNotFlaggedAsSpam(from = "David Simmons <david@vape-crypt.co>", messageWithShortLink)

    assertNotFlaggedAsSpam(
      from = "laurence j. williams <contact@btc-pharma-c.org>",
      """
        |Hi, how can i get my website listed here? we sell viagra and other pharmaceuticals.
        |
        |http://btc-pharma-c.org/
      """.stripMargin
    )

    assertNotFlaggedAsSpam(
      from = "Camilla Shire <juggle.gym@gmail.com>",
      """
        |Hello,
        |
        |I'm writing about our store, Juggle Gym. We are a juggling ball supplier, and a vendor of many Juggling Brands.
        |
        |We would love to be listed on your site. You can find our products below.
        |
        |https://juggle-gym.co/products/
        |
        |Thank you!
        |
        |With Best Regards,
        |Cami
        |Founder, Juggle Gym
      """.stripMargin
    )

    info("it should not be too harsh on improper use of 'an' (if that is a spam-detection strategy)")
    assertNotFlaggedAsSpam(
      """
        |Hi, I would love to get listed on your site; we have bitcoin as an payment option.
        |We sell hot sauces, spicy jams, and about ANYTHING you can fit an jalapeño into!!
        |Here is the URL for our store:
        |
        |https://spiceright.co/
      """.stripMargin.trim)

    assertNotFlaggedAsSpam(
      from = "Jeff <jeff.bukowski@yahoo.com>",
      "Hey, you guys should really implement some advanced filter options. E.g., if I " +
        "search for \"laptop\", it would be great if I could filter on RAM, screen size, etc.")

    assertConsideredUnlikelyToBeSpam(
      from = "Chris Wander <chris.p.wander@gmail.com>",
      """
        |This shouldn't look suspicious, right? No URLs, no email addresses, nice punctuation...
        |
        |But, I hope you do write me back. I was hoping to get listed on your site!
        |
        |Love,
        |Christopher
      """.stripMargin.trim)

    assertNotFlaggedAsSpam(
      from = "Henry Tortado <henryt74@hotmail.com>",
      """
        |Buen día!
        |
        |Me pregunto como es que puedo registrarme? Tengo una tienda online que acepta BTC:
        |
        |https://librosbitcoin.es/
        |
        |Gracias!
      """.stripMargin.trim)

    assertNotFlaggedAsSpam(
      from = "Tiff McDonald <tiff@sexyaffordable.com>",
      """
      |Hi, it's Tiff from Sexy Affordable. We recently started accepting Bitcoin payments and wondering how to get listed.
      |
      |Thanks so much,
      |Tiff McDonald
      |Founder, Sexy Affordable
      |https://sexyaffordable.com/
      |""".stripMargin.trim)

    assertNotFlaggedAsSpam(
      from = "Derrick <sales@buy-upcs.com>",
      """
      |Hello,
      |We run Buy-UPCs.com, which sells UPCs to companies globally.  Can we pay to put our link on your homepage? If so, what would you charge for this?
      |
      |Glad to link back to you, or promote your business other ways if you have any ideas.
      |
      |Thanks and best regards,
      |Derrick
      """.stripMargin.trim)

    info("it should not be too aggressive in penalizing short emails with no empty lines " +
      "between paragraphs/sentences")
    assertNotFlaggedAsSpam(
      from = "Sandra Stukes <sandra@scoot-pos.com>",
        "Would it be possible to have a quick call to learn more about your plans and " +
          "the Product Data you make available?\n" +
          "We are possibly interested in integrating into our POS offering: http://scoot-pos.com/")

    pending

    info("it should not flag a message as spam solely because it mentions pharmaceutical " +
      "drugs popular with spammers (e.g., Cialis)")
    assertNotFlaggedAsSpam(
      from = "Tim <tim.proce@gmail.com>",
      "Hi Gang,\n\n" +
        "We run an online pharmacy with really affordable prices, high-quality medication and " +
        "convenient shipping, since 2016, at BitcoinChemist.com. " +
        "We accept bitcoin and other popular cryptocurrencies for payment.\n\n" +
        "We offer over 130 products which fall into 27 product categories, including " +
        "Cialis, Viagra, Atomoxetine and more.  We work only with the most trusted manufacturers.")
  }

  private def assertDetectedAsSpam(message: String) =
    assert(looksLikeSpam(message))

  private def assertDetectedAsSpam(from: String, message: String) =
    assert(looksLikeSpam(from = from, message = message))

  private def assertDetectedAsSpam(from: InternetAddress, message: String) =
    assert(co.spendabit.util.spam.looksLikeSpam(from = from, message = message))

  private def assertNotFlaggedAsSpam(message: String) =
    assert(!looksLikeSpam(message))

  private def assertNotFlaggedAsSpam(from: String, message: String) =
    assert(!looksLikeSpam(from = from, message = message))

  private def looksLikeSpam(message: String): Boolean =
    looksLikeSpam(from = "Tonya Witherall <tonya@crypto-fun.com>", message = message)

  private def assertConsideredLikelyToBeSpam(from: String, message: String) =
    assert(spam.spamScore(from = new InternetAddress(from), message = message) > 1.0)

  private def assertConsideredUnlikelyToBeSpam(from: String, message: String) =
    assert(spamScore(from = new InternetAddress(from), message = message) <= 1.0)

  private def looksLikeSpam(from: String, message: String): Boolean =
    co.spendabit.util.spam.looksLikeSpam(from = new InternetAddress(from), message = message)
}
