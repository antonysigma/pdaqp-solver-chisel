import chisel3._
import chisel3.util._

class TreeWalkTestTop extends Module {
    val nSolution = Constants.nSolution
    val nParameter = Constants.nParameter
    val nNodes = Constants.nNodes
    val nFeedbacks = Constants.nFeedbacks
    val Q = Constants.Q

    val nHalfspaces = nNodes - nFeedbacks

    val io = IO(new Bundle {
        val start = Input(Bool())
        val isRightOfTestVector = Input(UInt(16.W))

        val busy = Output(Bool())
        val done = Output(Bool())
        val hpId = Output(UInt(log2Ceil(nNodes).W))
    })

    val treeWalk = Module(new TreeWalk(nNodes, nParameter, Constants.jumpList, Constants.hpList))
    val isRightOfShiftReg = RegInit(0.U(16.W))
    val treeWalkStartReg = RegInit(false.B)

    treeWalk.io.rightIn := isRightOfShiftReg & 1.U(1.W)
    treeWalk.io.start := io.start
    io.hpId := treeWalk.io.hpIdOut

    for (i <- 0 until nParameter) {
        treeWalk.io.pIn(i) := 0.S(16.W)
    }
    object S extends ChiselEnum { val Idle, BusyWalking, Ready = Value }
    val state = RegInit(S.Idle)

    io.busy := (state === S.BusyWalking)
    io.done := (state === S.Ready)

    switch(state) {
        is(S.Idle) {
            when(io.start) {
                treeWalkStartReg := true.B
                isRightOfShiftReg := io.isRightOfTestVector
                state := S.BusyWalking
            }
        }

        is(S.BusyWalking) {
            when(!treeWalk.io.busy) {
                treeWalkStartReg := false.B
                state := S.Ready
            }

            when(treeWalk.io.busy) {
                isRightOfShiftReg := isRightOfShiftReg >> 1
            }
        }

        is(S.Ready) {
            // Ack signal
            when(!io.start) {
                state := S.Idle
            }
        }
    }
}
