package ch08

import scala.language.{higherKinds, reflectiveCalls}
import scala.util.Try

/**
 *  Title: Functors
 *    - It allows to apply a function of one argument to each element stored in the container.
 *    - Works with "single" Effect
 * @tparam F
 */
trait Functor[F[_]] {
  def map[A,B](in: F[A])(f: A => B): F[B] // foldMap(in: F[A])(f: A => B): B //foldMap() returns B not F[B]!!

  def mapC[A,B](f: A => B): F[A] => F[B] = fa => map(fa)(f)
}

object Functor {
  implicit val bucketFunctor: Functor[List] = new Functor[List] {
    override def map[A, B](in: List[A])(f: A => B): List[B] = in.map(f)

    override def mapC[A, B](f: A => B): List[A] => List[B] = (_: List[A]).map(f)
  }

  implicit val optionFunctor: Functor[Option] = new Functor[Option] {
    override def map[A, B](in: Option[A])(f: A => B): Option[B] = in.map(f)
    override def mapC[A, B](f: A => B): Option[A] => Option[B] = (_: Option[A]).map(f)
//    override def mapC[A, B](f: A => B): Option[A] => Option[B] = (o: Option[A]) => o.map(f)
  }

  implicit def eitherFunctor[L] = new Functor[({ type T[A] = Either[L, A] })#T] {
    override def map[A, B](in: Either[L, A])(f: A => B): Either[L, B] = in.map(f)
    override def mapC[A, B](f: A => B): Either[L, A] => Either[L, B] = (_: Either[L, A]).map(f)
  }

  implicit val tryFunctor: Functor[Try] = new Functor[Try] {
    override def map[A, B](in: Try[A])(f: A => B): Try[B] = in.map(f)
    override def mapC[A, B](f: A => B): Try[A] => Try[B] = (_: Try[A]).map(f)
  }
}
