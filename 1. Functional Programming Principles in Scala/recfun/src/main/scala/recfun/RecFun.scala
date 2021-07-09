package recfun

import scala.annotation.tailrec

object RecFun extends RecFunInterface {

  def main(args: Array[String]): Unit = {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(s"${pascal(col, row)} ")
      println()
    }
  }

  /**
   * Exercise 1
   */
  def pascal(c: Int, r: Int): Int = {
    if (c == 0 || r == c) 1
    else pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
   * Exercise 2
   */
  def balance(chars: List[Char]): Boolean = {
    @tailrec
    def balanceBrackets(arr: List[Char], carry: Int, index: Int): Int = {
      if (carry < 0) -1;
      else {
        if (index == arr.length) {
          if (carry == 0) {
            Int.MaxValue
          } else {
            -1
          }
        } else {
          if (arr(index) == '(') {
            balanceBrackets(arr, carry + 1, index + 1)
          } else if (arr(index) == ')') {
            balanceBrackets(arr, carry - 1, index + 1)
          } else {
            balanceBrackets(arr, carry, index + 1)
          }
        }
      }
    }

    balanceBrackets(chars, 0, 0) == Int.MaxValue
  }

  /**
   * Exercise 3
   */
  def countChange(money: Int, coins: List[Int]): Int = {
    if (money > 0 && coins.nonEmpty)
      countChange(money, coins.tail) + countChange(money - coins.head, coins)
    else if (money == 0) 1 else 0
  }
}
