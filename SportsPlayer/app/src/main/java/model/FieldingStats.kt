package model

class FieldingStats(

    //Fielding Stats
    var catches:Int=0,
    var caught_behind:Int=0,
    var runouts:Int=0,
    var stumpings:Int=0,
    var assist_runout:Int=0
) {
    constructor():this(0,0,0,0,0)
}