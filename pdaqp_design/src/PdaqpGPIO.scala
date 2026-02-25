import chisel3._
import chisel3.util._

class PdaqpGPIO extends Module {
    val nSolution = Constants.nSolution
    val nParameter = Constants.nParameter
    val nNodes = Constants.nNodes
    val nFeedbacks = Constants.nFeedbacks
    val Q = Constants.Q

    val nHalfspaces = nNodes - nFeedbacks

    val io = IO(new Bundle {
        val start = Input(Bool())
        val parameterIn = Input(Vec(nParameter, SInt(16.W)))

        val busy = Output(Bool())
        val done = Output(Bool())
        val solutionOut = Output(Vec(nSolution, SInt(16.W)))
    })

    val dotProduct = Module(
        new DotLEqOffsetSInt16(Q, nHalfspaces, nParameter, Constants.hyperplane_normal, Constants.hyperplane_offset)
    )
    val treeWalk = Module(new TreeWalk(nNodes, nParameter, Constants.jumpList, Constants.hpList))
    val matmul_add = Module(
        new MatMultiplyAddSInt16(Q, nFeedbacks, nSolution, nParameter, Constants.feedbackA, Constants.feedbackC)
    )

    val hpId = RegInit(0.U(log2Ceil(nNodes).W))
    val treeWalkStartReg = RegInit(false.B)

    dotProduct.io.parameters := treeWalk.io.pOut
    dotProduct.io.nodeId := treeWalk.io.hpIdOut
    treeWalk.io.rightIn := dotProduct.io.isRightOf

    treeWalk.io.start := treeWalkStartReg
    treeWalk.io.pIn := io.parameterIn

    matmul_add.io.hpId := treeWalk.io.hpIdOut
    matmul_add.io.p := io.parameterIn
    io.solutionOut := matmul_add.io.y

    // State
    object S extends ChiselEnum { val Idle, BusyWalking, BusyFeedback = Value }
    val state = RegInit(S.Idle)

    io.busy := (state === S.BusyWalking)

    // TODO: Matrix-vector multiplication module can take more than one clock cycle to
    // settle. Implement valid signal from MatMultiplyAdd module
    io.done := (state === S.BusyFeedback)

    switch(state) {
        is(S.Idle) {
            when(io.start) {
                treeWalkStartReg := true.B
                state := S.BusyWalking
            }
        }

        is(S.BusyWalking) {
            when(!treeWalk.io.busy) {
                treeWalkStartReg := false.B
                hpId := treeWalk.io.hpIdOut
                state := S.BusyFeedback
            }
        }

        is(S.BusyFeedback) {
            // Ack signal
            when(!io.start) {
                state := S.Idle
            }
        }
    }
}
