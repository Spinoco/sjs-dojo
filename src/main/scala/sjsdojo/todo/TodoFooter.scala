package sjsdojo.todo

import react.RenderAction

import scalaz.Equal

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */
case class TodoFooter(leftCount: Int, hasCompleted: Boolean, filter: TodoFilter.Value)

object TodoFooter {

  implicit val equal: Equal[TodoFooter] = Equal.equalA
  implicit lazy val factory = react.display0("TodoFooter")(impl.render).factory

  object impl {
    def render(props: TodoFooter): RenderAction = react.action.render{ implicit cmp =>
      import react.dom._

      val clearButton =
        if(props.hasCompleted) {
          button("className" -> "clear-completed", "onClick" -> eventHandler(_ => cmp.send(ClearCompleted)))(text("ClearCompleted"))
        } else empty

      footer("className" -> "footer")(
        span("className" -> "todo-count")(
          strong()(text(props.leftCount.toString))
        )
        , ul("className" -> "filters")(
          li()(a("className" -> react.classSet("selected" -> (props.filter == TodoFilter.ALL)), "onClick" -> eventHandler(_ => cmp.send(ChangeFilter(TodoFilter.ALL))))(text("All")))
          , li()(a("className" -> react.classSet("selected" -> (props.filter == TodoFilter.ACTIVE)), "onClick" -> eventHandler(_ => cmp.send(ChangeFilter(TodoFilter.ACTIVE))))(text("Active")))
          , li()(a("className" -> react.classSet("selected" -> (props.filter == TodoFilter.COMPLETED)), "onClick" -> eventHandler(_ => cmp.send(ChangeFilter(TodoFilter.COMPLETED))))(text("Completed")))
        )
        , clearButton
      )
    }
  }

}
