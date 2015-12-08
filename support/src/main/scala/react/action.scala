package react

import react.jsbridge.ReactComponent

import scala.concurrent.duration.FiniteDuration

/**
  * Created by pach on 27/01/15.
  */
object action {

  /** sends given message to parent component **/
  def sendToParent(msg: ReactEvent): SendToParent =
    SendToParent(msg)

  /** causes to render supplied element giving a chance to define callbacks to component **/
  def render(f: jsbridge.ReactComponent => jsbridge.ReactElement): RenderAction =
    RenderAction(f)

  /** causes to render supplied element **/
  def render(el: => jsbridge.ReactElement): RenderAction =
    RenderAction(_ => el)

  /** scheduled to run given function after specified time **/
  def onTimeout(after: FiniteDuration)(f: ReactComponent => Unit): ComponentAction = {
    component { cmp =>
      org.scalajs.dom.window.setTimeout(() => {
        f(cmp)
      }, after.toMillis.toInt)
    }
  }

  /** effect to component **/
  def component(f: ReactComponent => Unit): ComponentAction = {
    ComponentAction(f)
  }

  def refAction(ref: String, f: (ReactComponent, ReactComponent) => Unit): RefAction = {
    RefAction(ref, f)
  }


}
