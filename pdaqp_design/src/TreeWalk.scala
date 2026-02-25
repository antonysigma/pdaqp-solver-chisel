import chisel3._
import chisel3.util._

class TreeWalk(nNodes: Int, nParameter: Int, jumpListConst: Seq[Int], hpListConst: Seq[Int]) extends Module {
    require(jumpListConst.length == nNodes)
    require(hpListConst.length == nNodes)

    val addrWidth = log2Ceil(nNodes max 1)
    val io = IO(new Bundle {
        val start = Input(Bool())
        val pIn = Input(Vec(nParameter, SInt(16.W)))

        val busy = Output(Bool())
        val done = Output(Bool())
        val hpIdOut = Output(UInt(addrWidth.W))

        // Connections to the isRightOf module.
        val pOut = Output(Vec(nParameter, SInt(16.W)))
        val rightIn = Input(Bool()) // isRightOf(hpIdOut, pOut)
    })

    // Look up tables
    val jumpList = VecInit(jumpListConst.map(_.U(addrWidth.W)))
    val hpList = VecInit(hpListConst.map(_.U(addrWidth.W)))

    // State
    object S extends ChiselEnum { val Idle, Run, Done = Value }
    val state = RegInit(S.Idle)

    // Loop registers (16-bit id / next)
    val idReg = RegInit(0.U(addrWidth.W))
    val nextIdReg = RegInit(jumpListConst(0).U(addrWidth.W))

    // Outputs
    io.busy := (state === S.Run)
    io.done := (state === S.Done)

    io.hpIdOut := hpList(idReg)
    io.pOut := io.pIn

    switch(state) {
        is(S.Idle) {
            when(io.start) {
                idReg := 0.U
                nextIdReg := jumpList(0)
                state := S.Run
            }
        }

        is(S.Run) {
            // while (next != id) { ... } else terminate
            when(nextIdReg === idReg) {
                state := S.Done
            }.otherwise {
                val newId = nextIdReg + io.rightIn.asUInt
                idReg := newId
                nextIdReg := newId + jumpList(newId)
            }
        }

        is(S.Done) {
            // simple “ack”: go idle when start deasserts
            when(!io.start) { state := S.Idle }
        }
    }
}
