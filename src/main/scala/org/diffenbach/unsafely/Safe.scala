package org.diffenbach.unsafely


/***
 * Separate an interface into a "safe" subset and an "unsafe" superset, allowing a client programmer
 * to opt to use the "unsafe" interface only by explicit request, by using a down-cast.
 *
 * This pattern can be used to retroactively make library safer, by introducing a new "safe" super-interface of
 * an existing unsafe type.
 */

trait Foo {
  def safely(a: Int): Foo // some safe operations(s)
  def asUnsafe(): UnsafeFoo
  // an explicit way to get to the unsafe interface
  // the client programmer must explicitly "request" access
  // to unsafe operations, and can grep/use tooling to find
  // all such requests
  // technically, this is an unsafe downcast
  // we should prevent creation of any derived classes of Foo
  // other than UnsafeFoo to ensure it is actually safe
}

  trait UnsafeFoo extends Foo {
  def unsafely(a: Int): UnsafeFoo // some unsafe operation(s)
  def asFoo(): Foo
  // optionally, a way to return to the safe interface;
  // this is a safe up-cast because UnsafeFoo is-a Foo
}

class FooImpl extends UnsafeFoo {
  override def safely(a: Int): Foo = {
    println("safely", a)
    this
  }

  override def asFoo(): Foo= this

  override def asUnsafe(): UnsafeFoo = this

  override def unsafely(a: Int): UnsafeFoo = {
    println("unsafely", a)
    this
  }
}

object Foo {
  def apply(): Foo = new FooImpl()
  // note: the static (declared) type is the safe interface,
  // the dynamic (instantiated) type is the union of both interfaces
}



object Safe {
  def main(args: Array[String]): Unit = {

    val foo: Foo = Foo()

    foo.safely(1).safely(2)

    // foo.unsafely(1) is a compile error

    foo.asUnsafe().unsafely(1) // unless the client programmer explicitly requests it

    foo.asUnsafe().safely(2) // safe operations can be called on an unsafe object
    // a slightly different pattern makes it impossible to
    // call safe operations on an unsafe object (see below)

    foo.asUnsafe().unsafely(1).asFoo().safely(2) // we can "restore" the safe interface.
  }
}


/***
 * Separate an interface into two disjoint sets of methods, allowing a client programmer
 * to opt to exchange a reference to one set for a reference to the other only by explicit request, by using a cross-cast.
 * The exchange may be uni-directional or bi-directional, at the discretion of the library author.
 * Optionally, the union of the two interfaces may also be exposed to (some) clients.
 */

trait SafeBar {
  def safely(a: Int): SafeBar // some safe operations(s)
  def asUnsafe(): UnsafeBar
  // an explicit way to get to the unsafe interface
  // the client programmer must explicitly "request" access
  // to unsafe operations, and can grep/use tooling to find
  // all such requests
  // technically, this is an unsafe cross-cast
  // we should prevent creation of any derived classes of Bar
  // that do not also implement UnsafeBar
}

trait UnsafeBar  {
  def unsafely(a: Int): UnsafeBar // some unsafe operation(s)
  // def asSafeBar(): SafeBar
  // optionally, a way to return to the safe interface;
  // this is also a cross-cast because there is no relationship between bar and UnsafeBar
}

class BarUnion extends SafeBar with UnsafeBar { // implements both interfaces, allows cross-casts to succeed
  override def safely(a: Int): SafeBar = {
    println("safely ", a)
    this
  }

  override def asUnsafe(): UnsafeBar = this

  override def unsafely(a: Int): UnsafeBar = {
    println("unsafely ", a)
    this
  }
}

object BarUnion {
  def apply(): SafeBar = new BarUnion()
  // note: the static (declared) type is the safe interface,
  // the dynamic (instantiated) type is the union of both interfaces
}

object StricterSafe {
  def main(args: Array[String]): Unit = {

    val bar = BarUnion()

    bar.safely(1).safely(2)

    // bar.unsafely(1) is a compile error

    bar.asUnsafe().unsafely(1) // unless the client programmer explicitly requests it

    //bar.asUnsafe().safely(2) // safe operations CANNOT be called on an unsafe object

    // a slightly different pattern makes it possible to
    // call safe operations on an unsafe object (see above)

    //bar.asUnsafe().unsafely(1).asBar().safely(2) // we can't "restore" the safe interface.

    // UnsafeBar::asSafeBar is optional: we can omit it so that once an object is unsafe,
    // it can't be made safe again.

    // We would do this if accessing unsafe operations changes the object's state such that
    // it cannot afterwards be safe. For immutable objects, we can allow it or not, depending
    // on how much we want to emphasize the difference between "safe" and "usafe" operations.

    // In both of these patterns we are merely casting one object to present two different interfaces;
    // another alternative is to have asUnsafe return a wholly new object (which may or may not reference
    // the original object).

  }
}


