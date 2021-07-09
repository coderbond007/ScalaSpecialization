package quickcheck

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck._

import scala.annotation.tailrec

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = for {
    a <- arbitrary[Int]
    h <- oneOf(Gen.const(empty), genHeap)
  } yield insert(a, h)
  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("min1") = forAll { a: A =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  property("cond1") = forAll { (a: A, b: A) =>
    val h = insert(b, insert(a, empty))
    val min = math.min(a, b)
    findMin(h) == min
  }

  property("cond2") = forAll { (a: A) =>
    val h = insert(a, empty)
    isEmpty(deleteMin(h))
  }

  property("cond3") = forAll { (h: H) =>
    @tailrec
    def isSorted(h: H): Boolean =
      if (isEmpty(h)) true
      else {
        val m = findMin(h)
        val h2 = deleteMin(h)
        isEmpty(h2) || (m <= findMin(h2) && isSorted(h2))
      }

    isSorted(h)
  }

  property("cond4") = forAll { (h1: H, h2: H) =>
    findMin(meld(h1, h2)) == math.min(findMin(h1), findMin(h2))
  }

  property("cond5") = forAll { (h1: H, h2: H) =>
    @tailrec
    def heapEqual(h1: H, h2: H): Boolean =
      if (isEmpty(h1) && isEmpty(h2)) true
      else {
        val m1 = findMin(h1)
        val m2 = findMin(h2)
        m1 == m2 && heapEqual(deleteMin(h1), deleteMin(h2))
      }

    heapEqual(meld(h1, h2), meld(deleteMin(h1), insert(findMin(h1), h2)))
  }
}
