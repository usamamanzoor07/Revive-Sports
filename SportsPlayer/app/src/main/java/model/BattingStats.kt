package model

data class BattingStats (

    //Batting Stats
    var matches:Int=0,
    var innings:Int=0,
    var not_out:Int=0,
    var runs:Int=0,
    var ball_played:Int=0,
    var highest_runs:Int=0,
    var batting_avg:Int=0,
    var batting_strike_rate:Int=0,
    var thirtees:Int=0,
    var fifties:Int=0,
    var hundred:Int=0,
    var num_four:Int=0,
    var num_six:Int=0,
    var ducks:Int=0,
    var match_won:Int=0,
    var match_loss:Int=0
)
{
    constructor():this(0,0,0,0,0,0)
}