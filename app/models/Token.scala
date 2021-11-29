package models
import slick.lifted.MappedTo

final case class Token(token: String) extends AnyVal with MappedTo[String] {
    override def value: String = token
}