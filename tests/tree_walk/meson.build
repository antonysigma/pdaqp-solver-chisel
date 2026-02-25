vtreewalk_test_top_bin = custom_target('TreeWalkTestTop Verilator',
    input: chisel_targets_treewalk,
    output: is_verilator_v5 ? [
        'VTreeWalkTestTop__ALL.cpp',
        'VTreeWalkTestTop.h',
    ] : [
        'VTreeWalkTestTop.cpp',
        'VTreeWalkTestTop__Syms.cpp',
        'VTreeWalkTestTop.h',
    ],
    command: verilator_build_cmd + [
        '-I@0@'.format(treewalk_mock_test_dir),
    ],
)

pdaqp_common_inc = pdaqp_proj.get_variable('common_inc')

test_tree_walk_sml_exe = executable('test-tree-walk-sml',
    sources: [
        'test-tree-walk-sml.cpp',
        vtreewalk_test_top_bin,
    ],
    include_directories: [
        pdaqp_problem_def_inc,
        pdaqp_common_inc,
    ],
    dependencies: [
        verilator_dep,
        pdaqp_tree_walker_lut_dep,
        catch2_test_dep,
        fmt_dep,
    ],
)

test('Feedback offset matches',
    test_tree_walk_sml_exe,
    args: [
        '-r', 'tap',
    ],
    protocol: 'tap',
    is_parallel: true,
    suite: 'tree-walk',
)

