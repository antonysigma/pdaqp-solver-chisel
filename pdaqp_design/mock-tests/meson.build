chisel_targets_treewalk = custom_target('TreeWalkerMockedTop in SystemVerilog',
    input: chisel_inputs + [
        '../src/TreeWalkTestTop.scala',
        '../src/GenVerilogTreeWalkMocked.scala'
    ],
    output: 'TreeWalkTestTop.sv',
    env: {
      'MESON_SOURCE_ROOT': meson.project_source_root(),
    },
    command: [
        run_at_src_root_exe,
        mill_exe,
        '--no-server',
        meson.project_name() + '.runMain',
        'GenVerilogTreeWalkMocked',
        meson.project_build_root() / '@OUTDIR@',
    ],
)

treewalk_mock_test_dir = meson.current_build_dir()
