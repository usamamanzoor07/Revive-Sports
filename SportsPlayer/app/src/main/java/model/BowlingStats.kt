package model

class BowlingStats(

    //Bowling Stats
    var overs_bowled:Int=0,
    var maiden:Int=0,
    var wicket:Int=0,
    var runs_concede:Int=0,
    var best_bowling_figures:Int=0,
    var economy:Int=0,
    var bowling_strike_rate:Int=0,
    var bowling_avg:Int=0,
    var wide:Int=0,
    var no_ball:Int=0,
    var dot_balls:Int=0,
    var fours_conceded:Int=0,
    var sixes_conceded:Int=0)
{
    constructor():this(0,0,0,0,0,0)
}