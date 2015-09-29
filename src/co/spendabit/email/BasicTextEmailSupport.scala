package co.spendabit.email

import javax.mail
import javax.mail.internet.{MimeMessage, InternetAddress}
import scala.collection.convert.WrapAsJava

import org.slf4j.LoggerFactory

trait BasicTextEmailSupport extends WrapAsJava {

  sealed trait Protocol { def code: String }
  case object SMTP  extends Protocol { val code = "smtp" }
  case object SMTPS extends Protocol { val code = "smtps" }

  protected def protocol: Protocol = SMTPS

  protected def smtpHost: String
  protected def smtpPort: Int
  protected def smtpUsername: String
  protected def smtpPassword: String

  private lazy val log = LoggerFactory.getLogger("co.spendabit.email.BasicTextEmailSupport")

  protected def sendTextEmail(from: InternetAddress, to: Seq[InternetAddress],
                              subject: String, textBody: String) {
    val toLine = to.map(_.getAddress).mkString(", ")
    val session = smtpSession(from)
    val message = new MimeMessage(session)
    message.setSentDate(new java.util.Date)
    message.setFrom(from)
    message.setRecipients(mail.Message.RecipientType.TO, toLine)
    message.setSubject(subject)
    message.setText(textBody, "utf-8")
    log.info(s"Pushing out email with subject '${message.getSubject}', from-address " +
      s"${message.getFrom.mkString(" and ")}, and recipients " +
      s"'${message.getAllRecipients.mkString(" and ")}'")
    sendThenClose(from, message, session)
  }

  private def sendThenClose(from: InternetAddress, message: MimeMessage, session: mail.Session) {
    val t = session.getTransport(protocol.code.toLowerCase)
    try {
      t.connect(smtpHost, smtpUsername, smtpPassword)
      t.sendMessage(message, message.getAllRecipients);
    } finally {
      t.close()
    }
  }

  private def smtpSession(fromAddr: InternetAddress): mail.Session = {
    val properties = new java.util.Properties
    val proto = protocol.code.toLowerCase
    properties.put(s"mail.$proto.host", smtpHost)
    properties.put(s"mail.$proto.port", smtpPort.toString)
    properties.put(s"mail.$proto.from", fromAddr.getAddress)
    properties.put(s"mail.$proto.auth", "true")
    if (protocol == SMTPS)
      properties.put(s"mail.$proto.ssl.enable", "true")
    val s = javax.mail.Session.getInstance(properties)
    log.info("Got email session with SMTP host " + s.getProperty(s"mail.$proto.host") +
      " and from-address " + s.getProperty(s"mail.$proto.from"))
    s
  }
}
