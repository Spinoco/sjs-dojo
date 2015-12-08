package sjsdojo.app

import org.scalajs.dom.Element
import react.React
import sjsdojo.todo.{TodoList, TodoItem}
import react.jsbridge.ReactElement._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 8.12.15
  */
object AppWindow extends JSApp {
  @JSExport
  def main(): Unit = {

    val node = org.scalajs.dom.window.document.getElementsByClassName("todoapp")
    val reactElement = TodoList(Seq(TodoItem(0, "Coding Dojo!", false))).toElement
    React.render(reactElement, node(0).asInstanceOf[Element])
  }
}
