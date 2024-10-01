package jigsaw.peripherals.i2c
import chisel3._
import chisel3.util._

import jigsaw.peripherals.common.{AbstractDevice, AbstractDeviceIO}
import caravan.bus.common.{BusConfig, AbstrRequest, AbstrResponse}

class I2cIO[A <: AbstrRequest, B <: AbstrResponse]
        (gen: A, gen1: B) extends AbstractDeviceIO[A,B]{
    val req = Flipped(Decoupled(gen))   // req aaygi
    val rsp = Decoupled(gen1)           // resp jaayga
    val cio_i2c_sda_in = Input(Bool())

    val cio_i2c_sda = Output(Bool())
    val cio_i2c_scl = Output(Bool())
    val cio_i2c_intr = Output(Bool())
}

class I2c[A <: AbstrRequest, B <: AbstrResponse]
        (gen:A, gen1:B) extends AbstractDevice[A,B] {
    val io = IO(new I2cIO(gen, gen1))

    io.req.ready := 1.B

    val i2c_top = Module (new I2C_Top)

 
    val write_register, read_register  = Wire(Bool())
    val data_reg = Wire(UInt(32.W))
    val addr_reg = Wire(UInt(8.W))

    write_register := Mux(io.req.fire(), io.req.bits.isWrite, false.B)
    read_register := Mux(io.req.fire(), !io.req.bits.isWrite, false.B)
    data_reg := io.req.bits.dataRequest
    addr_reg := io.req.bits.addrRequest(6,0)
    i2c_top.io.wdata := data_reg
    i2c_top.io.addr := addr_reg
    i2c_top.io.we := write_register
    i2c_top.io.ren := read_register

    io.rsp.bits.dataResponse := RegNext(Mux(io.rsp.ready , i2c_top.io.wdata , 0.U))
    io.rsp.valid := RegNext(Mux(write_register || read_register, true.B, false.B))
    io.rsp.bits.error := RegNext(Mux(io.rsp.ready , i2c_top.io.intr , 0.U))

    i2c_top.io.sda_in := io.cio_i2c_sda_in

    io.cio_i2c_sda := i2c_top.io.sda
    io.cio_i2c_scl := i2c_top.io.scl
    io.cio_i2c_intr := i2c_top.io.intr
}