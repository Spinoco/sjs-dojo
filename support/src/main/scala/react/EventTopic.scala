package react

import org.scalajs.dom.{CustomEvent, Element}
import react.jsbridge.ReactComponent

import scalaz.{Tag, @@}


/**
  * React component as a Source of message sent to computation topic
  */
case class ReactSource(component: ReactComponent) extends Source

/**
  * A different topic as sourve of message sent to the computation
  */
case class TopicSource[A](topic: ComputationTopic[A], f: ReactEvent => A) extends Source


case class TaskSource(cb: ReactEvent => Unit) extends Source

/**
  * Source of message sent to computation topic
  */
sealed trait Source

object Source {

  implicit class SourceSyntax(val self: Source) extends AnyVal {

    def reply(e: ReactEvent): Unit = {
      self match {
        case s: ReactSource => s.component.send(e)
        case s: TopicSource[_] => s.topic.send(s.f(e))
        case TaskSource(cb) => cb(e)
      }
    }
  }

}

/**
  * Destination for computations. This accepts messages of type `A` and handles any computations with them
  * Typically these are singleton services like HttpService
  */
case class ComputationTopic[A](name: String @@ ComputationTopic[A], target: Element)

/**
  * Created by pach on 27/01/15.
  */
object ComputationTopic {


  /** snytax to sending to any topic thatis not of `ReactEvent`, type that means not to components **/
  implicit class ComputationTopicSyntax[E](val self: ComputationTopic[E]) extends AnyVal {
    /**
      * Publishes `e` to given topic.
      * @param e
      */
    def send(e: E): Unit = {
      //TODO was changed to custom detail from detail due to chrome bug
      val evt = org.scalajs.dom.document.createEvent("CustomEvent").asInstanceOf[CustomEvent]
      evt.initCustomEvent(Tag.unwrap(self.name), false, true, scalajs.js.Dictionary("value" -> e))
      evt.asInstanceOf[scalajs.js.Dynamic].updateDynamic("customDetail")(scalajs.js.Dictionary("value" -> e))
      self.target.dispatchEvent(evt)
    }


    /** sends `e`, but also sets the reply address, used when resending event back to service **/
    def forward(e: E, replyTo: Source): Unit = {
      //TODO was changed to custom detail from detail due to chrome bug
      val evt = org.scalajs.dom.document.createEvent("CustomEvent").asInstanceOf[CustomEvent]
      evt.initCustomEvent(Tag.unwrap(self.name), false, true, scalajs.js.Dictionary("value" -> e, "replyTo" -> replyTo))
      evt.asInstanceOf[scalajs.js.Dynamic].updateDynamic("customDetail")(scalajs.js.Dictionary("value" -> e, "replyTo" -> replyTo))
      self.target.dispatchEvent(evt)
    }

  }


}

object ReactSource {

  /** syntax fro replying to the components **/
  implicit class ReactSourceSyntax(val self: ReactSource) extends AnyVal {
    /** replies to react component. Note that `send`  won't work as reply since they will be discarded by Component **/
    def reply(e: ReactEvent): Unit = {
      self.component.send(e)
    }
  }

}
