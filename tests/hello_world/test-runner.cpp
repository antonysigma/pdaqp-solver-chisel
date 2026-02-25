#include <verilated.h>
#include "Vour.h"

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);

    Vour top;

    while (!Verilated::gotFinish()) {
        top.eval();
    }

    top.final();
    return 0;
}