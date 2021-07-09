package funsets.v2

trait FunSetsInterface {
    type FunSets = Int => Boolean

    def contains(s: FunSets, elem: Int): Boolean

    def singletonSet(elem: Int): FunSets

    def union(s: FunSets, t: FunSets): FunSets

    def intersect(s: FunSets, t: Int => Boolean): FunSets

    def diff(s: FunSets, t: FunSets): FunSets

    def filter(s: FunSets, p: Int => Boolean): FunSets

    def forall(s: FunSets, p: Int => Boolean): Boolean

    def exists(s: FunSets, p: Int => Boolean): Boolean

    def map(s: FunSets, f: Int => Int): FunSets

    def toString(s: FunSets): String
}
