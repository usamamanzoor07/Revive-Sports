package model

data class SelectedPlayerForMatch(
    var score: Int =0,
    var ball_played: Int = 0,
    var num_four: Int = 0,
    var num_six: Int = 0,
    var over_taken: Int = 0,
    var wide: Int = 0,
    var no_ball: Int = 0,
    var dot_balls: Int = 0,
    var wicket: Int = 0,
    var catches: Int = 0,
    var caught_behind: Int = 0,
    var runouts: Int = 0,
    var stumpings: Int = 0,
    var out_cause: String? = "",
    var out_status: String? = "",
    var inning: Boolean=false,
    var thirty: Boolean=false,
    var fifty: Boolean=false,
    var hundred: Boolean=false,
    var match_won: Boolean=false
) {
    constructor() : this(
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        "",
        "",
        false,
        false,
        false,
        false,
        false
    ) {
    }
}