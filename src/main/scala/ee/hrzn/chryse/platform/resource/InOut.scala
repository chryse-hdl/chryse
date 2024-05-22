package ee.hrzn.chryse.platform.resource

import chisel3._

// TODO: it's an error to use both "i" and "o" (tristate is a different kettle
// of fish entirely).
class InOut extends ResourceBase with ResourceSinglePin {
  val i = new ResourceData[Bool](Input(Bool())) {}
  val o = new ResourceData[Bool](Output(Bool())) {}

  def setName(name: String): Unit = {
    i.setName(s"$name")
    o.setName(s"$name")
  }

  def onPin(id: Pin): this.type = {
    i.onPin(id)
    o.onPin(id)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(i, o)
}

object InOut {
  def apply() = new InOut
}
