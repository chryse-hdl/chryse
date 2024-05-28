package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.noPrefix
import ee.hrzn.chryse.chisel.directionOf
import ee.hrzn.chryse.platform.resource.Pin

import scala.collection.mutable
import scala.language.existentials
import scala.language.implicitConversions

trait ChryseTop extends RawModule {
  override def desiredName = "chrysetop"

  case class ConnectedResource(
      pin: Pin,
      frequencyHz: Option[Int],
  )

  object ConnectedResource {
    implicit def pin2Cr(pin: Pin): ConnectedResource =
      ConnectedResource(pin, None)
  }

  protected def platformConnect(
      name: String,
      res: resource.ResourceData[_ <: Data],
  ): Option[(Data, Data)] = None

  protected def platformPort[HW <: Data](
      res: resource.ResourceData[HW],
      topIo: Data,
      portIo: Data,
  ): Unit = {
    directionOf(topIo) match {
      case directionOf.Input =>
        topIo := portIo
      case directionOf.Output =>
        portIo := topIo
    }
  }

  protected def connectResources(
      platform: PlatformBoard[_ <: PlatformBoardResources],
      clock: Option[Clock],
  ): Map[String, ConnectedResource] = {
    val connected = mutable.Map[String, ConnectedResource]()

    for { res <- platform.resources.all } {
      val name = res.name.get
      res match {
        case res: resource.ClockSource =>
          if (res.ioInst.isDefined) {
            throw new Exception(
              "clock sources must be manually handled for now",
            )
          }
          // NOTE: we can't just say clki := platform.resources.clock in our top
          // here, since that'll define an input IO in *this* module which we
          // can't then sink like we would in the resource.Base[_] case.
          connected += name -> ConnectedResource(
            res.pinId.get,
            Some(platform.clockHz),
          )
          clock.get := noPrefix(IO(Input(Clock())).suggestName(name))

        case _ =>
          platformConnect(name, res) match {
            case Some((topIo, portIo)) =>
              connected += name -> res.pinId.get
              platformPort(res, topIo, portIo)
            case None =>
              if (res.ioInst.isDefined) {
                connected += name -> res.pinId.get
                val (topIo, portIo) = res.makeIoConnection()
                platformPort(res, topIo, portIo)
              }
          }
      }
    }

    connected.to(Map)
  }
}
