import chisel3._

class AXI4StreamInput32 extends Bundle {
    val tdata = Input(UInt(32.W))
    val tvalid = Input(Bool())
    val tready = Output(Bool())
}

class UnpackAxis32ToS16Params(val nPorts: Int, val nParameter: Int) extends Module {
    // Elabor-time checks (best place for width/count invariants)
    require(
        nPorts * 32 >= nParameter * 16,
        s"Insufficient bandwidth. Expected $nPorts * 32 >= $nParameter * 16"
    )

    val io = IO(new Bundle {
        val start = Input(Bool())
        val s_axis = Vec(nPorts, new AXI4StreamInput32)
        val params = Output(Vec(nParameter, SInt(16.W)))
        val ready = Output(Bool())
    })

    val armed = RegInit(false.B)
    val tValid = Wire(Vec(nPorts, Bool()))
    tValid := io.s_axis.map(_.tvalid)

    val allValid = tValid.reduceTree(_ && _)
    val doCapture = armed & allValid
    val paramsReg = Reg(Vec(nParameter, SInt(16.W)))

    io.params := paramsReg
    io.ready := !armed

    // Signal bus ready when armed
    for (i <- 0 until nPorts) {
        io.s_axis(i).tready := armed
    }

    when(io.start) {
        armed := true.B
    }

    when(doCapture) {
        // Unpack each 32b word into two SInt(16)
        // Convention: {hi16, lo16} == tdata[31:16], tdata[15:0]
        for (i <- 0 until nPorts) {
            val w = io.s_axis(i).tdata
            if (2 * i < nParameter) {
                paramsReg(2 * i + 0) := w(15, 0).asSInt // lo
            }

            if (2 * i + 1 < nParameter) {
                paramsReg(2 * i + 1) := w(31, 16).asSInt // hi
            }
        }

        armed := false.B
    }
}
