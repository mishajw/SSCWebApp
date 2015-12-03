package backend

import java.util.Properties
import javax.mail._
import javax.mail.internet.{MimeBodyPart, MimeMultipart, InternetAddress, MimeMessage}

object MailSender {
  def send(email: String, pass: String, server: String,
           to: String, subject: String, body: String): Unit = {

    println(email)
    println(pass)
    println(server)
    println(to)
    println(subject)
    println(body)

    val message = createPlainMessage(email, pass, server)
    message.setFrom(new InternetAddress(email))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    message.setSubject(subject)

    val messageBody = new MimeMultipart()
    val messageBodyText = new MimeBodyPart()
    messageBodyText.setText(body)
    messageBody.addBodyPart(messageBodyText)
    message.setContent(messageBody)

    Transport.send(message)
  }

  private def createPlainMessage(email: String, pass: String, host: String): Message = {
    val props = new Properties

    // SMTP
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", "587")

    //IMAP
    props.put("mail.store.protocol", "imaps")

    // Credentials
    props.put("mail.user", email)
    props.put("mail.password", pass)

    val session = Session.getInstance(props, new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(email, pass)
    })

    new MimeMessage(session)
  }
}
