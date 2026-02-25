#include <algorithm>
#include <cstdint>
#include <optional>
#include <tuple>

#include "VMatMultiplyAddSInt16.h"
#include "apply_feedback.h"
#include "constants.hpp"
#include "problem-def.hpp"

namespace {
#pragma pack(push, 1)
struct Decoded {
    FeedbackID hp_id{};
    Parameter parameter{};
};
#pragma pack(pop)

static_assert(sizeof(Decoded) == (PDAQP_INPUT));

inline std::optional<Decoded>
decodeInput(const uint8_t* data, size_t size) {
    if (size < sizeof(Decoded)) {
        return std::nullopt;
    }

    Decoded decoded{};

    std::copy_n(data, sizeof(Decoded), reinterpret_cast<uint8_t*>(&decoded));
    return {decoded};
}

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

template <std::signed_integral T, size_t Q>
constexpr auto
operator-(const math::fixed<T, Q>& a, const math::fixed<T, Q>& b) {
    return math::fixed<T, Q>{static_cast<T>(a.data - b.data)};
}

template <std::signed_integral T, size_t Q>
constexpr math::fixed<T, Q>
abs(const math::fixed<T, Q>& a) {
    return (a.data < 0 ? -a : a);
}

template <Decimal eps>
constexpr bool
approxEqual(const Solution& a, const Solution& b) {
    if constexpr (eps.data == 0) {
        return std::equal(a.data.begin(), a.data.end(), b.data.begin());
    } else {
        for (auto [x, y] = std::make_pair(a.data.begin(), b.data.begin()); x != a.data.end();
            ++x, ++y) {
            if (abs((*x) - (*y)) > eps) [[unlikely]] {
                return false;
            }
        }

        return true;
    }
}

}  // namespace

extern "C" int
LLVMFuzzerTestOneInput(const uint8_t* data, size_t size) {
    const auto has_decoded = decodeInput(data, size);

    if (!has_decoded) {
        return 0;
    }

    auto [hp_id, p] = *has_decoded;
    static_assert(pdaqp_feedbacks.size() % ((n_parameter + 1) * n_solution) == 0);
    constexpr size_t n_feedbacks = pdaqp_feedbacks.size() / (n_parameter + 1) / n_solution;
    hp_id.value = hp_id.value % n_feedbacks;

    const auto measured = runDUT(hp_id, p);
    const auto expected = pdaqp_solver::applyFeedback<true>(std::move(hp_id), std::move(p));

    constexpr auto eps = Decimal{0.0f};
    if (!approxEqual<eps>(measured, expected)) [[unlikely]] {
        // Crash so libFuzzer keeps the input as a reproducer.
        __builtin_trap();
    }
    return 0;
}
