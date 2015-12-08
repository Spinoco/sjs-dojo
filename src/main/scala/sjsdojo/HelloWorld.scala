package sjsdojo

import react.RenderAction

import scalaz.Equal

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */

case class HelloWorld(msg: String)

object HelloWorld {

  implicit val equal: Equal[HelloWorld] = Equal.equalA
  implicit lazy val factory = react.display0("HelloWorld")(impl.render).factory

  object impl {

    def render(props: HelloWorld): RenderAction = react.action.render{ cmp =>
      import react.dom._

      div()(text(s"Hello World: ${props.msg}"))
    }
  }
}
