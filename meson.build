project(
    'pdaqp_design',
    'cpp',
    default_options: [
        'cpp_std=c++20',
        'buildtype=debugoptimized',
    ],
)

verilator_exe = find_program('verilator', required: true)
verilator_ver = run_command(verilator_exe, '--version', check: true).stdout().strip().split()[1]
is_verilator_v5 = verilator_ver.version_compare('>=5.0')
message('Detected Verilator version: ' + verilator_ver)

verilator_inc = include_directories('/usr/share/verilator/include')
verilator_lib = static_library(
    'verilator',
    sources: [
        '/usr/share/verilator/include/verilated_cov.cpp',
        '/usr/share/verilator/include/verilated.cpp',
        #'/usr/share/verilator/include/verilated_dpi.cpp',
        '/usr/share/verilator/include/verilated_fst_c.cpp',
        '/usr/share/verilator/include/verilated_save.cpp',
        '/usr/share/verilator/include/verilated_threads.cpp',
        '/usr/share/verilator/include/verilated_vcd_c.cpp',
        '/usr/share/verilator/include/verilated_vcd_sc.cpp',
        '/usr/share/verilator/include/verilated_vpi.cpp',
    ],
    include_directories: verilator_inc,
    dependencies: [
        dependency('threads'),
    ],
)

verilator_dep = declare_dependency(
    link_with: verilator_lib,
    include_directories: verilator_inc,
)

subdir('scripts')
mill_exe = subproject('mill-build-system').get_variable('mill_exe')
subdir('pdaqp_design/src')
subdir('pdaqp_design/mock-tests')
subdir('tests/hello_world')

pdaqp_proj = subproject(
    'pdaqp-solver-cpp',
    default_options: [
        'name=pid',
    ],
)

catch2_test_dep = subproject('catch2').get_variable('catch2_with_main_dep')

fmt_dep = subproject('fmt').get_variable('fmt_dep')
subdir('tests/dot_product')
subdir('tests/matmul_add')
subdir('tests/tree_walk')

libffi_lib = dependency('libffi', version: '>=3.5', required: false)

if libffi_lib.found()
    subdir('synthesis')
endif