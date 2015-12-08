package sjsdojo.todo

import react.jsbridge.ReactElement
import react.{NoAction, ReactAction, RenderAction, ReactEvent}
import react.jsbridge.ReactElement._

import scala.scalajs.js
import scalaz.{\/-, -\/, \/}

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */

case class TodoList(todos: Seq[TodoItem])

object TodoFilter extends Enumeration {
  val ALL, ACTIVE, COMPLETED = Value
}

case class RemoveItem(id: Int) extends ReactEvent
case class UpdateItem(id: Int, update: Option[String]) extends ReactEvent
case class ItemCompleted(id: Int, completed: Boolean) extends ReactEvent
case object ToggleAll extends ReactEvent
case class NewTodoValue(value: String) extends ReactEvent
case object AddNew extends ReactEvent
case object ClearCompleted extends ReactEvent
case class ChangeFilter(filter: TodoFilter.Value) extends ReactEvent

object TodoList {

  case class TodoListState(todos: Seq[TodoItem], filter: TodoFilter.Value, newTodo: Option[String])

  implicit lazy val factory = react.withState("TodoList")(impl.init) { case e: ReactEvent => e }(impl.process).factory

  object impl {
    def init(props: TodoList): TodoListState = {
      TodoListState(props.todos, TodoFilter.ALL, None)
    }

    def process(s: TodoListState, update: TodoList \/ ReactEvent): (TodoListState, ReactAction) = {
      update match {
        case -\/(props) =>
          val ns = init(props)
          ns -> render(ns)

        case \/-(RemoveItem(id)) =>
          val ns = s.copy(todos = s.todos.filterNot(_.id == id))
          ns -> render(ns)

        case \/-(UpdateItem(id, msg)) =>
          msg match {
            case Some(m) if m.trim.nonEmpty =>
              val ns = s.copy(todos = s.todos.map(i => if (i.id == id) i.copy(msg = m.trim) else i))
              ns -> render(ns)
            case _ =>
              val ns = s.copy(todos = s.todos.filterNot(_.id == id))
              s -> render(ns)
          }

        case \/-(ItemCompleted(id, completed)) =>
          val ns = s.copy(todos = s.todos.map(i => if (i.id == id) i.copy(completed = completed) else i))
          ns -> render(ns)

        case \/-(NewTodoValue(value)) =>
          val ns = s.copy(newTodo = Some(value))
          ns -> render(ns)

        case \/-(AddNew) =>
          s.newTodo match {
            case Some(todo) if todo.trim.nonEmpty =>
              val idx = if (s.todos.nonEmpty) s.todos.map(_.id).max + 1 else 0
              val ns = s.copy(newTodo = None, todos = TodoItem(idx, todo.trim, false) +: s.todos)
              ns -> render(ns)

            case _ =>
              s -> NoAction
          }

        case \/-(ToggleAll) if s.todos.forall(_.completed) =>
          val ns = s.copy(todos = s.todos.map(_.copy(completed = false)))
          ns -> render(ns)

        case \/-(ToggleAll) =>
          val ns = s.copy(todos = s.todos.map(_.copy(completed = true)))
          ns -> render(ns)

        case \/-(ClearCompleted) =>
          val ns = s.copy(todos = s.todos.filterNot(_.completed))
          ns -> render(ns)

        case \/-(ChangeFilter(filter)) =>
          val ns = s.copy(filter = filter)
          ns -> render(ns)

        case other =>
          org.scalajs.dom.window.console.info("other", other.asInstanceOf[js.Any])
          s -> NoAction
      }
    }


    def render(s: TodoListState): RenderAction = react.action.render { implicit cmp =>
      import react.dom._

      val filtered = s.filter match {
        case TodoFilter.ALL => s.todos
        case TodoFilter.ACTIVE => s.todos.filterNot(_.completed)
        case TodoFilter.COMPLETED => s.todos.filter(_.completed)
      }
      val todos = filtered.map(item => s"item_${item.id}" -> item.toElement)

      val allCompleted = s.todos.forall(_.completed == true)

      val allInfo =
        if (s.todos.nonEmpty)
          input(
            "className" -> "toggle-all"
            , "type" -> "checkbox"
            , "onChange" -> eventHandler(_ => cmp.send(ToggleAll))
            , "checked" -> allCompleted
          )()
        else empty

      val footer =
        if (s.todos.nonEmpty)
          TodoFooter(s.todos.filterNot(_.completed).size, s.todos.exists(_.completed), s.filter).toElement
        else empty

      div()(
        header("className" -> "header")(
          h1()(text("todos"))
          , input(
            "className" -> "new-todo"
            , "placeholder" -> "What needs to be done?"
            , "value" -> s.newTodo.orNull[String]
            , "onKeyDown" -> mapKeyToEvent("Enter" -> AddNew)
            , "onChange" -> valueChanged(NewTodoValue)
            , "autoFocus" -> true
          )()
        )
        , section("className" -> "main")(
          allInfo
          , ul("className" -> "todo-list")(
            js.Dictionary[ReactElement](todos: _*)
          )
        )
        , footer
      )
    }
  }

}
