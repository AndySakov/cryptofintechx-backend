package api.misc

case class Result[A](message: Message, data: Option[A])