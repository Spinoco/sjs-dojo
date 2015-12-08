package react

import org.scalajs.dom._
import react.jsbridge.NativeFactoryBridge

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
  * Created by pach on 27/01/15.
  */
object dom {

  object addons {
    def cssTransitionGroup(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
      NativeFactoryBridge(React.createFactory(React.addons.CSSTransitionGroup), Dictionary(params: _*))
    }
  }

  val empty: jsbridge.ReactElement = {
    false.asInstanceOf[jsbridge.ReactElement] //unsafe but better than passing any to children
  }

  def text(value: String): jsbridge.ReactElement = {
    value.asInstanceOf[jsbridge.ReactElement] //allows to pass just string to native js.
  }

  def div(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.div, Dictionary(params: _*))
  }

  def section(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.section, Dictionary(params: _*))
  }

  def footer(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.footer, Dictionary(params: _*))
  }

  def span(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.span, Dictionary(params: _*))
  }

  def strong(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.strong, Dictionary(params: _*))
  }

  def header(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.header, Dictionary(params: _*))
  }

  def a(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.a, Dictionary(params: _*))
  }

  def textarea(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.textarea, Dictionary(params: _*))
  }

  def input(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.input, Dictionary(params: _*))
  }

  def label(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.label, Dictionary(params: _*))
  }


  def br(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.br, Dictionary(params: _*))
  }

  def p(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.p, Dictionary(params: _*))
  }

  def img(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.img, Dictionary(params: _*))
  }

  def h1(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h1, Dictionary(params: _*))
  }

  def h2(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h2, Dictionary(params: _*))
  }

  def h3(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h3, Dictionary(params: _*))
  }

  def h4(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h4, Dictionary(params: _*))
  }

  def h5(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h5, Dictionary(params: _*))
  }

  def h6(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.h6, Dictionary(params: _*))
  }

  def button(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.button, Dictionary(params: _*))
  }

  def form(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.form, Dictionary(params: _*))
  }

  def fieldset(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.fieldset, Dictionary(params: _*))
  }

  def ul(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.ul, Dictionary(params: _*))
  }

  def li(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.li, Dictionary(params: _*))
  }

  def canvas(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.canvas, Dictionary(params: _*))
  }

  def svg(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.svg, Dictionary(params: _*))
  }

  def circle(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.circle, Dictionary(params: _*))
  }

  def embed(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.embed, Dictionary(params: _*))
  }

  def audio(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.DOM.audio, Dictionary(params: _*))
  }

  def webview(params: (String, js.Any)*): jsbridge.NativeFactoryBridge = {
    NativeFactoryBridge(React.createFactory("webview"), Dictionary(params: _*))
  }

  def eventHandler(f: Event => Unit): js.Function1[Event, Unit] = {
    (e: Event) =>
      e.stopPropagation()
      val de = e.asInstanceOf[js.Dynamic]
      f(de.nativeEvent.asInstanceOf[Event])
  }

  def valueChanged[E <: ReactEvent](f: String => E)(implicit cmp: jsbridge.ReactComponent):js.Function1[Event, Unit] = {
    (e: Event) =>
      val de = e.asInstanceOf[js.Dynamic]
      cmp.send(f(de.target.value.asInstanceOf[String]))
  }

  def mapKeyToEvent[E <: ReactEvent](k2e: (String, E)*)(implicit cmp: jsbridge.ReactComponent): js.Function1[Event, Unit] = {
    (e: Event) =>
      val ke = e.asInstanceOf[KeyboardEvent]
      k2e.toMap.get(ke.key).foreach{event =>
        e.stopPropagation()
        e.preventDefault()
        cmp.send(event)
      }
  }

  def checkedChanged[E <: ReactEvent](f: Boolean => E)(implicit cmp: jsbridge.ReactComponent):js.Function1[Event, Unit] = {
    (e: Event) =>
      val de = e.asInstanceOf[js.Dynamic]
      cmp.send(f(de.target.checked.asInstanceOf[Boolean]))
  }
}
