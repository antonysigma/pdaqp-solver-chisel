import chisel3._
import chisel3.util._

class AXI4StreamOutput32 extends Bundle {
    val tdata = Output(UInt(32.W))
    val tvalid = Output(Bool())
    val tready = Input(Bool())
}

class PackAxis32FromS16Params(val nPorts: Int, val nSolution: Int) extends Module {
    // Elabor-time checks (best place for width/count invariants)
    require(
        nPorts * 32 >= nSolution * 16,
        s"Insufficient bandwidth. Expected $nPorts * 32 >= $nSolution * 16"
    )

    val io = IO(new Bundle {
        val start = Input(Bool())
        val solution = Input(Vec(nSolution, SInt(16.W)))
        val m_axis = Vec(nPorts, new AXI4StreamOutput32)
    })

    val armed = RegInit(false.B)

    for (i <- 0 until nPorts) {
        val loIdx = 2 * i
        val hiIdx = 2 * i + 1

        val lo16 = if (loIdx < nSolution) io.solution(loIdx).asUInt else 0.U(16.W)
        val hi16 = if (hiIdx < nSolution) io.solution(hiIdx).asUInt else 0.U(16.W)

        io.m_axis(i).tdata := Cat(hi16, lo16)
        io.m_axis(i).tvalid := armed
    }

    when(io.start) {
        armed := true.B
    }

    // Handshake: complete only when ALL ports are ready in same cycle.
    val tReady = Wire(Vec(nPorts, Bool()))
    tReady := io.m_axis.map(_.tready)
    val allReady = tReady.reduceTree(_ && _)
    val doSend = armed && allReady
    when(doSend) {
        armed := false.B
    }

}
