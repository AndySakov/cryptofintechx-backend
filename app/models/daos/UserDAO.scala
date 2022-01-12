package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future
import models.UserProfile
import java.util.UUID

/** Gives access to the user storage.
  */
trait UserDAO {

  /** Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    * @return The found user or None if no user for the given login info could be found.
    */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /** Retrieves a user by their userID.
    *
    * @param userID The user id to check for.
    * @return an option of user.
    */
  def findById(userID: String): Future[Option[User]]

  /** Saves a user.
    *
    * @param user The user to save.
    * @return The saved user.
    */
  def save(user: User): Future[User]

  /** Updates a users profile.
    *
    * @param userID The id of the user to update.
    * @param newProfile The new profile to store
    * @return The updated user.
    */
  def updateProfile(userID: UUID, newProfile: UserProfile): Future[User]

  /** Updates a users password.
    *
    * @param userID The id of the user to update.
    * @param newPassword The new password to store
    * @return The updated user.
    */
  def updatePassword(userID: UUID, newPassword: String): Future[User]

  /** Deletes a user.
    *
    * @param user The user to delete.
    * @return The deleted user.
    */
  def delete(user: User): Future[User]
}
