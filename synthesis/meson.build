synlig_exe = subproject('synlig').get_variable('synlig_exe')

pdaqp_sv = ''
foreach f : chisel_outputs
    pdaqp_sv += ' @0@'.format(chisel_lang_build_dir / f)
endforeach

synlig_conf = configure_file(
    input: 'synth.ys.in',
    output: 'synth.ys',
    configuration: {
        'TOP': 'PdaqpGPIO',
        'pdaqp_sv': pdaqp_sv,
    },
)

synlig_run = run_target('synlig',
    command: [
        synlig_exe,
        '-s',
        synlig_conf,
    ],
    depends: chisel_targets,
)

alias_target('yosys', synlig_run)
