package backend

import java.util.Properties
import javax.mail._
import javax.mail.internet.{MimeBodyPart, MimeMultipart, InternetAddress, MimeMessage}

class MailSender(email: String, pass: String, server: String) {

  /**
    * Details on the connection
    */
  private val PORT = 587
  private val session = createSession
  private val transport = createTransport

  /**
    * Send a message
    * @param to email address to
    * @param subject subject of message
    * @param body body of message
    */
  def send(to: String, subject: String, body: String): Unit = {
    val message = new MimeMessage(session)
    message.setFrom(new InternetAddress(email))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    message.setSubject(subject)

    val messageBody = new MimeMultipart()
    val messageBodyText = new MimeBodyPart()
    messageBodyText.setText(body)
    messageBody.addBodyPart(messageBodyText)
    message.setContent(messageBody)

    transport.sendMessage(message, Array(new InternetAddress(to)))
  }

  /**
    * Close the connection
    */
  def close(): Unit = {
    transport.close()
  }

  /**
    * Create a session for sending
    * @return
    */
  private def createSession = {
    val props = new Properties

    // SMTP
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", server)
    props.put("mail.smtp.port", PORT.toString)

    //IMAP
    props.put("mail.store.protocol", "imaps")

    // Credentials
    props.put("mail.user", email)
    props.put("mail.password", pass)

    Session.getInstance(props, new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(email, pass)
    })
  }

  /**
    * Create a method of transport
    * Throws error if bad credentials
    * @return
    */
  private def createTransport = {
    val transport = session.getTransport("smtp")
    transport.connect(server, PORT, email, pass)
    transport
  }
}
