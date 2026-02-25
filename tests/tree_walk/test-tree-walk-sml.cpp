#include <fmt/format.h>

#include <algorithm>
#include <array>
#include <cstdint>

//
#include <catch2/catch_test_macros.hpp>

#include "VTreeWalkTestTop.h"
#include "constants.hpp"
#include "problem-def.hpp"
#include "tree_walker.h"

namespace {

constexpr size_t n_tree_nodes = pdaqp_jump_list.size();
static_assert(n_tree_nodes <= 16);

constexpr size_t n_feedbacks = pdaqp_feedbacks.size();
}  // namespace

TEST_CASE("Binary search eventually finish", "[sanity_check]") {
    VTreeWalkTestTop dut;

    const auto tick = [&dut]() {
        dut.clock = false;
        dut.eval();
        dut.clock = true;
        dut.eval();
    };

    dut.io_start               = true;
    dut.io_isRightOfTestVector = 0x00;

    tick();

    REQUIRE(dut.io_busy);
    REQUIRE_FALSE(dut.io_done);

    for (size_t timeout = n_tree_nodes; dut.io_busy && timeout > 0; timeout--) {
        tick();
    }

    REQUIRE(dut.io_done);

    // Send Ack signal
    dut.io_start = false;
    tick();
    REQUIRE_FALSE(dut.io_done);
}

TEST_CASE("Binary search returns valid halfplane id", "[hp_id]") {
    VTreeWalkTestTop dut;

    const auto tick = [&dut]() {
        dut.clock = false;
        dut.eval();
        dut.clock = true;
        dut.eval();
    };

    const auto getHalfplaneID = [&](uint16_t mask) -> uint16_t {
        dut.io_isRightOfTestVector = mask;
        dut.io_start = true;
        tick();

        REQUIRE(dut.io_busy);
        REQUIRE_FALSE(dut.io_done);

        for (size_t timeout = n_tree_nodes; dut.io_busy && timeout > 0; timeout--) {
            tick();
        }

        REQUIRE(dut.io_done);

        // Send Ack signal
        dut.io_start = false;
        tick();
        REQUIRE_FALSE(dut.io_done);

        return dut.io_hpId;
    };

    for (uint16_t mask = 0; mask < n_tree_nodes; mask++) {
        const auto hp_id = getHalfplaneID(mask);
        REQUIRE(hp_id < n_feedbacks);

        INFO(fmt::format("Binary tree trajectory preset = {:#08x}", mask));
        REQUIRE(hp_id == pdaqp_solver::treeWalkerMocked(mask).value);
    }
}
