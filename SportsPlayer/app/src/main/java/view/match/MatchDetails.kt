package view.match

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.database.FirebaseDatabase
import com.pawegio.kandroid.startActivityForResult
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.match_details_layout.*
import model.Match
import model.MatchInvite
import org.jetbrains.anko.*
import view.match.ui.SearchTeamForMatch
import view.team.TeamDetailActivity
import view.team.ui.TeamRequestMatchFragment
import java.text.SimpleDateFormat
import java.util.*

class MatchDetails : AppCompatActivity(), SearchTeamForMatch.OnFragmentInteractionListener {

    override fun onFragmentInteraction(teamId: String,teamLogo:String,teamName:String) {
        Log.d("onFragmentInteraction",teamId)
        Log.d("onFragmentInteraction",teamLogo)

        val intent = Intent()
        intent.putExtra("teamId",teamId)
        intent.putExtra("teamLogo",teamLogo)
        intent.putExtra("teamName",teamName)
        setResult(Activity.RESULT_OK, intent)


    }

    lateinit var matchType:String
    lateinit var ballType:String
    lateinit var team_B_id:String
    lateinit var team_B_Logo:String
    lateinit var team_B_Name:String
    lateinit var newRequestId:String
    var databaseRef: FirebaseDatabase?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.match_details_layout)
        supportActionBar?.title="Match Details"
        //[Team id fields initialization]

        team_B_id=""

        databaseRef= FirebaseDatabase.getInstance()


        //Click Listener for Team_A and Team_B
      //  team_A_StartMatchActivity.setOnClickListener { selectTeamA() }
        team_B_Match_Details.setOnClickListener { startActivityForResult<SelectTeamActivity>(team_B) }

        //RadioGroup Click Listener
        matchType_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            matchType=radio.text.toString()
            when(matchType){
                "Test"->{ matchOvers_Match_Details.visibility= View.GONE }
                "Limited Over"->{matchOvers_Match_Details.visibility=View.VISIBLE}
            }
            toast("Match Type: $matchType")
        }
        ballType_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            ballType=radio.text.toString()
            toast("Ball Type: $ballType")

        }

        //matchTime Click and FocusChange Listener
        matchTime_Match_Details.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
            { setMatchTime() }
        }
        matchTime_Match_Details.setOnClickListener {
            setMatchTime()
        }
       //matchDate Click and FocusChange Listener
        matchDate_Match_Details.setOnClickListener{
            setMatchDate()
        }
    matchDate_Match_Details.setOnFocusChangeListener { _, hasFocus ->
        if(hasFocus)
        {setMatchDate()}
    }

    //saveMatch Button Click Listener
        send_challenge_request_button_match_details.setOnClickListener {
            sendRequestForMatch()
        }


    }


    override fun onResume() {
        super.onResume()

        val teamData=intent.extras
        if(teamData!==null)
        {
            val teamId=teamData.getString("teamId")
            val teamLogo=teamData.getString("teamLogo")
            Log.d("Select Team Activity",teamId)
        }else{
            Log.d("Select Team Activity","  NULL")
        }


    }



    private fun setMatchDate()
    {
        val cal = Calendar.getInstance()
        // cal.add(Calendar.YEAR)
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            matchDate_Match_Details.setText(sdf.format(cal.time))
        }

        DatePickerDialog(
            this, dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }


    private fun setMatchTime()
    {
       val matchHour=0
        val matchMinute=0
        val timePicker = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minutes ->
                matchTime_Match_Details.setText("$hourOfDay : $minutes")
            },matchHour,matchMinute,false)
        timePicker.show()

    }

private fun sendRequestForMatch() {

    val team_A_id = intent.getStringExtra("teamId")
    val team_A_Logo=intent.getStringExtra("teamLogo")
    val team_A_Name=intent.getStringExtra("teamName")
    Log.d("team",team_A_id)
    val overs = matchOvers_Match_Details.text.toString().trim()
    val city = matchCity_Match_Details.text.toString().trim()
    val venue = matchVenue_Match_Details.text.toString().trim()
    val date = matchDate_Match_Details.text.toString().trim()
    val time = matchTime_Match_Details.text.toString().trim()
    val squad = squad_count_Match_Details.text.toString().trim()
    if (overs.isNotEmpty()
        && city.isNotEmpty()
        && venue.isNotEmpty()
        && date.isNotEmpty()
        && time.isNotEmpty()
        && squad.isNotEmpty()
        && matchType.isNotEmpty()
        && ballType.isNotEmpty()
        && team_A_id.isNotEmpty()
        && team_B_id.isNotEmpty()
    ) {
        val newDatabaseReference=databaseRef?.reference

        //generate unique id for match
        val requestId=newDatabaseReference?.push()?.key!!
        Log.d("requestId ",requestId)
        newRequestId=requestId

        val newMatchInvite=MatchInvite(matchType,overs,city,venue,date,time,ballType,team_A_id,team_B_id,squad,requestId)

        Log.d("Team_A_Id ",team_A_id)
        Log.d("team_B_Id ",team_B_id)
        val addRequest=HashMap<String,Any>()
        addRequest["/MatchInvite/$requestId"]=newMatchInvite
        addRequest["/TeamsMatchInvite/$team_A_id/$requestId"]=true
        addRequest["/TeamsMatchInvite/$team_B_id/$requestId"]=true



        newDatabaseReference.updateChildren(addRequest).addOnCompleteListener { task->
            if(task.isSuccessful){
                Log.d("MatchSaved ",requestId)
                toast("Request Sent")
                //progressDialog.dismiss()
                startActivity<TossActivity>("teamA_Id" to team_A_id,
                    "teamB_Id" to team_B_id,
                    "teamALogo" to team_A_Logo,
                    "teamBLogo" to team_B_Logo,
                    "teamAName" to team_A_Name,
                    "teamBName" to team_B_Name,
                    "newRequestId" to newRequestId)
            }
        }.addOnFailureListener { exception ->
            toast(exception.localizedMessage.toString())
            //progressDialog.dismiss()

        }

        startActivity<TeamDetailActivity>()



    } else {
        alert {
            title="Missing Field"
            message="Please Fill All Provided Fields"
            okButton {
                dialog ->
                dialog.dismiss()
            }
        }.show()
         }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_CANCELED)
        {Log.d("resultCode","canceled")}
        if (data != null && resultCode == Activity.RESULT_OK)
        {
            when(requestCode)
            {
                team_B->{
                    val team2_id = data.getStringExtra("teamId")
                    val team2_logo = data.getStringExtra("teamLogo")
                    val team2_Name = data.getStringExtra("teamName")
                    Log.d("MatchDetails_Team_B",team2_id)
                    if (team2_id.isNotEmpty() && team2_logo.isNotEmpty() && team2_Name.isNotEmpty() )
                    {
                        team_B_id=team2_id
                        team_B_Logo=team2_logo
                        team_B_Name=team2_Name
                        Picasso.get().load(team2_logo).into(team_B_Match_Details)
                        selected_Team_B_Name_Match_Details.text=team2_Name
                    }
                }
            }
        }
    }






    companion object{
        const val team_B=2
    }

}
