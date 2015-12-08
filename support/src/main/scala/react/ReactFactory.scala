package react

import react.jsbridge.ReactElement

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
 * Created by pach on 28/01/15.
 */
sealed trait ReactFactory[A] {
  def apply(a:A): ReactElement
  def apply(a: A, children: Vector[(String, ReactElement)]): ReactElement
  def apply(a:A,child:jsbridge.ReactElement, children: jsbridge.ReactElement* ):ReactElement
  def apply(a:A,children:Seq[jsbridge.ReactElement]):ReactElement
  def apply(a:A,children:Map[String,jsbridge.ReactElement]):ReactElement
  val cls: ReactClass[A]
  val native: React.NativeFactory
}



object ReactFactory {

  def apply[A](clz:ReactClass[A]):ReactFactory[A] = {

    new ReactFactory[A] {
      val native: React.NativeFactory = React.createFactory(clz.native)

      val cls: ReactClass[A] = clz

      def apply(a: A): ReactElement = {
        native(Dictionary("value" -> a), ())
      }

      def apply(a: A, children: Vector[(String, ReactElement)]): ReactElement = {
        val ch = (new js.Object).asInstanceOf[js.Dictionary[ReactElement]]
        children.foreach(c => ch.update(c._1, c._2))
        native(Dictionary("value" -> a), ch)
      }

      def apply(a: A, child: ReactElement, children: ReactElement*): ReactElement = {
        if (children.isEmpty)   native(Dictionary("value" -> a),child)
        else apply(a,child +: children)
      }

      def apply(a: A, children: Seq[ReactElement]): ReactElement = {
        val ch = (new js.Object).asInstanceOf[js.Dictionary[ReactElement]]
        var i = 0
        while (i < children.length) { //to be fast, we just iterate imperative
          ch.update("_"+i.toString,children(i))
          i = i +1
        }
        native(Dictionary("value" -> a),ch)
      }

      def apply(a: A, children: Map[String, ReactElement]): ReactElement = {
        val ch = (new js.Object).asInstanceOf[js.Dictionary[ReactElement]]
        children.foreach { case (k,el) => ch.update(k,el) }
        native(Dictionary("value" -> a),ch)
      }
    }
  }




}