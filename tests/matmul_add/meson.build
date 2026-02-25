vmatmul_add_bin = custom_target('MatMultiplyAddSInt16',
    input: pdaqp_design_sv['MatMultiplyAddSInt16'],
    output: is_verilator_v5 ? [
        'VMatMultiplyAddSInt16__ALL.cpp',
        'VMatMultiplyAddSInt16.h',
    ] : [
        'VMatMultiplyAddSInt16.cpp',
        'VMatMultiplyAddSInt16__Syms.cpp',
        'VMatMultiplyAddSInt16.h',
    ],
    command: verilator_build_cmd + [
        '-I@0@'.format(chisel_lang_build_dir),
    ],
)

pdaqp_apply_feedback_dep = pdaqp_proj.get_variable('apply_feedback_dep')

matmul_add_input_byte_count = 6
test_matmul_add_exe = executable('test-runner',
    sources: [
        'test-matmul-add.cpp',
        vmatmul_add_bin,
    ],
    include_directories: [
        pdaqp_problem_def_inc,
    ],
    dependencies: [
        verilator_dep,
        pdaqp_apply_feedback_dep,
    ],
    cpp_args: [
        '-DPDAQP_INPUT=@0@'.format(matmul_add_input_byte_count),
        '-march=native',
        '-fsanitize=fuzzer',
        '-fno-omit-frame-pointer',
    ],
    link_args: [
        '-fsanitize=fuzzer',
        '-fno-omit-frame-pointer',
    ],
)

test_zero_param_exe = executable('test-zero-param',
    sources: [
        'test-zero-param.cpp',
        vmatmul_add_bin,
    ],
    include_directories: [
        pdaqp_problem_def_inc,
    ],
    dependencies: [
        verilator_dep,
        pdaqp_apply_feedback_dep,
        catch2_test_dep,
        fmt_dep,
    ],
)

test('Feedback offset matches',
    test_zero_param_exe,
    args: [
        '-r', 'tap',
    ],
    protocol: 'tap',
    is_parallel: true,
    suite: 'gemv',
)

test('Matrix-vector multiplication and add',
    test_matmul_add_exe,
    args: [
        '-max_len=@0@'.format(matmul_add_input_byte_count),
        '-jobs=' + nprocs,
        '-workers=' + nprocs,
        '-timeout=1',
        '-print_final_stats=1',
        '-runs=@0@'.format(10 * 1000 * 1000),
    ],
    is_parallel: false,
    suite: 'gemv',
    priority: -1,
)

