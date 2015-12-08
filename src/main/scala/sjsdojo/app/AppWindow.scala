package sjsdojo.app

import org.scalajs.dom.Element
import react.jsbridge.ReactElement._
import react.React
import sjsdojo.HelloWorld

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
    val reactElement = HelloWorld("Spinoco!").toElement
    React.render(reactElement, node(0).asInstanceOf[Element])
  }
}
