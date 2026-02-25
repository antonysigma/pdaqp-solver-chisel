import circt.stage.ChiselStage

object GenVerilogTreeWalkMocked extends App {
    final val outDir = args.headOption.getOrElse("build-system-verilog")

    ChiselStage.emitSystemVerilogFile(
        gen = new TreeWalkTestTop(),
        args = Array("--target-dir", outDir),
        //firtoolOpts = Array("--disable-all-randomization")
    )
}
