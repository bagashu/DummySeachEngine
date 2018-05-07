/**
  * Created by abagla on 5/6/18.
  */

case class ParseQuery(xs: List[String]) {

  // Query Validation checks...
  def isValid(xs: String): Boolean = true

  // Convert the expression in to POSTFIX and evaluate
  def parse: Expr = {

    // POSTFIX representation
    // (a&b)|c = ab&c|
    val (stack, output) = xs.foldLeft((List.empty[String], List.empty[String])){
      case ((st, out), element) => element match {
        case "(" | "&" | "|"=>
          (element +: st , out)
        case ")" => (st.dropWhile{_ != "("}.drop(1), out ::: st.takeWhile{_ != "("} )
        case token => (st, out :+ token)
      }
    }
    // Push everything from the stack to output
    val ans = output ::: stack

    // Convert POSTFIX expression into Expr class
    // ab&c| = DisJunction(Conjunction(token(a), token(b)), token(c))
    ans.foldLeft(List.empty[Expr]){
      case (acc, element) => element match {
        case "&" =>
          // acc should have atleast 2 elements to process disjunction or conjunction
          if (acc.length < 2) throw new IllegalArgumentException(s"Invalid Query $xs")
          else Conjunction(acc.head, acc.tail.head) +: acc.drop(2)

        case "|" =>
          // acc should have atleast 2 elements to process disjunction or conjunction
          if (acc.length < 2) throw new IllegalArgumentException(s"Invalid Query $xs")
          else DisJunction(acc.head, acc.tail.head) +: acc.drop(2)

        case tok => Token(tok) +: acc
      }
    }.head
  }
}
