package models.services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.{ User, UserProfile }

import scala.concurrent.Future
import java.util.UUID

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
     * Retrieves a user by their userID.
     *
     * @param userID The user id to check for.
     * @return an option of user.
     */
  def getUserById(userID: String): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
     * Updates a users profile.
     *
     * @param userID The id of the user to update.
     * @param newProfile The new profile to store
     * @return The updated user.
     */
  def updateProfile(userID: UUID, newProfile: UserProfile): Future[User]

  /**
    * Deletes a user.
    *
    * @param user The user to delete.
    * @return The deleted user.
    */
  def delete(user: User): Future[User]
}
