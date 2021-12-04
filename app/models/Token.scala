package models
import slick.lifted.MappedTo

final case class Token(token: String) extends AnyVal with MappedTo[String] with Serializable {
    override def value: String = token
}