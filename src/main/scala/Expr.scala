import scala.collection.mutable.{Map => myMutableMap}

trait Expr {
  def eval(ma: myMutableMap[String, myMutableMap[String, Int]]): Set[String] = this match {
    case Token(xs) =>  ma.get(xs).fold(Set.empty[String])(x => x.keySet.toSet)
    case Conjunction(first, second) =>  first.eval(ma) & second.eval(ma)
    case DisJunction(first, second) => first.eval(ma) union second.eval(ma)
  }
}
case class Token(token: String) extends Expr
case class Conjunction(token1: Expr, token2: Expr) extends Expr
case class DisJunction(token1: Expr, token2: Expr) extends Expr
