package sjsdojo

import react.{ReactAction, ReactEvent, RenderAction}

import scalaz.{\/-, -\/, \/, Equal}

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */

case class HelloWorld(msg: String)

object HelloWorld {

  implicit lazy val factory = react.withState("HelloWorld")(impl.init){case e: ReactEvent => e}(impl.process).factory

  case class HelloWorldState(msg: String)
  case class MsgChanged(value: String) extends ReactEvent

  object impl {

    def init(props: HelloWorld): HelloWorldState = {
      HelloWorldState(props.msg)
    }

    def process(s: HelloWorldState, update: HelloWorld \/ ReactEvent): (HelloWorldState, ReactAction) = {
      update match {
        case -\/(p) =>
          val ns = init(p)
          ns -> render(ns)

        case \/-(MsgChanged(value)) =>
          val ns = s.copy(msg = value)
          ns -> render(ns)
      }
    }

    def render(s: HelloWorldState): RenderAction = react.action.render{implicit cmp =>
      import react.dom._

      div()(
        div()(
          input("value" -> s.msg, "onChange" -> valueChanged(MsgChanged))()
        )
      , text(s"Hello World: ${s.msg}")
      )
    }
  }
}
