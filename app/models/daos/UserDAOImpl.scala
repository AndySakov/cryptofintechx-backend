package models.daos

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import models.tables.UserTable
import models.{ User, UserProfile }
import play.api.db.slick.DatabaseConfigProvider
// import slick.jdbc.JdbcProfile
import utils.pgsql.CustomPostgresProfile.api._

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }
import utils.pgsql.CustomPostgresProfile

/** Gives access to the user storage.
  */
class UserDAOImpl @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
  )(implicit
    ec: ExecutionContext
  ) extends UserDAO {
  private val users = TableQuery[UserTable]

  private val db = dbConfigProvider.get[CustomPostgresProfile].db

  /** Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  override def find(loginInfo: LoginInfo): Future[Option[User]] = db.run {
    users.filter(_.email === loginInfo.providerKey).result.headOption
  }

  /** Retrieves a user by their userID.
    *
    * @param userID The user id to check for.
    * @return an option of user.
    */
  def findById(userID: String): Future[Option[User]] = db.run {
    users.filter(_.userID === UUID.fromString(userID)).result.headOption
  }

  /** Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  override def save(user: User): Future[User] = db.run {
    users returning users += user
  }

  /** Updates a users profile.
    *
    * @param userID The id of the user to update.
    * @param newProfile The new profile to store
    * @return The updated user.
    */
  def updateProfile(userID: UUID, newProfile: UserProfile): Future[User] = db.run {
    users
      .filter(_.userID === userID)
      .map(user => (user.phoneNumber, user.avatarURL).mapTo[UserProfile])
      .update(newProfile) andThen {
      users.filter(_.userID === userID).result.map(_.head)
    }
  }

  /** Updates a users password.
    *
    * @param userID The id of the user to update.
    * @param newPassword The new password to store
    * @return The updated user.
    */
  def updatePassword(userID: UUID, newPassword: String): Future[User] = db.run {
    users.filter(_.userID === userID).map(_.password).update(newPassword) andThen {
      users.filter(_.userID === userID).result.map(_.head)
    }
  }

  /** Deletes a user.
    *
    * @param user The user to delete.
    * @return The deleted user.
    */
  override def delete(user: User): Future[User] = db.run {
    users.filter(_.email === user.email).delete.map(_ => user)
  }
}
