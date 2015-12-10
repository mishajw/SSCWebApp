package controllers

import java.util.UUID

import backend.MailSender
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.collection.mutable
import scala.util.Try

object Application extends Controller {

	case class LoginDetail(email: String, pass: String, server: String, mailSender: Option[MailSender])

	val loginStorage: mutable.Map[String, LoginDetail] = mutable.Map()

	/**
		* Show the login page
		*/
	def loginPage = Action {
		Ok(views.html.loginPage())
	}

	/**
		* Log in with email details
		*/
	def login = Action { implicit request =>
		val (email, pass, server) = loginForm.bindFromRequest.get
		val uuid = UUID.randomUUID().toString

		try {
			val mailSender = new MailSender(email, pass, server)

			loginStorage(uuid) = LoginDetail(email, pass, server, Some(mailSender))

			Redirect(routes.Application.mail()).withSession("sid" -> uuid)
		} catch {
			case e: Throwable =>
				loginStorage(uuid) = LoginDetail(email, pass, server, None)
				Redirect(routes.Application.loginFailed()).withSession("sid" -> uuid)
		}
	}

	def retryLogin = Action { implicit request =>
		sid match {
			case Some(sid) =>
				val loginDetail = loginStorage(sid)
				Ok(views.html.loginPage(loginDetail.email, loginDetail.pass, loginDetail.server))
			case None =>
				Ok(views.html.loginPage())
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

				loginStorage(sid).mailSender match {
					case Some(mailSender) =>
						mailSender.send(to, subject, body)
						Redirect(routes.Application.sent())
					case None =>
						Redirect(routes.Application.loginPage())
				}
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
				Try(loginStorage(sid).mailSender.get.close())
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