package models.services

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.{ User, UserProfile }
import models.daos.UserDAO

import scala.concurrent.{ ExecutionContext, Future }
import java.util.UUID

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
      * Retrieves a user by their userID.
      *
      * @param userID The user id to check for.
      * @return an option of user.
      */
   def getUserById(userID: String): Future[Option[User]] = userDAO.findById(userID)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User] = userDAO.save(user)

  /**
     * Updates a users profile.
     *
     * @param userID The id of the user to update.
     * @param newProfile The new profile to store
     * @return The updated user.
     */
  def updateProfile(userID: UUID, newProfile: UserProfile): Future[User] = userDAO.updateProfile(userID, newProfile)

  /**
      * Deletes a user.
      *
      * @param user The user to delete.
      * @return The deleted user.
      */
    def delete(user: User): Future[User] = userDAO.delete(user)
}
