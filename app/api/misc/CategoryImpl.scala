package api.misc

object CategoryImpl extends Enumeration {
  type Category = Value
  val NORMAL = Value("Normal")
  val SILVER = Value("Silver")
  val GOLD = Value("Gold")
  val DIAMOND = Value("Diamond")
}
