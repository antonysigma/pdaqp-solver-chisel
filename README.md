# Hardware accelerated PDA-QP solver in Chisel HDL

This project explores how domain-specific languages (DSLs) can be used to
express optimization solvers in hardware while preserving the design intent of
the original CVX problem formulation.

The implementation combines several DSLs at different abstraction layers:

- Chisel for hardware description
- Meson build system for reproducible builds and automated verification
- Python nested `dataclass` structures (aka lightweight objects) for algorithm design lowering to parameterized hardware blocks.
- (Outside this repo) CVXPyGen for solver generation
- (Outside this repo) `python-control` control system modelling tool and constrained QP problem synthesis.

This repository is primarily an exploratory prototype intended to study the
interaction between optimization DSLs and hardware DSLs.

In particular, the goal is to evaluate whether restrictive, declarative DSLs can
improve clarity and maintainability when mapping mathematical formulations to
hardware implementations.

## Design Philosophy

A key hypothesis explored in this repository is that:

> More restrictive DSL grammars can lead to clearer expression of domain logic.

Languages such as Chisel intentionally limit certain forms of general-purpose
programming in order to maintain strong correspondence between code structure
and generated hardware.

Similarly, CVX problem descriptions are declarative, and the hardware
descriptions must ultimately map to static circuits

By combining DSLs across these layers, it becomes easier to reason about how the
mathematical formulation of a convex optimization problem translates into
hardware structure.

This approach contrasts with workflows where general-purpose scripts (TCL, Bash,
or Python) generate and validates low-level RTL directly. Such approaches can be
powerful, but they may obscure the relationship between the optimization model
and the generated hardware.

This repository therefore experiments with a "less is more" design philosophy,
where constrained DSLs help preserve the structure and intent of the original
problem specification.

The implementation should be viewed as one possible exploration among several
viable toolchain designs, including the alternative approaches discussed in
[cvxpygen PR #94](https://github.com/cvxgrp/cvxpygen/pull/94).

## Hardware block diagram

```
                  ┌────────────────────────────────────────────────┐
                  │    ┌───────────────────────┐                   │
                  │    │  TreeWalk             │                   │
io.start─────────►│    │───────────────────────│                   │
io.parameterIn───►│    │             start◄────│───treeWalkStartReg│
                  │    │ hp_list     pIn◄──────│───io.parameterIn  │
                  │    │ jump_list   rightIn◄──┼───────┐           │
                  │    │                       │       │           │
                  │    │             busy─────►│       │           │
                  │    │             done─────►│       │           │
                  │    │                       │       │           │
                  │    │             hpIdOut───│─┬─────┼───┐       │
                  │    │             pOut──────│─┼──┐  │   │       │
                  │    └───────────────────────┘ │  │  │   │       │
                  │  ┌───────────────────────────┴─┐│  │   │       │
                  │  │ DotLEqOffsetSInt16        │ ││  │   │       │
                  │  └─────────────────────────────┘│  │   │       │
                  │  │ Hyperplanes   nodeId    ◄─┘ ││  │   │       │
                  │  │ Bias          parameters◄───│┘  │   │       │
                  │  │               isRightOf─────│───┘   │       │
                  │  └─────────────────────────────┘       │       │
                  │  ┌────────────────────────┐            │       │
                  │  │ MatMultiplyAddSInt16   │            │       │
                  │  └────────────────────────┘            │       │
                  │  │   MatrixA     hpId◄────│────────────┘       │
                  │  │   OffsetB     p◄───────│────io.parameterIn  │
                  │  │               y────────┼───────────────────►│ io.solutionOut
                  │  └────────────────────────┘               ┌───►│ io.busy
                  │                                           │ ┌─►│ io.done
                  │  State transition logic:                  │ │  │
                  │     Idle -> BusyWalking -> BusyFeedback   │ │  │
                  │                                           │ │  │
                  │   io.busy = (state == BusyWalking)────────┘ │  │
                  │   io.done = (state == BusyFeedback)─────────┘  │
                  └────────────────────────────────────────────────┘
```

## Non-Goals

This repository intentionally avoids several aspects of production-grade
hardware design. The focus is conceptual exploration rather than optimized
deployment.

### Hardware-in-the-loop testing

Hardware-in-the-loop (HIL) testing is not included. Instead, design verification
and signal validation currently focuses on functional equivalence tests, and
randomized tests. The latter also known as Monte Carlo / fuzzing against the
software reference implementation (aka the gold copy).

These tests are executed through the automated build workflow in Meson DSL.

### Timing-optimized MAC pipelines

The matrix-vector operations in the current implementation are pure
combinational circuits.

While this simplifies the design and helps illustrate the mapping between the
solver formulation and the generated hardware, it is not suitable for
high-performance hardware.

Production implementations should include:

- pipelined MAC stages;
- register buffering in deep arithmetic paths;
- timing closure strategies for high clock frequencies;
- fanout and signal brownout analysis.

### Hardware-aware MAC unrolling

The current implementation does not optimize arithmetic units for specific FPGA
resources. In particular, professional implementations would typically account
for:

- DSP slice availability (e.g. Xilinx DSP48 blocks)
- optimal MAC unrolling factors
- resource-aware scheduling

This repository does not attempt such optimizations.

### Direct sensor integration

Interfacing with external sensors (for example I²C-controlled 9-DOF IMUs) is
outside the scope of this project.

I expect users to integrate sensor acquisition and (de-)multiplexing separately,
connecting the larger hardware design to the solver module as input signals.

### AXI-based data transfer interfaces

Control parameter packing and unpacking via AXI interfaces is not implemented.

In production, users are expected to implement the required interface logic in
Chisel. For example, integration of the RTOS event loop from the programmable
system (PS) and the FPGA-accelerated solver module in the programmable logic
(PL) through memory mapping.

This is particularly relevant for systems based on devices such as Xilinx Zynq
featuring the Accelerator Coherence Port (ACP).