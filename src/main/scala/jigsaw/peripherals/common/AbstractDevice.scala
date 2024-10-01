package jigsaw.peripherals.common

import chisel3._
import chisel3.util._ 

import caravan.bus.common.{AbstrRequest, AbstrResponse}

abstract class AbstractDeviceIO[A <: AbstrRequest, B <: AbstrResponse] extends Bundle{
    val req: DecoupledIO[A]
    val rsp: DecoupledIO[B]
}

abstract class AbstractDevice[A <: AbstrRequest, B <: AbstrResponse] extends Module{
    val io: AbstractDeviceIO[A,B]
}