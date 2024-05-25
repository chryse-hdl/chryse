package ee.hrzn.chryse.platform.resource

import chisel3._

class UART extends ResourceBase {
  // TODO (iCE40): lower IO_STANDARD=SB_LVTTL and PULLUP=1.
  // TODO: these will differ per-platform so need to come in from outside.
  val rx = ResourceData(Input(Bool()))
  val tx = ResourceData(Output(Bool()))

  def setName(name: String): Unit = {
    rx.setName(s"${name}_rx")
    tx.setName(s"${name}_tx")
  }

  def onPins(rx: Pin, tx: Pin): this.type = {
    this.rx.onPin(rx)
    this.tx.onPin(tx)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(rx, tx)
}

object UART {
  def apply() = new UART
}
