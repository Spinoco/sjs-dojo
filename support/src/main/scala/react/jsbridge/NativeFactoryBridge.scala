package react.jsbridge

import react.React.NativeFactory
import react.jsbridge

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
  * Used for javascript native react factories
  */
sealed trait NativeFactoryBridge {
  def apply(): ReactElement

  def apply(child: ReactElement, children: jsbridge.ReactElement*): ReactElement

  def apply(children: Seq[jsbridge.ReactElement]): ReactElement

  def keyed(children: Seq[(String, jsbridge.ReactElement)]): ReactElement

  def apply(children: js.Dictionary[ReactElement]): ReactElement
}


object NativeFactoryBridge {

  def apply(native: NativeFactory, props: js.Dictionary[_]): NativeFactoryBridge = {
    new NativeFactoryBridge {

      def apply(): ReactElement = native(props, ())

      def apply(child: ReactElement, children: ReactElement*): ReactElement = {
        if (children.isEmpty) native(props, child)
        else apply(child +: children)
      }

      def apply(children: Seq[ReactElement]): ReactElement = {
        val ch = (new js.Object).asInstanceOf[js.Dictionary[ReactElement]]
        var i = 0
        while (i < children.length) {
          //to be fast, we just iterate imperative
          ch.update("_" + i.toString, children(i))
          i = i + 1
        }
        native(props, ch)
      }

      def keyed(children: Seq[(String, ReactElement)]): ReactElement = {
        val ch = Dictionary[ReactElement](children: _*)
        native(props, ch)
      }

      def apply(children: Dictionary[ReactElement]): ReactElement = {
        native(props, children)
      }
    }
  }


}