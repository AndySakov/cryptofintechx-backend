package api.utils

object BCrypt {
  def encrypt(plaintext: String): String =
    org.mindrot.jbcrypt.BCrypt.hashpw(plaintext, org.mindrot.jbcrypt.BCrypt.gensalt)

  def validate(plaintext: String, hashed: String): Boolean =
    org.mindrot.jbcrypt.BCrypt.checkpw(plaintext, hashed)
}

