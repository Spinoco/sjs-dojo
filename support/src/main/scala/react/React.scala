package react

import org.scalajs.dom.Element

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

/**
  * Accessor to React library
  *
  * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react">React</a>
  */

@js.native
@JSName("React")
object React extends js.Object {

  type NativeFactory = js.Function2[js.Dictionary[_], js.Any, jsbridge.ReactElement]

  /** @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.dom">React.DOM</a> **/
  val DOM: ReactDOM = js.native

  /** @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.children">React.Children</a> **/
  val Children: ReactChildren = js.native

  val addons: Addons = js.native

  /**
    * Create a component class, given a specification.
    * A component implements a render method which returns one single child.
    * That child may have an arbitrarily deep child structure.
    * One thing that makes components different than standard prototypal classes is that you don't need to call new on them.
    * They are convenience wrappers that construct backing instances (via new) for you.
    *
    * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.createclass">React.createClass</a>
    */
  def createClass(props: js.Dictionary[_]): jsbridge.ReactClass = js.native

  /**
    * Create and return a new ReactElement of the given type.
    * The type argument can be either an html tag name string (eg. 'div', 'span', etc), or a ReactClass (created via React.createClass).
    * @param clz         class of component
    * @param props       props of components
    * @param children    children
    * @return
    *
    * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.createelement>React.createElement</a>
    */
  def createElement(clz: jsbridge.ReactClass, props: js.Dictionary[_], children: jsbridge.ReactElement*): jsbridge.ReactElement = js.native

  /** @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.createelement>React.createElement</a> **/
  def createElement(name: String, props: js.Dictionary[_], children: jsbridge.ReactElement*): jsbridge.ReactElement = js.native

  /** @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.createfactory">React.createFactory</a> **/
  def createFactory(clz: jsbridge.ReactClass): NativeFactory = js.native

  /** @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.createfactory">React.createFactory</a> **/
  def createFactory(name: String): NativeFactory = js.native

  /** <a href="http://facebook.github.io/react/docs/top-level-api.html#react.proptypes">React.PropTypes</a> **/
  def PropTypes: PropTypes = js.native

  /**
    * Renders the element to given node
    *
    * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#reactdom.render">ReactDOM.render</a>
    */
  def render(element: jsbridge.ReactElement, domNode: Element): jsbridge.ReactComponent = js.native


  /**
    * test if supplied is valid element
    *
    * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.isvalidelement">React.isValidElement</a>
    */
  def isValidElement(element: js.Any): Boolean = js.native

  @js.native
  sealed trait ReactDOM extends js.Object {
    val a: NativeFactory = js.native
    val div: NativeFactory = js.native
    val section: NativeFactory = js.native
    val footer: NativeFactory = js.native
    val header: NativeFactory = js.native
    val span: NativeFactory = js.native
    val strong: NativeFactory = js.native
    val input: NativeFactory = js.native
    val textarea: NativeFactory = js.native
    val img: NativeFactory = js.native
    val p: NativeFactory = js.native
    val h1: NativeFactory = js.native
    val h2: NativeFactory = js.native
    val h3: NativeFactory = js.native
    val h4: NativeFactory = js.native
    val h5: NativeFactory = js.native
    val h6: NativeFactory = js.native
    val button: NativeFactory = js.native
    val label: NativeFactory = js.native
    val form: NativeFactory = js.native
    val fieldset: NativeFactory = js.native
    val ul: NativeFactory = js.native
    val li: NativeFactory = js.native
    val canvas: NativeFactory = js.native
    val br: NativeFactory = js.native

    val svg: NativeFactory = js.native
    val circle: NativeFactory = js.native

    val embed: NativeFactory = js.native
    val audio: NativeFactory = js.native
  }

  /** @see <a href="http://facebook.github.io/react/docs/addons.html">Add-ons</a> **/
  @js.native
  sealed trait Addons extends js.Object {
    /** @see <a href="http://facebook.github.io/react/docs/animation.html">ReactCSSTransitionGroup</a> **/
    val CSSTransitionGroup: jsbridge.ReactClass = js.native
  }


}

/**
  * Helpers for React Children
  */
@js.native
trait ReactChildren extends js.Object {

  /**
    * Returns count of children
    *
    * @see <a href="http://facebook.github.io/react/docs/top-level-api.html#react.children.count">React.Children.count</a>
    */
  def count(children: js.Any): Int = js.native

}

@js.native
trait PropTypes extends js.Object {
  def func: js.Function0[Unit] = js.native
}
