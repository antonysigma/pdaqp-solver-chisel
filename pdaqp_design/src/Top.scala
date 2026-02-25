import chisel3._

class Top extends Module {
    val nSolution = Constants.nSolution
    val nParameter = Constants.nParameter
    val nNodes = Constants.nNodes
    val nFeedbacks = Constants.nFeedbacks
    val Q = Constants.Q

    val io = IO(new Bundle {
        val start = Input(Bool())
        val s_axis = Vec((nParameter + 1) / 2, new AXI4StreamInput32)

        val busy = Output(Bool())
        val done = Output(Bool())
        val m_axis = Vec((nSolution + 1) / 2, new AXI4StreamOutput32)
    })

    val unpack = Module(new UnpackAxis32ToS16Params((nParameter + 1) / 2, nParameter))
    val pack = Module(new PackAxis32FromS16Params((nSolution + 1) / 2, nSolution))

    val pdaqpGPIO = Module(new PdaqpGPIO())

    unpack.io.start := io.start
    pdaqpGPIO.io.start := unpack.io.ready
    pdaqpGPIO.io.parameterIn := unpack.io.params
    pack.io.solution := pdaqpGPIO.io.solutionOut

    for (i <- 0 until (nParameter + 1) / 2) {
        unpack.io.s_axis(i).tdata := io.s_axis(i).tdata
        unpack.io.s_axis(i).tvalid := io.s_axis(i).tvalid
        io.s_axis(i).tready := unpack.io.s_axis(i).tready
    }

    for (i <- 0 until (nSolution + 1) / 2) {
        io.m_axis(i).tdata := pack.io.m_axis(i).tdata
        io.m_axis(i).tvalid := pack.io.m_axis(i).tvalid
        pack.io.m_axis(i).tready := io.m_axis(i).tready
    }

    io.busy := pdaqpGPIO.io.busy
    io.done := pdaqpGPIO.io.done
    pack.io.start := pdaqpGPIO.io.done
}
