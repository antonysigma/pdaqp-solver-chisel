verilator_build_cmd = (is_verilator_v5 ? [
        verilator_exe,
        '--lib-create', '@BASENAME@',
        '--build',
    ] : [
        verilator_exe,
    ]) + [
        '--cc',
        '--Mdir', '@OUTDIR@',
        #'-I@0@'.format(chisel_lang_build_dir),
        #'-CFLAGS', '-march=native -O3',
        '@INPUT@',
    ]

vour_bin = custom_target('Vour',
    input: 'our.v',
    output: is_verilator_v5 ? [
        'Vour__ALL.cpp',
        'Vour.h',
    ] : [
        'Vour.cpp',
        'Vour__Syms.cpp',
        'Vour.h',
    ],
    command: verilator_build_cmd,
)

test_runner_exe = executable('test-runner',
    sources: [
        'test-runner.cpp',
        vour_bin,
    ],
    dependencies: verilator_dep,
)

test('Hello world',
    test_runner_exe,
)
