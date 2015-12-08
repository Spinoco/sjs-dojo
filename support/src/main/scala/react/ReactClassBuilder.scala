package react

import org.scalajs.dom.{CustomEvent, Event, window}
import react.jsbridge.ReactComponent

import scala.annotation.tailrec
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.JSConverters._
import scalaz.{-\/, \/, \/-}


/**
  * Specification for react component. The main logic of the component shall reside in process.
  *
  * state of component contains current component process (state.scala)
  * @param process         Initial function to represent state of the component. It may result in actions that
  *                        will eventually result in component being rendered.
  *                        If component won't emit at least one `RenderAction` the component will skip its `render` method.
  * @param name            name of React component - is displayed in React tools.
  */
case class ReactClassBuilder[S, -A](
  process: (Option[S], A \/ ReactEvent) => (S, ReactAction)
  , name: String
  , cleanup: S => ReactAction = (_: S) => NoAction
)


object ReactClassBuilder {

  implicit class ReactClassBuilderSyntax[S, A](val self: ReactClassBuilder[S, A]) extends AnyVal {
    def toReactClass: ReactClass[A] =
      ReactClass(React.createClass(impl.build(self)), self.name)

    def toReactFactory: ReactFactory[A] =
      ReactFactory(toReactClass)
  }


  /**
    * Runs any actions that are in state. This is fed by actions sequence (that may be empty) on several places
    * - when we feed process by event (send, eventHandler)
    * - when new props are received and we feed component
    * - on mounting the new component
    * - after component has been updated
    */
  def runActions[S, A](comp: jsbridge.ReactComponent): Unit =
    impl.runStateActions[S, A](comp)

  /**
    * Helper that sends event to given react component
    */
  def send[S, A](to: ReactComponent, event: ReactEvent): Unit =
    impl.send[S, A](to, event)


  object impl {

    /**
      * State of the component. This is mutated as we need when feeding messages to the process.
      * We need this in var to get around deferred nature of the React setState
      *
      * NOTE THIS SHALL NEVER BE ACCESSED DIRECTLY. THIS IS HANDLED INTERNALLY BY REACTCLASSBUILDER
      *
      * @param process  Holds up to date reference to process of the component
      * @param render   Holds the next render state.
      *                 Note that  when this is set, only feed process but never run any actions.
      *                 This has to be set to None when componentDidUpdate is invoked and then all actions in `actions` shall be run.
      * @param action  Holds actions that needs to be processed.
      * @param handler  Handler that handles the messages `ReactEvent` from children.
      */
    case class ComponentState[S, A](
      process: (Option[S], A \/ ReactEvent) => (S, ReactAction)
      , var state: Option[S] = None
      , var render: Option[RenderAction] = None
      , var action: ReactAction = NoAction
      , var handler: Option[js.Function1[Event, Unit]] = None
    )


    implicit class ReactComponentSyntax(val self: jsbridge.ReactComponent) extends AnyVal {
      def cstate[S, A]: ComponentState[S, A] = self.state("scala").asInstanceOf[ComponentState[S, A]]

      def getA[A]: Option[A] = {
        if (self.props == null) None
        else self.props.get("value").map(_.asInstanceOf[A])
      }

      def findRender(action: ReactAction): (Option[RenderAction], ReactAction) = {
        def go(current: ReactAction, buff: ReactAction): (Option[RenderAction], ReactAction) = {
          current match {
            case ListOfActions(ra: RenderAction, tail) =>
              val (maybeRender, tailTail) = findRender(tail)
              (maybeRender orElse Some(ra)) -> (buff ++ tailTail)
            case ListOfActions(other, tail) => go(tail, buff ++ other)
            case ra: RenderAction => Some(ra) -> buff
            case other => None -> (buff ++ other)
          }
        }
        go(action, NoAction)
      }

      /** Feeds component with `A` and eventually returns any actions that needs to be run before eventually component has to be rendered **/
      def feedA[S, A](a: A): Unit = {
        feed(-\/(a))
      }

      /** Feed component with event `E` and eventually returns any actions that needs to be run before eventually component has to be rendered **/
      def feedEvent[S, A](event: ReactEvent): Unit = {
        feed(\/-(event))
      }

      def feed[S, A](aOrE: A \/ ReactEvent): Unit = {
        val (state, action) = cstate[S, A].process(cstate[S, A].state, aOrE)
        cstate[S, A].state = Some(state)

        val (render, reactAction) = findRender(action)
        cstate[S, A].action = reactAction

        //window.console.log("event action", reactAction.asInstanceOf[js.Any], render.asInstanceOf[js.Any])
        cstate[S, A].render = render orElse cstate[S, A].render
      }

      def render_? : Boolean = cstate.render.isDefined

      def clearRender: Unit = cstate.render = None

      def registerHandler(handler: js.Function1[Event, Unit]): Unit = cstate.handler = Some(handler)
    }

    /**
      * Helper that sends event to given react component
      */
    def send[S, A](to: ReactComponent, event: ReactEvent): Unit = {
      to.feedEvent[S, A](event)

      if (!js.isUndefined(to._reactInternalInstance)) {
        if (to.render_? && to.isMounted()) to.setState(Dictionary("render" -> 0))
        else ReactClassBuilder.runActions(to)
      }
    }


    /**
      * Builds the component specification (actually the properties passed to React.createClass
      *
      * Implementation is based on specific flow of call backs fro react which is :
      *
      * 1. When node is first mounted on DOM:
      * - componentWillMount is called with specified(initial) props and state of the component.
      * new instance of component process is created here.
      * - componentDidMount is called and at this moment getDOMNode is active
      *
      * 2. When parent note is updated or setProps on root component is invoked
      * - componentWillReceiveProps is invoked with next props supplied
      * - shouldComponentUpdate is invoked with next props and state
      * - componentWillUpdate is invoked in case shouldComponentUpdate yield true
      * - componentDidUpdate is invoked
      *
      * 3. When component's setState is called
      * - shouldComponentUpdate is invoked
      * - componentWillUpdate is invoked
      * - componentDidUpdate is invoked
      *
      * 4. When ForceUpdate is called
      * - render is invoked
      * - componentDidUpdate is invoked
      *
      *
      * React Class is backed by Process1. writer's internal state is kept inside local var in this
      * build function.
      *
      *
      */
    def build[S, A](comp: ReactClassBuilder[S, A]): js.Dictionary[_] = {

      Map(
        "displayName" -> comp.name
        , "getInitialState" -> ((() => initialState[S, A](comp.process)): js.Function0[js.Dictionary[_]])
        , "componentWillMount" -> (componentWillMount[S, A] _: js.ThisFunction0[jsbridge.ReactComponent, Unit])
        , "componentDidMount" -> (componentDidMount[S, A] _: js.ThisFunction0[jsbridge.ReactComponent, Unit])
        , "componentWillReceiveProps" -> (componentWillReceiveProps[S, A] _: js.ThisFunction1[jsbridge.ReactComponent, js.Dynamic, Unit])
        , "shouldComponentUpdate" -> (shouldComponentUpdate[S, A] _: js.ThisFunction2[jsbridge.ReactComponent, js.Any, js.Dictionary[_], Boolean])
        , "componentWillUpdate" -> (componentWillUpdate[S, A] _: js.ThisFunction2[jsbridge.ReactComponent, js.Any, js.Dictionary[_], Unit])
        , "componentDidUpdate" -> (componentDidUpdate[S, A] _: js.ThisFunction2[jsbridge.ReactComponent, js.Any, js.Any, Unit])
        , "componentWillUnmount" -> (componentWillUnmount[S, A](comp.cleanup) _: js.ThisFunction0[jsbridge.ReactComponent, Unit])
        , "render" -> (render[S, A] _: js.ThisFunction0[jsbridge.ReactComponent, js.Any])
        , "contextTypes" -> js.Dynamic.literal("router" -> React.PropTypes.func)
      ).toJSDictionary

    }


    /** @see <a href="http://facebook.github.io/react/docs/component-specs.html#getinitialstate>getInitialState</a> **/
    def initialState[S, A](process: (Option[S], A \/ ReactEvent) => (S, ReactAction)): js.Dictionary[_] =
      Dictionary("scala" -> ComponentState[S, A](process))


    /**
      * React handler to be call before component will be mounted
      *
      * feeds process with props value and collects results that are fed to state.
      * If value is not provided iniitally, logs error an clears the state
      * @param comp
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#mounting-componentdidmount">componentDidMount</a>
      */
    def componentWillMount[S, A](comp: jsbridge.ReactComponent): Unit = {
      comp.getA[A] match {
        case Some(a) =>
          comp.feedA[S, A](a)
          runStateActions(comp)

        case None =>
          window.console.error("No initial property state set for  component. ", comp)
      }
    }


    /**
      * This is invoked in case the component is receiving the new props. Just updating the received with some `I`
      * Note: props.value set to undefined is valid value (Unit == no props == undefined)
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#updating-componentwillreceiveprops">componentWillReceiveProps</a>
      */
    def componentWillReceiveProps[S, A](comp: jsbridge.ReactComponent, props: js.Dynamic): Unit = {
      if (js.isUndefined(props) || (props.value == null)) {
        window.console.error("Component did received props, but no `A` has been set", comp)
      } else {
        comp.feedA(props.value.asInstanceOf[A])
      }
    }

    /**
      * Yields the true if we have to render anything.
      * Under all other than initial, this runs the writer, and when render is nonempty yields to true, else false
      * If render is empty, all effects are processed in this function (componentWillUpdate is invoked after render)
      * If render is nonEmpty, all effects are processed in componentWillUpdate
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#updating-shouldcomponentupdate">shouldComponentUpdate</a>
      */
    def shouldComponentUpdate[S, A](comp: jsbridge.ReactComponent, props: js.Any, s: js.Dictionary[_]): Boolean = {
      comp.render_?
    }

    /**
      * nothing at willUpdate state
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#updating-componentwillupdate">componentWillUpdate</a>
      */
    def componentWillUpdate[S, A](comp: jsbridge.ReactComponent, props: js.Any, s: js.Dictionary[_]): Unit = {
    }

    /**
      * State cleanup operation is here.
      * We inspect state and set it to `initial`.
      * Also if the received is nonEmpty we making sure that we run one more cycle
      * Also we register any DOMAction here and emit SpTypedEvent if any `O` was emitted
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#updating-componentdidupdate">componentDidUpdate</a>
      */
    def componentDidUpdate[S, A](comp: jsbridge.ReactComponent, props: js.Any, s: js.Any): Unit = {
      runStateActions(comp)
      if (comp.render_?) comp.forceUpdate
    }

    /**
      * Handler to component when the component have been mounted. Called only once per lifetime of component
      *
      * We register event handlers here. The event handlers allows us to push back any events
      * produced by children nodes so we can react to them accordingly.
      * @param comp
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#mounting-componentdidmount">componentDidMount</a>
      */
    def componentDidMount[S, A](comp: jsbridge.ReactComponent): Unit = {
      val domNode = comp.getDOMNode()


      val handler: js.Function1[Event, Unit] = (e: Event) => e match {
        case ce: CustomEvent if ce.detail != null && !js.isUndefined(ce.detail) =>
          ce.detail.asInstanceOf[Dictionary[_]].get("value") match {
            case Some(re: ReactEvent) =>
              ce.stopPropagation()
              comp.send(re)
            case _ => ()
          }
        case _ => ()

      }


      if (domNode != null) domNode.addEventListener("ReactEvent", handler)
      else window.console.error("Component has null domNode ( factory returned empty?)", comp)
      comp.registerHandler(handler)

      //look into actions if some of them don't need to be run after initial render
      if (comp.render_?) comp.forceUpdate()
      runStateActions(comp)
    }


    /**
      * Component is unmounting. Apart from removing eventListeners, we shall perform any DOM actions, dispatch any events
      * etc from the fallback of the process.
      * State of the process will be set to halt.
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#unmounting-componentwillunmount">componentWillUnmount</a>
      */
    def componentWillUnmount[S, A](cleanup: S => ReactAction)(comp: jsbridge.ReactComponent): Unit = {
      comp.cstate[S, A].state.foreach(s => runActions(comp, cleanup(s)))
      comp.cstate.handler.foreach { h =>
        comp.getDOMNode.removeEventListener("ReactEvent", h)
      }
    }

    /**
      * Renders the component actually. This will take anything in state.render and will just run the rendering for it.
      * @param comp
      * @return
      *
      * @see <a href="http://facebook.github.io/react/docs/component-specs.html#render">render</a>
      */
    def render[S, A](comp: jsbridge.ReactComponent): js.Any = {
      import react.dom._

      comp.cstate[S, A].render match {
        case Some(RenderAction(f)) =>
          comp.clearRender
          f(comp)


        case None =>
          div()(text("Invalid render content, state.render empty"))
      }
    }

    /**
      * Runs any actions that are in state. This is fed by actions sequence (that may be empty) on several places
      * - when we feed process by event (send, eventHandler)
      * - when new props are received and we feed component
      * - on mounting the new component
      * - after component has been updated
      */
    def runActions[S, A](comp: jsbridge.ReactComponent, actions: ReactAction): Unit = {

      def runSingleAction(action: SingleAction): Unit = {

        action match {
          case ComponentAction(f) =>
            f(comp)

          case RefAction(ref, f) =>
            comp.refs.get(ref).map { native =>
              f(comp, native)
            }.getOrElse {
              window.console.error("Reference to react component unavailable", ref, comp)
            }

          case SendToParent(a) =>
            // we travers to find next scala parent
            // if found we invoke directly sen
            @tailrec
            def toParent(of: ReactComponent): Unit = {
              val p = of.ownerUnsafe
              if (p == null || js.isUndefined(p)) window.console.warn("Cannot send event to parent from root component", a.asInstanceOf[js.Any], comp)
              else {
                if (p.state != null && p.state.isDefinedAt("scala")) send(p, a)
                else toParent(p)
              }
            }
            toParent(comp)

          case _: RenderAction =>
            sys.error("Impossible, no render here")
        }
      }

      def go(action: ReactAction): Unit = {
        action match {
          case action: SingleAction => runSingleAction(action)
          case ListOfActions(singleAction, next) =>
            runSingleAction(singleAction)
            go(next)
          case NoAction =>
        }
      }

      go(actions)


    }

    def runStateActions[S, A](comp: jsbridge.ReactComponent): Unit = {
      if (!js.isUndefined(comp._reactInternalInstance) && comp.isMounted()) {
        runActions(comp, comp.cstate[S, A].action)
        comp.cstate[S, A].action = NoAction
      }
    }
  }

}
