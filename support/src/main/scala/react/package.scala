import scala.language.implicitConversions
import scalaz.\/._
import scalaz.{-\/, Equal, \/, \/-}

/**
  * Created by pach on 27/01/15.
  */
package object react {
  /**
    * Builds react class from name, initial state `A => S` and function that handles events and produces next state
    */
  def withState[A, E, S](name: String)(init: A => S)(pf: PartialFunction[ReactEvent, E])
    (f: (S, A \/ E) => (S, ReactAction)
      , fi: S => ReactAction = (_: S) => NoAction, cleanup: S => ReactAction = (_: S) => NoAction
    ): ReactClass[A] = {
    ReactClassBuilder[S, A]({
      case (None, -\/(a)) =>
        val first = init(a)
        val (s, action) = f(first, -\/(a))
        s -> (fi(first) ++ action)

      case (None, \/-(_)) => sys.error("Impossible state")

      case (Some(s), in) => in match {
        case -\/(a) => f(s, -\/(a))
        case \/-(e) if pf.isDefinedAt(e) =>
          f(s, \/-(pf(e)))
        case _ => s -> NoAction
      }
    }, name, cleanup).toReactClass
  }

  /**
    * Builds react class from name, initial state `S` and function that handles events and produces next state
    */
  def withState0[A, E, S](name: String)(init: S)(pf: PartialFunction[ReactEvent, E])
    (f: (S, A \/ E) => (S, ReactAction)
      , fi: S => ReactAction = (_: S) => NoAction
    ): ReactClass[A] = {
    ReactClassBuilder[S, A]({
      case (None, in) => in match {
        case -\/(a) =>
          val (first, action) = f(init, -\/(a))
          first -> (fi(first) ++ action)
        case \/-(e) if pf.isDefinedAt(e) => f(init, \/-(pf(e)))
        case _ => init -> NoAction
      }

      case (Some(s), in) => in match {
        case -\/(a) => f(s, -\/(a))
        case \/-(e) if pf.isDefinedAt(e) => f(s, \/-(pf(e)))
      }
    }, name).toReactClass
  }

  /**
    * A constructor that builds component where internal state is equaling to `A`
    * Note that except the first invocation, `f` is not consulted when current `A` equals to next `A` submitted.
    * This constrtuctor allows you as well to supply `fi` that is consulted on very first `A` received. That
    * gives chance to register any side effects (like for example timeout) on very first `A` received.
    *
    * Note that this may not be used, if component refers to `children`.
    */
  def withProps[A: Equal, E](name: String)(pf: PartialFunction[ReactEvent, E])
    (f: (A, A \/ E) => (A, ReactAction)
      , fi: A => ReactAction = (_: A) => NoAction
    ): ReactClass[A] = {
    def feq(cur: A, in: A \/ E): (A, ReactAction) = {
      in.fold(
        a => if (implicitly[Equal[A]].equal(cur, a)) a -> NoAction else f(cur, in)
        , _ => f(cur, in)
      )
    }
    def init(a: A): ReactAction = {
      val (na, out) = f(a, left(a))
      out ++ fi(na)
    }

    ReactClassBuilder[A, A]({
      case (None, -\/(a)) =>
        a -> init(a)
      case (None, \/-(_)) => sys.error("Impossible state")
      case (Some(cur), -\/(a)) =>
        feq(cur, -\/(a))
      case (Some(cur), \/-(e)) if pf.isDefinedAt(e) => feq(cur, \/-(pf(e)))
      case (Some(cur), _) => cur -> NoAction

    }, name).toReactClass
  }

  /**
    * Just display the content produced by `f`. Useful when content attaches any handlers (onClick...)
    * Note this will propagate all events from children to parent
    * @param name
    * @param f
    * @tparam A
    * @return
    */
  def display[A: Equal](name: String)(f: (A, A) => RenderAction): ReactClass[A] = {
    withProps[A, ReactEvent](name)({ case a => a })({
      case (a, -\/(na)) => na -> f(a, na)
      case (a, \/-(event)) => a -> action.sendToParent(event)
    })
  }

  //the same as display but render depends on current props only
  def display0[A: Equal](name: String)(f: A => RenderAction): ReactClass[A] = {
    display(name) { case (_, a) => f(a) }
  }

  /** Scala version of React.addons.classSet **/
  def classSet(cls: (String, Boolean)*): String = {
    cls.filter(_._2).map(_._1).mkString(" ")
  }
}
