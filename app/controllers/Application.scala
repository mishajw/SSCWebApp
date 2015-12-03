package controllers

import java.util.UUID
import javax.mail.AuthenticationFailedException

import backend.MailSender
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.collection.mutable

object Application extends Controller {
  val loginStorage: mutable.Map[String, MailSender] = mutable.Map()

  /**
    * Show the login page
    */
  def loginPage = Action {
    Ok(views.html.loginPage())
  }

  /**
    * Log in with email details
    */
  def login = Action { implicit request => try {
      val (email, pass, server) = loginForm.bindFromRequest.get
      val mailSender = new MailSender(email, pass, server)
      val uuid = UUID.randomUUID().toString

      loginStorage(uuid) = mailSender

      Redirect(routes.Application.mail()).withSession("sid" -> uuid)
    } catch {
      case e: AuthenticationFailedException =>
        Redirect(routes.Application.loginFailed)
      case e: Throwable =>
        Redirect(routes.Application.loginFailed)
    }
  }

  /**
    * Allow user to send mail
    */
  def mail = Action (implicit request => sid match {
    case Some(sid) => Ok(views.html.sendMail())
    case None => Redirect(routes.Application.loginPage())
  })

  /**
    * Send mail
    */
  def send = Action { implicit request =>
    sid match {
      case Some(sid) =>
        val (to, subject, body) = sendForm.bindFromRequest.get
        val mailSender = loginStorage(sid)

        mailSender.send(to, subject, body)

        Redirect(routes.Application.sent())
      case None =>
        Redirect(routes.Application.loginPage())
    }
  }

  /**
    * Page showing successful send
    */
  def sent = Action {
    Ok(views.html.sent())
  }

  /**
    * Log out the user
    * @return
    */
  def logoutPage() = Action { implicit request =>
    sid match {
      case Some(sid) =>
        loginStorage(sid).close()
        loginStorage remove sid
      case None =>
    }

    Redirect(routes.Application.loginPage())
  }

  /**
    * Page showing unsuccessful login
    */
  def loginFailed = Action {
    Ok(views.html.loginFailed())
  }

  /**
    * Get the Session ID from the request
    */
  def sid(implicit request: Request[AnyContent]) = {
    request.session.get("sid")
  }

  /**
    * Form for logging in
    */
  def loginForm = Form(tuple(
      "email" -> text,
      "pass" -> text,
      "server" -> text
    )
  )

  /**
    * Form for sending emails
    */
  def sendForm = Form(tuple(
      "to" -> text,
      "subject" -> text,
      "body" -> text
    )
  )
}