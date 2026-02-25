object Constants {
    // BEGIN compile-time constants (already quantized)
    final val nSolution = 3
    final val nParameter = 2
    final val nNodes = 5
    final val nFeedbacks = 3
    final val Q = 14

    final val hyperplane_normal = Seq(
        8409,
        14060,
        11585,
        -11585
    )

    final val hyperplane_offset = Seq(
        21024,
        -12228
    )

    final val feedbackA = Seq(
        2304, -2304, -85, 85, -2218, 2218, 3276, -677, 3276, 5706, 0, 5928, 0, 0, 1560, -1560, -1560, 1560
    )

    final val feedbackC = Seq(
        2432, 8405, 5546, 0, 0, 0, 0, 10142, 6241
    )

    final val hpList = Seq(
        1, 0, 2, 1, 0
    )

    final val jumpList = Seq(
        1, 2, 0, 0, 0
    )
    // END compile-time constants (already quantized)
}
