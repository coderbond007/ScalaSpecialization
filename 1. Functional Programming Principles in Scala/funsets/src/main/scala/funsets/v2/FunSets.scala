package funsets.v2

trait FunSets extends FunSetsInterface {
    /**
     * We represent a set by its characteristic function, i.e.
     * its `contains` predicate.
     */
    override type FunSets = Int => Boolean

    override def contains(s: FunSets, elem: Int): Boolean = s(elem)

    override def singletonSet(elem: Int): FunSets = (x: Int) => elem == x

    override def union(s: FunSets, t: FunSets): FunSets = (x: Int) => contains(s, x) || contains(t, x)

    override def intersect(s: FunSets, t: Int => Boolean): FunSets = ???

    override def diff(s: FunSets, t: FunSets): FunSets = ???

    override def filter(s: FunSets, p: Int => Boolean): FunSets = ???

    override def forall(s: FunSets, p: Int => Boolean): Boolean = ???

    override def exists(s: FunSets, p: Int => Boolean): Boolean = ???

    override def map(s: FunSets, f: Int => Int): FunSets = ???

    override def toString(s: FunSets): String = ???
}
object FunSets extends FunSets
