vdot_leq_bin = custom_target('DotLEqOffsetSInt16',
    input: pdaqp_design_sv['DotLEqOffsetSInt16'],
    output: is_verilator_v5 ? [
        'VDotLEqOffsetSInt16__ALL.cpp',
        'VDotLEqOffsetSInt16.h',
    ] : [
        'VDotLEqOffsetSInt16.cpp',
        'VDotLEqOffsetSInt16__Syms.cpp',
        'VDotLEqOffsetSInt16.h',
    ],
    command: verilator_build_cmd + [
        '-I@0@'.format(chisel_lang_build_dir),
    ],
)

pdaqp_tree_walker_lut_dep = pdaqp_proj.get_variable('tree_walker_lut_dep')
pdaqp_common_inc = pdaqp_proj.get_variable('common_inc')
pdaqp_problem_def_inc = pdaqp_proj.get_variable('problem_def_inc')

cxx = meson.get_compiler('cpp')
cxx.has_argument('-fsanitize=fuzzer')

dot_leq_input_byte_count = 6
test_dot_leq = executable('test-runner',
    sources: [
        'test-dot-leq.cpp',
        vdot_leq_bin,
    ],
    include_directories: [
        pdaqp_common_inc,
        pdaqp_problem_def_inc,
    ],
    dependencies: [
        verilator_dep,
        pdaqp_tree_walker_lut_dep,
    ],
    cpp_args: [
        '-DPDAQP_DOT_LEQ_INPUT=@0@'.format(dot_leq_input_byte_count),
        '-march=native',
        '-fsanitize=fuzzer',
        '-fno-omit-frame-pointer',
    ],
    link_args: [
        '-fsanitize=fuzzer',
        '-fno-omit-frame-pointer',
    ],
)

nprocs = run_command('nproc', check: true).stdout().strip()

test('Dot product and less-than-and-equal',
    test_dot_leq,
    args: [
        '-max_len=@0@'.format(dot_leq_input_byte_count),
        '-jobs=' + nprocs,
        '-workers=' + nprocs,
        '-timeout=1',
        '-print_final_stats=1',
        '-runs=@0@'.format(10 * 1000 * 1000),
    ],
    is_parallel: false,
    priority: -1,
)
