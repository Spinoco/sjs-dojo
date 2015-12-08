package react.jsbridge

import react.ReactFactory

import scala.language.implicitConversions
import scala.scalajs.js


@js.native
trait ReactElement extends js.Object

object ReactElement {

  implicit def toReactElement[A: ReactFactory](a: A): ReactElement = {
    implicitly[ReactFactory[A]].apply(a)
  }

  implicit class ReactElementSyntax[A](val self: A)(implicit RF: ReactFactory[A]) {
    def toElement: ReactElement = toReactElement(self)

    def apply(): ReactElement = RF(self)

    def apply(child: ReactElement, children: ReactElement*): ReactElement = {
      if (children.isEmpty) RF(self, child)
      else RF(self, child +: children)
    }

    def apply(children: Seq[ReactElement]): ReactElement = RF(self, children)

    def apply(children: Map[String, ReactElement]): ReactElement = RF(self, children)

  }

  implicit class OptionElementSyntax(val self: Option[ReactElement]) {
    def orEmpty: ReactElement = self.fold(react.dom.empty)(identity)
  }

  implicit class StringElementSyntax(val self: String) {
    def toText: ReactElement = react.dom.text(self)
  }

}
