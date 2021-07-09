package reductions

import org.scalameter._

object ParallelParenthesesBalancingRunner {

    @volatile var seqResult = false

    @volatile var parResult = false

    val standardConfig = config(
        Key.exec.minWarmupRuns -> 40,
        Key.exec.maxWarmupRuns -> 80,
        Key.exec.benchRuns -> 120,
        Key.verbose -> true
    ) withWarmer (new Warmer.Default)

    def main(args: Array[String]): Unit = {
        val length = 100000000
        val chars = new Array[Char](length)
        val threshold = 10000
        val seqtime = standardConfig measure {
            seqResult = ParallelParenthesesBalancing.balance(chars)
        }
        println(s"sequential result = $seqResult")
        println(s"sequential balancing time: $seqtime")

        val fjtime = standardConfig measure {
            parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
        }
        println(s"parallel result = $parResult")
        println(s"parallel balancing time: $fjtime")
        println(s"speedup: ${seqtime.value / fjtime.value}")
    }
}

object ParallelParenthesesBalancing extends ParallelParenthesesBalancingInterface {

    /** Returns `true` iff the parentheses in the input `chars` are balanced.
     */
    def balance(chars: Array[Char]): Boolean = {
        val result = chars.foldLeft(0)((n, c) => c match {
            case '(' => if (n < 0) -1 else n + 1
            case ')' => if (n <= 0) -1 else n - 1
            case _ => n
        })
        result == 0
    }

    /** Returns `true` iff the parentheses in the input `chars` are balanced.
     */
      def parBalance(chars: Array[Char], threshold: Int): Boolean = {

        def traverse(idx: Int, until: Int, arg1: Int, arg2: Int) : Option[Int] = {
          if (until - idx <= threshold) {
            chars.slice(idx, until).foldLeft(Option(0))((n, c) => c match {
              case '(' => n.map(_ + 1)
              case ')' => n.map(_ - 1) match {
                case None => None
                case Some(y) if y < 0 && idx == 0 => None
                case x => x
              }
              case _ => n
            })
          } else {
            val mid = idx + (until - idx) / 2
            val (a1, a2) = parallel(traverse(idx, mid, arg1, arg2), traverse(mid, until, arg1, arg2))
            val vValue = for {
              x <- a1
              y <- a2
            } yield x + y
            vValue match {
              case Some(z) if z < 0 && idx == 0 => None
              case other => other
            }
          }
        }

        def reduce(from: Int, until: Int) :Option[Int] = {
          traverse(0, until, 0, until)
        }

        reduce(0, chars.length).getOrElse(-1) == 0
      }
//    def parBalance(chars: Array[Char], threshold: Int): Boolean = {
//        // arg1 == ( and arg2 == )
//        @tailrec
//        def traverse(idx: Int, until: Int, arg1: Int, arg2: Int): Int = {
//            if (idx == until) arg1 - arg2
//            else if (chars(idx) == '(') traverse(idx + 1, until, arg1 + 1, arg2)
//            else if (chars(idx) == ')') traverse(idx + 1, until, arg1, arg2 + 1)
//            else traverse(idx + 1, until, arg1, arg2)
//        }
//
//        def reduce(from: Int, until: Int): Int = {
//            val length = until - from
//            if (length / 4 >= threshold) {
//                val par = parallel(
//                    traverse(from, threshold, 0, 0),
//                    traverse(from + threshold, threshold * 2, 0, 0),
//                    traverse(from + threshold * 2, threshold * 3, 0, 0),
//                    traverse(from * threshold * 3, until, 0, 0)
//                )
//                par._1 + par._2 + par._3 + par._4
//            } else if ((until - from) / 2 >= threshold) {
//                val par = parallel(
//                    traverse(from, threshold, 0, 0),
//                    traverse(from + threshold, until, 0, 0)
//                )
//                par._1 + par._2
//            } else {
//                traverse(from, until, 0, 0)
//            }
//        }
//
//        if (chars.isEmpty) true
//        else reduce(0, chars.length) == 0
//    }

    // For those who want more:
    // Prove that your reduction operator is associative!

}
