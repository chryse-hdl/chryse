package ee.hrzn.chryse.chisel

import chisel3._

import scala.language.implicitConversions

private[chryse] object specifiedDirectionOf {
  def apply[T <: Data](data: T): SpecifiedDirection =
    classOf[Data]
      .getMethod("specifiedDirection") // private[chisel3]
      .invoke(data)
      .asInstanceOf[SpecifiedDirection]
}
