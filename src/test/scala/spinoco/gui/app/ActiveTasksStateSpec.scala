package spinoco.gui.app

import org.scalacheck.{Prop, Properties}
import spinoco.messages.Id
import spinoco.messages.task.TaskRequestOwner.UserRequestOwner
import spinoco.messages.task._

import scala.collection.SortedSet
import scala.scalajs.js
import scala.util.Random
import scalaz.Tag

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 4.12.15
  */
class ActiveTasksStateSpec extends Properties("ActiveTasksState") {
  import Prop._

  val emptyState = ActiveTasksState()
  val now = new js.Date()
  val task = ClientRootUserTask(
    id = Tag(Id.next)
    , label = "task"
    , owner = UserRequestOwner(Tag(Id.next), None)
    , assignee = UnAssigned
    , assignableTo = None
    , due = TaskDue(now, None)
    , message = None
    , files = Seq()
    , tpe = CommonTask(false, false)
    , followUp = Set()
    , result = None
    , externalId = None
  )

  val tasks = (2 to 22).map{ id =>
    val due = new js.Date()
    due.setHours(id, 0, 0, 0)
    task.copy(
      id = Tag(Id.next)
      , due = TaskDue(due, None)
    )
  }.toVector

  property("insert new task to empty Vector") = protect {
    val result = emptyState.insertTask(task)

    result.allTasks ?= Vector(task)
  }

  property("insert new task before existing tasks") = protect {
    val due = new js.Date()
    due.setHours(0, 0, 0,0)
    val insert = task.copy(due = TaskDue(due, None))
    val expected = ActiveTasksState(tasks).insertTask(insert)

    expected.allTasks ?= (insert +: tasks)
  }

  property("insert new task after existing tasks") = protect {
    val due = new js.Date()
    due.setHours(23, 0, 0,0)
    val insert = task.copy(id = Tag(Id.next), due = TaskDue(due, None))
    val expected = ActiveTasksState(tasks).insertTask(insert)

    expected.allTasks ?= (tasks :+ insert)
  }

  /**
    * Performance improvements:
    * Seq implementation using lots of ++ and linear search: 169 s
    * Seq replaced by Vector: 14 s
    * Vector + binary search: <1 s
    *
    * for time measuring, remove comments in test.
    */
  property("insert 500 tasks into empty state") = protect {
    val nowTimestamp = now.getTime.toLong
    Random.setSeed(nowTimestamp)
    val insert: Seq[ClientRootUserTask] = (0 to 500).map{ id =>
      val due = new js.Date(Math.abs(Random.nextLong()) % nowTimestamp)
      task.copy(
        id = Tag(Id.next)
        , due = TaskDue(due, None)
      )
    }

    //val start = new js.Date()
    val result = insert.foldLeft(ActiveTasksState(Vector.empty)){ case (s, next) =>
      s.insertTask(next)
    }
    //val end = new js.Date()
    //println("Time performance:", end.getTime() - start.getTime(), "ms")

    val expected = insert.toVector.sortBy(_.due.due.getTime.toLong)

    result.allTasks.map(_.id) ?= expected.map(_.id)
  }

}
