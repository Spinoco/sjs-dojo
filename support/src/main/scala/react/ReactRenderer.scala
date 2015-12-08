package react

/**
 * Helper class for simple state renderer. usually used inside impl as object render
 */
trait ReactRenderer[S] {
  def apply(s:S, next: ReactAction = NoAction):(S, ReactAction) = {
    s -> (apply(s) ++ next)
  }

  def apply(s: S): RenderAction
}
