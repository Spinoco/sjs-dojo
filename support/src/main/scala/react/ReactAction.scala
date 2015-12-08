package react

import react.jsbridge.{ReactComponent, ReactElement}

/**
  * Generic action of react component
  */
sealed trait ReactAction

sealed trait SingleAction extends ReactAction

object ReactAction {

  implicit class ReactActionSyntax(val self: ReactAction) extends AnyVal {
    def ++(other: ReactAction): ReactAction = {
      self match {
        case sa: SingleAction => ListOfActions(sa, other)
        case NoAction => other
        case ListOfActions(head, tail) => ListOfActions(head, tail ++ other)
      }
    }
  }

}

/** No-op **/
case object NoAction extends ReactAction

/**
  * concatenates multiple actions
  */
case class ListOfActions(head: SingleAction, tail: ReactAction) extends ReactAction

/**
  * Renders updates gui that will be attached to dom of this element
  */
case class RenderAction(f: (ReactComponent) => ReactElement) extends SingleAction

/**
  * Performs given action on DOM Node backing this React Component
  * @param f
  */
case class ComponentAction(f: ReactComponent => Unit) extends SingleAction

/**
  * Performs effect `f` on reference (child) of current component
  * @param f first parameter is current component, second parameter is component with given ref
  */
case class RefAction(ref: String, f: (ReactComponent, ReactComponent) => Unit) extends SingleAction

/**
  * An action that allows to publish message of type `A` to immediate parent of the component.
  *
  */
case class SendToParent(msg: ReactEvent) extends SingleAction
