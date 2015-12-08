package react.jsbridge

import scala.scalajs.js


@js.native
trait ReactClass extends js.Object {

  def apply(config: js.Dictionary[_], children: ReactElement*): ReactElement = js.native

}
