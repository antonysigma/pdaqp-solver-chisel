import chisel3._
import chisel3.util._

/** Fully unrolled dot product and threshold operation.
  *
  * @note
  *   This is more expressive to design the dot product with high level DSLs, e.g. HeteroCL, to explore the tradeoff
  *   among DSP, latency, and pipeline overhead.
  */
class DotLEqOffsetSInt16(
    val Q: Int,
    val nHalfspaces: Int,
    val nParameter: Int,
    val normalConsts: Seq[Int],
    val offsetConsts: Seq[Int]
) extends Module {
    require(
        normalConsts.length == nParameter * nHalfspaces,
        s"normalConsts must have length $nParameter * $nHalfspaces"
    )
    require(offsetConsts.length == nHalfspaces, s"offsetConsts must have length $nHalfspaces")

    val addrWidth = log2Ceil(nHalfspaces max 1)
    val io = IO(new Bundle {
        val nodeId = Input(UInt(addrWidth.W))
        val parameters = Input(Vec(nParameter, SInt(16.W)))
        val isRightOf = Output(Bool())
    })

    // Hardcoded values. To be synthsized as ROM
    val normal = VecInit(Seq.tabulate(nHalfspaces) { (n) =>
        VecInit(Seq.tabulate(nParameter) { (p) => normalConsts(n * nParameter + p).S(16.W) })
    })
    val offset = VecInit(offsetConsts.map(_.S(16.W)))

    val prods = Wire(Vec(nParameter, SInt(32.W)))
    for (i <- 0 until nParameter) {
        prods(i) := io.parameters(i) * normal(io.nodeId)(i)
    }

    val guardBits = log2Ceil(nParameter max 1) + 1
    require(Q >= guardBits)

    val prodsTruncated = Wire(Vec(nParameter, SInt(32.W)))
    prodsTruncated := prods.map(_ >> guardBits)

    val sum = Wire(SInt(32.W))
    sum := prodsTruncated.reduceTree(_ + _)

    io.isRightOf := (sum <= (offset(io.nodeId).pad(32) << (Q - guardBits)))
}
