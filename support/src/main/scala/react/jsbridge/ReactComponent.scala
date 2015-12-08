package react.jsbridge

import org.scalajs.dom.HTMLElement
import react._

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
  * Created by pach on 26/01/15.
  *
  */

/**
  * @see <a href="http://facebook.github.io/react/docs/component-api.html">React.Component</a>
  */
@js.native
sealed trait ReactComponent extends js.Object {

  /** @see <a href="http://facebook.github.io/react/docs/component-api.html#setstate"></a> **/
  def setState(next: Dictionary[_]): Unit = js.native

  /** @see <a href="http://facebook.github.io/react/docs/component-api.html#replacestate"></a> **/
  def replaceState(next: Dictionary[_]): Unit = js.native

  /** @see <a href="http://facebook.github.io/react/docs/component-api.html#forceupdate"></a> **/
  def forceUpdate(): Unit = js.native

  /** @see <a href="http://facebook.github.io/react/docs/component-api.html#getdomnode"></a> **/
  def getDOMNode(): HTMLElement = js.native

  /** @see <a href="http://facebook.github.io/react/docs/component-api.html#ismounted"></a> **/
  def isMounted(): Boolean = js.native

  /** @see <a href=""></a> **/
  def state: Dictionary[_] = js.native

  /** @see <a href=""></a> **/
  def props: Dictionary[js.Any] = js.native

  /** @see <a href=""></a> **/
  def refs: Dictionary[ReactComponent] = js.native

  /** @see <a href=""></a> **/
  def context: Dictionary[js.Any] = js.native

  /** @see <a href=""></a> **/
  def _reactInternalInstance: js.Object = js.native


}

object ReactComponent {

  implicit class ReactComponentSyntax(val self: ReactComponent) extends AnyVal {
    def params: js.Any = {
      self.props.getOrElse("params", Dictionary.empty[js.Any])
    }

    /** sends event `E` to this component **/
    def send[S, A](event: ReactEvent): Unit =
      ReactClassBuilder.send[S, A](self, event)

    /** Returns first child if there is any in children **/
    def firstChild: Option[ReactElement] = {
      self.props.get("children").flatMap { ch =>
        val count = React.Children.count(ch)
        if (count == 0) None
        else if (count == 1 && React.isValidElement(ch)) Some(ch.asInstanceOf[ReactElement])
        else if (js.Array.isArray(ch)) ch.asInstanceOf[js.Array[ReactElement]].headOption
        else ch.asInstanceOf[js.Dictionary[ReactElement]].headOption.map(_._2)
      }
    }

    /** returns children typed as j.Dictionary **/
    def children: js.Dictionary[ReactElement] = {
      self.props.get("children") match {
        case None => js.Dictionary.empty
        case Some(ch) =>
          val count = React.Children.count(ch)
          if (count == 0) js.Dictionary.empty
          else if (count == 1 && React.isValidElement(ch)) js.Dictionary("_0" -> ch.asInstanceOf[ReactElement])
          else if (js.Array.isArray(ch)) {
            val arry = ch.asInstanceOf[js.Array[ReactElement]]
            val dict = Dictionary.empty[ReactElement]
            for (i <- 0 until arry.length) {
              dict.update("_" + i, arry(i))
            }
            dict
          } else ch.asInstanceOf[js.Dictionary[ReactElement]]

      }

    }

    /** finds owner of this component. Note this may return null for root component   **/
    protected[react] def ownerUnsafe: ReactComponent = {
      self.asInstanceOf[js.Dynamic]._reactInternalInstance._currentElement._owner._instance.asInstanceOf[ReactComponent]
    }

    /** optionally extracts typed react element from refs. note that this is typing unsafely **/
    def ref[A <: ReactElement](key: String): Option[A] = {
      self.refs.get(key).map(_.asInstanceOf[A])
    }
  }

}
