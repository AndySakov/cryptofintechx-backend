package dao

import api.misc.SessionTypeImpl
import api.misc.SessionTypeImpl._
import api.utils.Generator._
import api.utils.Utils.exec
import models.{Session, Token}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
class SessionDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider, users: UserDAO)(implicit executionContext: ExecutionContext)  {
	
	val dbConfig = dbConfigProvider.get[JdbcProfile]
	val db = dbConfig.db
	import dbConfig.profile.api._
	
	private val Sessions = TableQuery[SessionsTable]
	
	def newSession(user_id: String): Token = {
		val newToken = Token(random(32))
		val current = Sessions.filter(session => session.user_id === user_id)
		val now = Instant.now()
		val timestampNow = Timestamp.from(now)
		val anHourFromNow = Timestamp.from(now.plus(1, ChronoUnit.HOURS))
		if (exec(db.run(current.exists.result))) {
			exec(db.run(current.map(token => (token.token, token.createdAt, token.expiresAt)).update((newToken, timestampNow, anHourFromNow))))
			newToken
		} else {
			exec(db.run(Sessions += Session(0L, randomID, user_id, newToken, timestampNow, anHourFromNow)).map(_ => newToken))
		}
	}
	
	def validateSession(token: Token): SessionType = {
		val sessionList = exec(db.run(Sessions.filter(session => session.token === token).result).map(res => res))
		token match {
			case Token(x) if x equalsIgnoreCase "null" => SessionTypeImpl.NOT_FOUND
			case _ =>
				if(sessionList.nonEmpty) {
					val current = sessionList.head
					if(Timestamp.from(Instant.now()).before(current.expiresAt)) {
						VALID
					} else {
						EXPIRED
					}
				} else {
					SessionTypeImpl.NOT_FOUND
				}
		}
	}

	def deleteSession(token: Token): Future[Unit] = {
		db.run(Sessions.filter(session => session.token === token).delete).map(_ => ())
	}
	
	private class SessionsTable(tag: Tag) extends Table[models.Session](tag, "sessions") {
		def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
		def token_id: Rep[String] = column[String]("token_id", O.Unique)
		def user_id: Rep[String] = column[String]("user_id", O.Unique)
		def token: Rep[Token] = column[Token]("token", O.Unique)
		def createdAt: Rep[Timestamp] = column[Timestamp]("createdAt")
		def expiresAt: Rep[Timestamp] = column[Timestamp]("expiresAt")
		
		def user = foreignKey("users_fk", user_id, users.Users)(_.user_id, onDelete = ForeignKeyAction.Restrict)
		
		def * = (id, token_id, user_id, token, createdAt, expiresAt).mapTo[models.Session]
	}
}
