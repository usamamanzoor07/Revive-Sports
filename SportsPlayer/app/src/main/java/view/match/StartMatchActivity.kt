package view.match

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.database.FirebaseDatabase

import com.pawegio.kandroid.startActivityForResult
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_start_match.*
import model.Match
import org.jetbrains.anko.*
import view.team.TeamsPlayerReadyToPlayMatch
import java.text.SimpleDateFormat
import java.util.*

class StartMatchActivity : AppCompatActivity(){

    lateinit var matchType:String
    lateinit var ballType:String
    lateinit var team_A_id:String
    lateinit var team_B_id:String
    lateinit var team_A_Logo:String
    lateinit var team_B_Logo:String
    lateinit var team_A_Name:String
    lateinit var team_B_Name:String
    lateinit var newMatchId:String
    var databaseRef: FirebaseDatabase?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_match)
        supportActionBar?.title="Start Match"

        databaseRef= FirebaseDatabase.getInstance()


        //Click Listener for Team_A and Team_B
        team_A_StartMatchActivity.setOnClickListener {
            startActivityForResult<SelectTeamActivity>(team_A)
        }
        team_B_StartMatchActivity.setOnClickListener {
            startActivityForResult<SelectTeamActivity>(team_B)
        }

        //RadioGroup Click Listener
        matchType_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            matchType=radio.text.toString()
            toast("Match Type: $matchType")
        }
        ballType_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            ballType=radio.text.toString()
            toast("Ball Type: $ballType")

        }

        //matchTime Click and FocusChange Listener
        matchTime_StartMatch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
            { setMatchTime() }
        }
        matchTime_StartMatch.setOnClickListener {
            setMatchTime()
        }
       //matchDate Click and FocusChange Listener
        matchDate_StartMatch.setOnClickListener{
            setMatchDate()
        }
    matchDate_StartMatch.setOnFocusChangeListener { _, hasFocus ->
        if(hasFocus)
        {setMatchDate()}
    }

    //saveMatch Button Click Listener
        saveMatch_StartMatchActivity.setOnClickListener {
            saveMatch()
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
            matchDate_StartMatch.setText(sdf.format(cal.time))
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
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                matchTime_StartMatch.setText("$hourOfDay : $minute")
            },matchHour,matchMinute,false)
        timePicker.show()

    }

private fun saveMatch() {
    val over = matchOver_StartMatch.text.toString().trim()
    val ground = matchGround_StartMatch.text.toString().trim()
    val date = matchDate_StartMatch.text.toString().trim()
    val time = matchTime_StartMatch.text.toString().trim()
    val umpire=matchUmpire_StartMatch.text.toString().trim()
    if (over.isNotEmpty()
        && ground.isNotEmpty()
        && date.isNotEmpty()
        && time.isNotEmpty()
        && umpire.isNotEmpty()
        && matchType.isNotEmpty()
        && ballType.isNotEmpty()
        && team_A_id.isNotEmpty()
        && team_B_id.isNotEmpty()
    ) {
        val newDatabaseReference=databaseRef?.reference

        //generate unique id for match
        val matchId=newDatabaseReference?.push()?.key!!
        Log.d("MatchId ",matchId)
        newMatchId=matchId

        val newMatch=Match(matchType,over,ground,date,time,ballType,team_A_id,team_B_id,umpire,matchId)

        Log.d("Team_A_Id ",team_A_id)
        Log.d("team_B_Id ",team_B_id)
        val startMatch=HashMap<String,Any>()
        startMatch["/Match/$matchId"]=newMatch
        startMatch["/TeamsMatch/$team_A_id/$matchId"]=true
        startMatch["/TeamsMatch/$team_B_id/$matchId"]=true


        newDatabaseReference.updateChildren(startMatch).addOnCompleteListener { task->
            if(task.isSuccessful){
                Log.d("MatchSaved ",matchId)
                toast("Match Saved")
                //progressDialog.dismiss()
                startActivity<TossActivity>("teamA_Id" to team_A_id,
                    "teamB_Id" to team_B_id,
                    "teamALogo" to team_A_Logo,
                    "teamBLogo" to team_B_Logo,
                    "teamAName" to team_A_Name,
                    "teamBName" to team_B_Name,
                    "newMatchId" to newMatchId)
            }
        }.addOnFailureListener { exception ->
            toast(exception.localizedMessage.toString())
            //progressDialog.dismiss()

        }





    } else {
        alert {
            title="Missing Field"
            message="please fill all the provided fields"
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
                team_A->{
                    val team1_id = data.getStringExtra("teamId")
                    val team1_logo = data.getStringExtra("teamLogo")
                    val team1_Name = data.getStringExtra("teamName")
                    Log.d("StartMatchActivity_T1",team1_id)
                    if(team1_id.isNotEmpty() && team1_logo.isNotEmpty() && team1_Name.isNotEmpty())
                    {
                        team_A_id=team1_id
                        team_A_Logo=team1_logo
                        team_A_Name=team1_Name
                        Picasso.get().load(team1_logo).into(team_A_StartMatchActivity)
                        selected_Team_A_Name_StartMatchActivity.text=team1_Name

                    }
                }
                team_B->{
                    val team2_id = data.getStringExtra("teamId")
                    val team2_logo = data.getStringExtra("teamLogo")
                    val team2_Name = data.getStringExtra("teamName")
                    Log.d("StartMatchActivity_T2",team2_id)
                    if (team2_id.isNotEmpty() && team2_logo.isNotEmpty() && team2_Name.isNotEmpty())
                    {
                        team_B_id=team2_id
                        team_B_Logo=team2_logo
                        team_B_Name=team2_Name
                        Picasso.get().load(team2_logo).into(team_B_StartMatchActivity)
                        selected_Team_B_Name_StartMatchActivity.text=team2_Name
                    }
                }
            }
        }
    }






    companion object{
      const  val team_A=1
       const val team_B=2
    }


}
