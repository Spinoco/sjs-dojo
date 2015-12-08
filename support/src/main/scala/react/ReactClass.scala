package react

import org.scalajs.dom.Element
import react.jsbridge.ReactComponent

import scala.scalajs.js.Dictionary

/**
  * Typed wrapper for ReactClass
  */
case class ReactClass[-A](native: jsbridge.ReactClass, name: String)


object ReactClass {

  implicit class ReactClassSyntax[A](val self: ReactClass[A]) extends AnyVal {

    /** render this class to given dom element **/
    def renderTo(a: A)(node: Element): ReactComponent = {
      React.render(apply(a), node)
    }

    /** creates element from this class **/
    def apply(a: A, children: jsbridge.ReactElement*): jsbridge.ReactElement =
      React.createElement(self.native, Dictionary("value" -> a), children: _*)

    /** creates a factory. This has to be used always instead of using class directly **/
    def factory: ReactFactory[A] =
      ReactFactory(self)

  }


}
