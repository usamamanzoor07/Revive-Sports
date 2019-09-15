package model

import java.util.*

class Match(val matchType:String,
            val matchOver:String,
            val matchGround:String,
            val matchDate:String,
            val matchTime:String,
            val ballType:String,
            val team_A:String,
            val team_B:String,
            val matchUmpire:String,
            val matchId:String)
{
constructor():this("","","","","","","","","","")

//    var current_score:Int
//    var current_over:Int
//    var current_ball:Int
//    var inning_team:Team?=null
//    var bowling_team:Team?=null



    fun startMatch(){}
    fun setInningTeam(t:Team) {}















}