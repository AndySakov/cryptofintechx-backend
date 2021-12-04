package api.misc

import api.misc.ResultTypeImpl.ResultType

case class ResultSet[A](resultType: ResultType, result: Result[A])