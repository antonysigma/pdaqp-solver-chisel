#include <fmt/format.h>

#include <algorithm>
#include <cstdint>
#include <optional>
#include <tuple>

//
#include <catch2/catch_test_macros.hpp>

#include "VMatMultiplyAddSInt16.h"
#include "apply_feedback.h"
#include "constants.hpp"
#include "problem-def.hpp"

namespace {
using Decimal = Solution::value_type;
Solution
runDUT(const FeedbackID hp_id, const Parameter& parameter) {
    Solution solution;

    static VMatMultiplyAddSInt16 dut;

    dut.io_hpId = hp_id.value;
    dut.io_p_0 = parameter.data[0].data;
    dut.io_p_1 = parameter.data[1].data;

    dut.eval();

    static_assert(solution.data.size() == 3);

    std::tie(solution.data[0], solution.data[1], solution.data[2]) = std::make_tuple(
        Decimal{static_cast<int16_t>(dut.io_y_0)}, Decimal{static_cast<int16_t>(dut.io_y_1)},
        Decimal{static_cast<int16_t>(dut.io_y_2)});
    return solution;
}

}  // namespace

namespace Catch {

template <typename T, size_t Q>
struct StringMaker<math::fixed<T, Q>> {
    static std::string convert(const math::fixed<T, Q>& value) {
        return fmt::format("{:g} ({:#04x})", static_cast<float>(value), value.data);
    }
};

}  // namespace Catch

TEST_CASE("Offsets in applyFeedback<hp_id>() must be identical", "[zero_param]") {
    static_assert(pdaqp_feedbacks.size() % ((n_parameter + 1) * n_solution) == 0);
    constexpr size_t n_feedbacks = pdaqp_feedbacks.size() / (n_parameter + 1) / n_solution;

    constexpr Parameter p{};
    for (FeedbackID hp_id{}; hp_id.value < n_feedbacks; hp_id.value++) {
        const auto measured = runDUT(hp_id, p);
        const auto expected = pdaqp_solver::applyFeedback<true>(hp_id, p);

        INFO("hp_id = " << hp_id.value);
        CHECK(measured.data == expected.data);
    }
}

TEST_CASE("Parameter = (1, 0)", "[one_zero_param]") {
    static_assert(pdaqp_feedbacks.size() % ((n_parameter + 1) * n_solution) == 0);
    constexpr size_t n_feedbacks = pdaqp_feedbacks.size() / (n_parameter + 1) / n_solution;

    using math::operator""_q214;
    constexpr Parameter p{{1.0_q214, 0.0_q214}};
    static_assert(p.data.size() == 2);

    for (FeedbackID hp_id{}; hp_id.value < n_feedbacks; hp_id.value++) {
        const auto measured = runDUT(hp_id, p);
        const auto expected = pdaqp_solver::applyFeedback<true>(hp_id, p);

        INFO("hp_id = " << hp_id.value);
        CHECK(measured.data == expected.data);
    }
}

TEST_CASE("Parameter = (0, 1)", "[zero_one_param]") {
    static_assert(pdaqp_feedbacks.size() % ((n_parameter + 1) * n_solution) == 0);
    constexpr size_t n_feedbacks = pdaqp_feedbacks.size() / (n_parameter + 1) / n_solution;

    using math::operator""_q214;
    constexpr Parameter p{{0.0_q214, 1.0_q214}};
    static_assert(p.data.size() == 2);

    for (FeedbackID hp_id{}; hp_id.value < n_feedbacks; hp_id.value++) {
        const auto measured = runDUT(hp_id, p);
        const auto expected = pdaqp_solver::applyFeedback<true>(hp_id, p);

        INFO("hp_id = " << hp_id.value);
        CHECK(measured.data == expected.data);
    }
}
