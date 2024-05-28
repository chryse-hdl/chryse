package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import chisel3.experimental.ExtModule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import java.io.StringWriter

class BlackBoxGeneratorSpec extends AnyFlatSpec with Matchers {
  behavior.of("BlackBoxGenerator")

  it should "generate unary input and output wires correctly" in {
    val sw = new StringWriter
    BlackBoxGenerator(sw, classOf[UnaryBB])
    sw.toString() should be("""attribute \cxxrtl_blackbox 1
                              |attribute \blackbox 1
                              |module \UnaryBB
                              |  attribute \cxxrtl_edge "p"
                              |  wire input 1 \clock
                              |
                              |  wire input 2 \ready
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 3 \valid
                              |end
""".stripMargin)
  }

  it should "expand Vec of Bool correctly" in {
    val sw = new StringWriter
    BlackBoxGenerator(sw, classOf[VecOfBoolBB])
    sw.toString() should be("""attribute \cxxrtl_blackbox 1
                              |attribute \blackbox 1
                              |module \VecOfBoolBB
                              |  wire input 1 \d_in_0
                              |
                              |  wire input 2 \d_in_1
                              |
                              |  wire input 3 \d_in_2
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 4 \d_out_0
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 5 \d_out_1
                              |end
""".stripMargin)
  }

  it should "expand wider elements correctly" in {
    val sw = new StringWriter
    BlackBoxGenerator(sw, classOf[WiderElementsBB])
    sw.toString() should be("""attribute \cxxrtl_blackbox 1
                              |attribute \blackbox 1
                              |module \WiderElementsBB
                              |  wire input 1 width 64 \d_in
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 2 width 8 \d_out_0
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 3 width 8 \d_out_1
                              |end
""".stripMargin)
  }

  it should "expand handle bundles correctly" in {
    println()
    val sw = new StringWriter
    BlackBoxGenerator(sw, classOf[BundleBB])
    sw.toString() should be("""attribute \cxxrtl_blackbox 1
                              |attribute \blackbox 1
                              |module \BundleBB
                              |  attribute \cxxrtl_edge "p"
                              |  wire input 1 \clock
                              |
                              |  attribute \cxxrtl_sync 1
                              |  wire output 2 width 8 \io_tx
                              |
                              |  wire input 3 width 8 \io_rx_bits
                              |
                              |  wire input 4 \io_rx_err
                              |end
""".stripMargin)
  }
}

private class UnaryBB extends ExtModule {
  val clock = IO(Input(Clock()))

  val ready = IO(Input(Bool()))
  val valid = IO(Output(Bool()))
}

private class VecOfBoolBB extends ExtModule {
  val d_in  = IO(Input(Vec(3, Bool())))
  val d_out = IO(Output(Vec(2, Bool())))
}

private class WiderElementsBB extends ExtModule {
  val d_in  = IO(Input(UInt(64.W)))
  val d_out = IO(Output(Vec(2, SInt(8.W))))
}

private class BundleBB extends ExtModule {
  val clock = IO(Input(Clock()))
  val io = IO(new Bundle {
    val tx = Output(UInt(8.W))
    val rx = Flipped(new Bundle {
      val bits = Output(UInt(8.W))
      val err  = Output(Bool())
    })
  })
}
