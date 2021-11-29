package dao

import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import models.Session
import models.Token
import java.time.Instant
import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}
import java.time.temporal.ChronoUnit;
import api.utils.Generator._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
class SessionDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider, users: UserDAO)(implicit executionContext: ExecutionContext)  {
	
	val dbConfig = dbConfigProvider.get[JdbcProfile]
	val db = dbConfig.db
	import dbConfig.profile.api._
	
	private val Sessions = TableQuery[SessionsTable]
	
	def newSession(user_id: String): Token = {
		val token = Token(random(32))
		Await.result(db.run(Sessions += Session(0L, randomID, user_id, token, Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)))).map(_ => token), 5 seconds)
	}
	
	def validateSession(user_id: String, token: Token): Boolean = {
		user_id match { 
			case x if x equalsIgnoreCase "null" => false
			case _ => Await.result(db.run(Sessions.filter(session => session.user_id === user_id && session.token === token && session.expiresAt > Timestamp.from(Instant.now())).result).map(res => res.size == 1), 5 seconds)
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
