package sjsdojo.todo

import react.{ReactAction, RenderAction, ReactEvent}
import sjsdojo.todo.TodoList.TodoListState

import scalaz.{\/-, -\/, \/}

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */
case class TodoItem(id: Int, msg: String, completed: Boolean)

object TodoItem {

  case class TodoItemState(props: TodoItem, editing: Option[String])

  case object EditMode extends ReactEvent
  case object ViewMode extends ReactEvent
  case class MsgChanged(msg: String)  extends ReactEvent

  implicit lazy val factory = react.withState("TodoItem")(impl.init){case e: ReactEvent => e}(impl.process).factory

  object impl {

    def init(props: TodoItem): TodoItemState = {
      TodoItemState(props, None)
    }

    def process(s: TodoItemState, update: TodoItem \/ ReactEvent): (TodoItemState, ReactAction) = {
      update match {
        case -\/(p) =>
          val ns = init(p)
          ns -> render(ns)

        case \/-(MsgChanged(msg)) =>
          val ns = s.copy(editing = Some(msg))
          ns -> render(ns)

        case \/-(EditMode) =>
          val ns = s.copy(editing = Some(s.props.msg))
          ns -> (render(ns) ++ react.action.refAction("edit", (_, node) => node.getDOMNode().focus()))

        case \/-(ViewMode) =>
          val ns = s.copy(editing = None)
          ns -> render(ns)

        case \/-(other) =>
          s -> react.action.sendToParent(other)
      }
    }

    def render(s: TodoItemState): RenderAction = react.action.render { implicit cmp =>
      import react.dom._

      val clz = react.classSet(
        "completed" -> s.props.completed
        , "editing" -> s.editing.isDefined
      )

      li("className" -> clz)(
        div("className" -> "view")(
          input("className" -> "toggle", "type" -> "checkbox", "checked" -> s.props.completed, "onChange" -> checkedChanged(ItemCompleted(s.props.id, _)))()
          , label("onDoubleClick" -> eventHandler(_ => cmp.send(EditMode)))(text(s.props.msg))
          , button("className" -> "destroy", "onClick" -> eventHandler(_ => cmp.send(RemoveItem(s.props.id))))()
        )
        , input(
          "className" -> "edit"
          , "ref" -> "edit"
          , "value" -> s.editing.orNull[String]
          , "onBlur" -> eventHandler(_ => cmp.send(UpdateItem(s.props.id, s.editing)))
          , "onChange" -> valueChanged(MsgChanged)
          , "onKeyDown" -> mapKeyToEvent("Enter" -> UpdateItem(s.props.id, s.editing), "Escape" -> ViewMode)
        )()
      )

    }
  }
}
