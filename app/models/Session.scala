package models

import java.sql.Timestamp

final case class Session(
  id: Long = 0L,
  token_id: String,
  user_id: String,
  token: Token,
  createdAt: Timestamp,
  expiresAt: Timestamp
) extends Serializable
