package model

class SelectedPlayerForMatch(
var score:Int=0,
var ball_played:Int=0,
var num_four:Int=0,
var num_six:Int=0,
var out_cause:String?=null,
var out_status:String?=null,
var over_taken:Int=0,
var wide:Int=0,
var no_ball:Int=0,
var wicket:Int=0
)

{
    constructor() : this(0,0,0,0,"","",0,0,0,0) {}
}