package spinoco.gui.component.cw

import org.scalacheck.Prop
import org.scalacheck.Properties
import spinoco.gui.components.cw._
import spinoco.messages.Id
import spinoco.messages.callcontrol.PhoneNumber

import scalaz.{Tag, @@}

/**
  * Created with IntelliJ IDEA.
  * User: raulim
  * Date: 26.11.15
  */
class ContactFormSpec extends Properties("ContactForm") {

  import Prop._
  import ContactForm.impl._

  type TableRowsIndex = Seq[(Int, String, String @@ PhoneNumber)]
  type TableRows = Seq[(String, String @@ PhoneNumber)]
  type DiffResult = Seq[(Option[(String, String @@ PhoneNumber)], Option[(String, String @@ PhoneNumber)], Option[Int])]

  val origIndex: TableRowsIndex = Seq(
    (0, "AAA", Tag.of[PhoneNumber]("AAA"))
    , (1, "BBB", Tag.of[PhoneNumber]("BBB"))
    , (2, "CCC", Tag.of[PhoneNumber]("CCC"))
    , (3, "DDD", Tag.of[PhoneNumber]("DDD"))
    , (4, "EEE", Tag.of[PhoneNumber]("EEE"))
  )

  val origValues: Seq[(String, String @@ PhoneNumber)] = Seq(
    "AAA" -> Tag("AAA")
    , "BBB" -> Tag("BBB")
    , "CCC" -> Tag("CCC")
    , "DDD" -> Tag("DDD")
    , "EEE" -> Tag("EEE")
  )

  def protectEditTable(orig: TableRowsIndex, update: LabeledTableEvent, expected: TableRowsIndex, length: Option[Int] = None) = {
    val result = editTable(orig, length.getOrElse(orig.length), update)
    expected ?= result
  }

  property("editTable - row added") = protect {
    val update = TableRowAdded(Id.next, "FFF", "FFF")
    val expected = origIndex :+(5, "FFF", Tag.of[PhoneNumber]("FFF"))

    protectEditTable(origIndex, update, expected)
  }

  property("editTable - first row removed") = protect {
    val update = TableRowDeleted(Id.next, 0)

    protectEditTable(origIndex, update, origIndex.tail)
  }

  property("editTable - last row removed") = protect {
    val update = TableRowDeleted(Id.next, origIndex.length - 1)

    protectEditTable(origIndex, update, origIndex.init)
  }

  property("editTable - row removed from middle") = protect {
    val update = TableRowDeleted(Id.next, 2)
    val expected = origIndex.patch(2, Nil, 1)
    protectEditTable(origIndex, update, expected)
  }

  property("editTable - unknown row removed") = protect {
    val update = TableRowDeleted(Id.next, 9999)
    protectEditTable(origIndex, update, origIndex)
  }

  property("editTable - row edited") = protect {
    val update = TableRowEdited(Id.next, (0, "XXX", "XXX"))
    val expected = origIndex.updated(0, (0, "XXX", Tag.of[PhoneNumber]("XXX")))
    protectEditTable(origIndex, update, expected)
  }

  property("editTable - row moved 0 -> 4") = protect {
    val update = TableRowMoved(Id.next, 0, 4)
    val expected = Seq(
      (1, "BBB", Tag.of[PhoneNumber]("BBB"))
      , (2, "CCC", Tag.of[PhoneNumber]("CCC"))
      , (3, "DDD", Tag.of[PhoneNumber]("DDD"))
      , (4, "EEE", Tag.of[PhoneNumber]("EEE"))
      , (0, "AAA", Tag.of[PhoneNumber]("AAA"))
    )

    protectEditTable(origIndex, update, expected)
  }

  property("editTable - row moved 3 -> 1") = protect {
    val update = TableRowMoved(Id.next, 3, 1)
    val expected = Seq(
      (0, "AAA", Tag.of[PhoneNumber]("AAA"))
      , (3, "DDD", Tag.of[PhoneNumber]("DDD"))
      , (1, "BBB", Tag.of[PhoneNumber]("BBB"))
      , (2, "CCC", Tag.of[PhoneNumber]("CCC"))
      , (4, "EEE", Tag.of[PhoneNumber]("EEE"))
    )

    protectEditTable(origIndex, update, expected)
  }

  def protectDiffSeq(orig: TableRows, originalIndex: TableRowsIndex, expected: DiffResult) = {
    val result = diffSeq[PhoneNumber](orig, originalIndex)
    result ?= expected
  }

  def processUpdates(orig: TableRowsIndex, updates: LabeledTableEvent*): TableRowsIndex = {
    val size = orig.length
    updates.foldLeft(orig) { case (result, u) =>
      editTable[PhoneNumber](result, size, u)
    }
  }

  property("diffSeq - row added") = protect {
    val updated = processUpdates(origIndex, TableRowAdded(Id.next, "FFF", "FFF"))
    protectDiffSeq(origValues, updated, Seq((None, Some("FFF" -> Tag("FFF")), Some(5))))
  }

  property("diffSeq - row edit") = protect {
    val updated = processUpdates(origIndex, TableRowEdited(Id.next, (0, "XXX", "XXX")))
    val expected: DiffResult = Seq(
      (Some("AAA" -> Tag("AAA")), Some("XXX" -> Tag("XXX")), None)
    )

    protectDiffSeq(origValues, updated, expected)
  }

  property("diffSeq - row moved 0 -> 3") = protect {
    val updated = processUpdates(origIndex
      , TableRowMoved(Id.next, 0, 3)
    )

    val expected: DiffResult = Seq(
      (Some("AAA" -> Tag("AAA")), None, Some(3))
    )

    protectDiffSeq(origValues, updated, expected)
  }

  property("diffSeq - row removed") = protect {
    val updated = processUpdates(origIndex, TableRowDeleted(Id.next, 2))
    protectDiffSeq(origValues, updated, Seq((Some("CCC" -> Tag("CCC")), None, None)))

  }

  property("diffSeq - multiple rows added") = protect {
    val updated =
      processUpdates(origIndex
        , TableRowAdded(Id.next, "FFF", "FFF")
        , TableRowAdded(Id.next, "GGG", "GGG")
        , TableRowAdded(Id.next, "HHH", "HHH")
        , TableRowAdded(Id.next, "III", "III")
      )

    val expected: DiffResult = Seq(
      (None, Some("FFF" -> Tag("FFF")), Some(5))
      , (None, Some("GGG" -> Tag("GGG")), Some(6))
      , (None, Some("HHH" -> Tag("HHH")), Some(7))
      , (None, Some("III" -> Tag("III")), Some(8))
    )

    protectDiffSeq(origValues, updated, expected)
  }


  property("diffSeq - multiple rows removed") = protect {
    val updated = processUpdates(origIndex
      , TableRowDeleted(Id.next, 0)
      , TableRowDeleted(Id.next, 0)
      , TableRowDeleted(Id.next, 1)
    )

    val expected: DiffResult = Seq(
      (Some("AAA" -> Tag("AAA")), None, None)
      , (Some("BBB" -> Tag("BBB")), None, None)
      , (Some("DDD" -> Tag("DDD")), None, None)
    )

    protectDiffSeq(origValues, updated, expected)
  }

  property("diffSeq - row added and removed") = protect {
    val updated = processUpdates(origIndex
      , TableRowAdded(Id.next, "FFF", "FFF")
      , TableRowDeleted(Id.next, 3)
    )

    val expected: DiffResult = Seq(
      (Some("DDD" -> Tag("DDD")), None, None)
      , (None, Some("FFF" -> Tag("FFF")), Some(4))

    )

    protectDiffSeq(origValues, updated, expected)
  }

  property("diffSeq - row added and moved") = protect {
    val updated = processUpdates(origIndex
      , TableRowAdded(Id.next, "FFF", "FFF")
      , TableRowMoved(Id.next, 5, 0)
    )

    val expected: DiffResult = Seq(
      (None, Some("FFF" -> Tag("FFF")), Some(0))
    )

    protectDiffSeq(origValues, updated, expected)
  }

  property("diffSeq - multiple rows added and moved") = protect {
    val updated = processUpdates(origIndex
      , TableRowAdded(Id.next, "FFF", "FFF")
      , TableRowMoved(Id.next, 5, 0)
      , TableRowAdded(Id.next, "GGG", "GGG")
      , TableRowMoved(Id.next, 6, 0)
      , TableRowAdded(Id.next, "HHH", "HHH")
      , TableRowMoved(Id.next, 7, 0)
    )

    val expected: DiffResult = Seq(
      (None, Some("HHH" -> Tag("HHH")), Some(0))
      , (None, Some("GGG" -> Tag("GGG")), Some(1))
      , (None, Some("FFF" -> Tag("FFF")), Some(2))
    )

    protectDiffSeq(origValues, updated, expected)

  }

  property("diffSeq - add move edit and delete") = protect {
    val updated = processUpdates(origIndex
      , TableRowAdded(Id.next, "FFF", "FFF")
      , TableRowMoved(Id.next, 5, 0)
      , TableRowEdited(Id.next, (2, "zZZ", "zZz"))
      , TableRowDeleted(Id.next, 1)
    )

    val expected: DiffResult = Seq(
      (Some("AAA" -> Tag("AAA")), None, None)
      , (None, Some("FFF" -> Tag("FFF")), Some(0))
      , (Some("CCC" -> Tag("CCC")), Some("zZZ" -> Tag("zZz")), None)
    )

    protectDiffSeq(origValues, updated, expected)
  }
}

