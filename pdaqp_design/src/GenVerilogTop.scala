import circt.stage.ChiselStage

object GenVerilogTop extends App {
    final val outDir = args.headOption.getOrElse("build-system-verilog")

    ChiselStage.emitSystemVerilogFile(
        gen = new Top(),
        args = Array("--target-dir", outDir),
        //firtoolOpts = Array("--disable-all-randomization")
    )
}
