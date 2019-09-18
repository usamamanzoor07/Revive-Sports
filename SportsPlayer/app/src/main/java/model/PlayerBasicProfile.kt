package model

data class PlayerBasicProfile(
                   //General Profile
                   val profile_img:String?=null,
                   val name:String?="",
                   val phoneNumber:String?="",
                   val playerId:String?="",
                   var gender:String="Male",
                   val dateOfBirth:String?="",
                   val age:Int=20,
                   val city:String?="",
                   val Location:String?="",
                   var playing_role:String?="",
                   var batting_style:String?="",
                   var bowling_style:String?=""
                  )
{
    constructor():this("","","","")

}