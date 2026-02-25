#include <cstdint>
#include <optional>
//#include <verilated.h>

//
#include "VDotLEqOffsetSInt16.h"
#include "hyperplane_math.hpp"

namespace {
static_assert(pdaqp_halfplanes.size() % (n_parameter + 1) == 0);
constexpr auto n_hyperplanes = pdaqp_halfplanes.size() / (n_parameter + 1);
constexpr auto hyperplane_fn_list{
    hyperplane::makeHalfspaceList(std::make_index_sequence<n_hyperplanes>{})};

bool runDUT(const uint16_t hp_id, const Parameter& parameter) {
    static VDotLEqOffsetSInt16 dut;

    dut.io_nodeId = hp_id;
    dut.io_parameters_0 = parameter.data[0].data;
    dut.io_parameters_1 = parameter.data[1].data;

    dut.eval();

    return dut.io_isRightOf != 0;
}

#pragma pack(push, 1)
struct Decoded {
    uint16_t hp_id{};
    Parameter parameter{};
};
#pragma pack(pop)

static_assert(sizeof(Decoded) == (PDAQP_DOT_LEQ_INPUT));

inline std::optional<Decoded>
decodeInput(const uint8_t* data, size_t size) {
    if (size < sizeof(Decoded)) {
        return std::nullopt;
    }

    Decoded decoded{};

    std::copy_n(data, sizeof(Decoded), reinterpret_cast<uint8_t*>(&decoded));
    return {decoded};
}
}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, size_t size) {
    const auto has_decoded = decodeInput(data, size);

    if (!has_decoded) {
        return 0;
    }

    auto [hp_id, p] = *has_decoded;
    hp_id = hp_id % n_hyperplanes;

    const bool measured = runDUT(hp_id, p);
    const bool expected = hyperplane_fn_list[hp_id](std::move(p));

    if (expected != measured) [[unlikely]] {
        // Crash so libFuzzer keeps the input as a reproducer.
        __builtin_trap();
    }
    return 0;
}
