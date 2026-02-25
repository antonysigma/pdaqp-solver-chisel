import chisel3._
import chisel3.util._

/** Fully unrolled matrix multiply-add operation.
  *
  * Assuming integer underflow/overflow is not a concern here, implement the multiply-reduce-add in one clock cycle.
  *
  * @note
  *   This is only efficient for small matrices, e.g. orientation tracking from 9-DOF IMU. For larger matrix sizes,
  *   implement the systolic array architecture.
  */
class MatMultiplyAddSInt16(
    val Q: Int,
    val nFeedbacks: Int,
    val nSolution: Int,
    val nParameter: Int,
    val AConst: Seq[Int],
    val cVecConst: Seq[Int]
) extends Module {
    require(
        AConst.length == nFeedbacks * nSolution * nParameter,
        s"Matrix A size mismatch: ${AConst.length} != $nFeedbacks * $nSolution * $nParameter"
    )
    require(
        cVecConst.length == nFeedbacks * nSolution,
        s"Matrix A size mismatch: ${cVecConst.length} != $nFeedbacks * $nSolution"
    )

    val addrWidth = log2Ceil(nFeedbacks max 1)
    val io = IO(new Bundle {
        val hpId = Input(UInt(addrWidth.W))
        val p = Input(Vec(nParameter, SInt(16.W)))
        val y = Output(Vec(nSolution, SInt(16.W)))
    })

    // Hardcoded values. To be synthsized as ROM
    val A = VecInit(Seq.tabulate(nFeedbacks) { (n) =>
        VecInit(Seq.tabulate(nParameter * nSolution) { (i) => AConst(n * nParameter * nSolution + i).S(16.W) })
    })
    val cVec = VecInit(Seq.tabulate(nFeedbacks) { (n) =>
        VecInit(Seq.tabulate(nSolution) { (s) => cVecConst(n * nSolution + s).S(16.W) })
    })

    // ---- Fully unrolled combinational datapath ----
    val guardBits = log2Ceil(nParameter max 1) + 1
    require(Q >= guardBits)

    for (i <- 0 until nSolution) {
        val prods = Wire(Vec(nParameter, SInt(32.W)))
        for (j <- 0 until nParameter) {
            prods(j) := A(io.hpId)(nParameter * i + j) * io.p(j)
        }

        val prodsTruncated = Wire(Vec(nParameter, SInt(32.W)))
        prodsTruncated := prods.map(_ >> guardBits)

        val sum = Wire(SInt(32.W))
        sum := prodsTruncated.reduceTree(_ + _)

        val withOffset = Wire(SInt(32.W))
        withOffset := sum + (cVec(io.hpId)(i).pad(32) << (Q - guardBits))

        io.y(i) := withOffset >> ((Q * 2 - guardBits) - (Q - guardBits))
    }
}
