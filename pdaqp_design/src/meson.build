chisel_files = [
    'DotLEqOffsetSInt16',
    'MatMultiplyAddSInt16',
    'PdaqpGPIO',
    'TreeWalk',
    #'Pack',
    #'Unpack',
    #'Top',
]

chisel_inputs = []
chisel_outputs = []
index = []
foreach f : chisel_files
    chisel_inputs += files('@0@.scala'.format(f))[0]
    chisel_outputs += '@0@.sv'.format(f)
    index += (index.length() == 0) ? 0 : index[index.length() - 1] + 1
endforeach
chisel_inputs += files(
    'Constants.scala',
    'GenVerilogTop.scala',
)

chisel_targets = custom_target('PdaqpGPIO in SystemVerilog',
    input: chisel_inputs,
    output: chisel_outputs,
    env: {
      'MESON_SOURCE_ROOT': meson.project_source_root(),
    },
    command: [
        run_at_src_root_exe,
        mill_exe,
        '--no-server',
        meson.project_name() + '.runMain',
        'GenVerilogTop',
        meson.project_build_root() / '@OUTDIR@',
    ],
)

chisel_lang_build_dir = meson.current_build_dir()

pdaqp_design_sv = {}
foreach i : index
  pdaqp_design_sv += {chisel_files[i]: chisel_targets[i]}
endforeach

# Auto-format
run_target(
  'format',
  env: {
    'MESON_SOURCE_ROOT': meson.project_source_root(),
  },
  command: [
      run_at_src_root_exe,
      mill_exe,
      '--no-server',
      'mill.scalalib.scalafmt/reformatAll',
  ],
)

# Linter
run_target(
  'lint',
  env: {
    'MESON_SOURCE_ROOT': meson.project_source_root(),
  },
  command: [
      run_at_src_root_exe,
      mill_exe,
      '--no-server',
      'pdaqp_design.fix',
  ],
)
