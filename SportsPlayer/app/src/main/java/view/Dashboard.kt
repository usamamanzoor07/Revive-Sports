package view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentTransaction
import com.example.sportsplayer.MainActivity
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pawegio.kandroid.visible
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.dashboard_activity.*
import kotlinx.android.synthetic.main.match_card_on_dashboard.view.*
import kotlinx.android.synthetic.main.my_team_list_ondashboard.view.*
import org.jetbrains.anko.startActivity
import view.ProfilePackage.Profile
import view.fragment.SearchTeamFragment
import view.match.StartMatchActivity
import view.matchscoring.MatchScoringActivity
import view.team.TeamDetailActivity

@Suppress("DEPRECATION")
class Dashboard:AppCompatActivity(), SearchTeamFragment.OnFragmentInteractionListener
{

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val CODE_IMAGE_GALLERY=1
    val SAMPLE_CROPPED_IMAGE_NAME="SampleCropImage"
    private var selectedProfileUri: Uri? = null




    private var mAuth:FirebaseAuth?=null
   private lateinit var searchTeamFragment: SearchTeamFragment
    val teamAdapter=GroupAdapter<ViewHolder>()

    val matchAdapter=GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)
        mAuth= FirebaseAuth.getInstance()


        teamAdapter.setOnItemClickListener { item, view ->

            val team=item as MyTeamOnDashboard
            Log.d("Dashboard_TeamName",team.teamName)
            Log.d("Dashboard_TeamCaptain",team.teamCaptain)
            Log.d("Dashboard_TeamCity",team.teamCity)


            startActivity<TeamDetailActivity>(
                "teamId" to team.teamId,
                "teamLogo" to team.teamLogo,
                "teamName" to team.teamName,
                "teamCity" to team.teamCity
                )

        }

            cropedImage.setOnClickListener {
                startActivity<Profile>()
            }

        //retrieve team data from the database
        fetchTeamFromDatabase()

        //retrieve team data from the database
        fetchMatchFromDatabase()

        //get the instance of SearchTeamFragment
        searchTeamFragment= SearchTeamFragment()

        //Listener to check the fragments on the Stack
        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.backStackEntryCount ==0)
            {
                makeViewsVisible(dashboard_layout)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK)

            when(requestCode){

            }

    }

    fun getUserInfo()
    {
        val uid=mAuth?.uid

        val playerRef= FirebaseDatabase.getInstance().getReference("/PlayerBasicProfile/$uid")
        playerRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                val playerImage=p0.child("profile_img").value.toString()
                val playerName=p0.child("name").value.toString()
                val playingRole=p0.child("playing_role").value.toString()
                val playerAge=p0.child("age").value.toString()
                val playerCity=p0.child("Location").value.toString()


                Picasso
                    .get()
                    .load(playerImage)
                    .fit() // use fit() and centerInside() for making it memory efficient.
                    .centerInside()
                    .into(profile_Image_DashboardActivity)
                playerName_DashboardActivity.text=playerName
                playerRole_DashboardActivity.text=playingRole
                playerAge_DashboardActivity.text=playerAge
                playerCity_DashboardActivity.text=playerCity

            }
        })

        val playerRefRuns= FirebaseDatabase.getInstance().getReference("/BattingStats/$uid")
        playerRefRuns.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                val playerRuns=p0.child("runs").value.toString()

                batting_figures_dashboard.text=playerRuns

            }
        })

        val playerRefWickets= FirebaseDatabase.getInstance().getReference("/BowlingStats/$uid")
        playerRefWickets.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val playerWickets=p0.child("wicket").value.toString()

                bowling_figures_dashboard.text=playerWickets

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        val playerRefCatches= FirebaseDatabase.getInstance().getReference("/BattingStats/$uid")
        playerRefCatches.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {


                val playerMatches=p0.child("matches").value.toString()

                matches_figures_Dashboard.text=playerMatches


            }
        })
    }


    private fun calculateAge(){

        val uid=mAuth?.uid

        val playerRef= FirebaseDatabase.getInstance().getReference("/PlayerBasicProfile/$uid")
        playerRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                val playerDob=p0.child("dateOfBirth").value.toString()

                


            }
        })
    }


    fun selectProfileImage() {
        val gallery = Intent(Intent.ACTION_PICK)
        gallery.type = "image/*"
        startActivityForResult(gallery,CODE_IMAGE_GALLERY)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
           val inflater= menuInflater
           inflater.inflate(R.menu.dashboard_menu,menu)
        val menuItem:MenuItem=menu!!.findItem(R.id.actionbar_search)
        val searchView=menuItem.actionView as SearchView
        searchView.setOnSearchClickListener {
            makeViewsInvisible(dashboard_layout)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,searchTeamFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("searchTeamFragment")
                .commit()
        }


        return super.onCreateOptionsMenu(menu)


    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId)
        {
            R.id.profile->startActivity<Profile>()
            R.id.editProfile->{
                startActivity<EditProfile>()
            }//Upcoming Matches Activity
            R.id.startMatch->{
               startActivity<StartMatchActivity>()
            }
            R.id.signOut->signOutUser()
            R.id.createTeam->startActivity<TeamRegistration>()
            R.id.create_team_Button->startActivity<TeamRegistration>()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        getUserInfo()
    }

    private fun makeViewsInvisible(vararg view:View)
    {
        for(v in view)
        {
            v.visible=false
        }
    }

        private fun makeViewsVisible(vararg view:View)
        {
            for(v in view)
            {
                v.visible=true
            }
        }

fun makeLayoutVisible()
{
    makeViewsVisible(dashboard_layout)
    return
}


    override fun onBackPressed() {

        if(supportFragmentManager.backStackEntryCount>0)
        {
            supportFragmentManager.popBackStackImmediate()
        }

        else {

            super.onBackPressed()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun signOutUser() {
        mAuth?.signOut()
        startActivity<MainActivity>()
    }

private fun fetchTeamFromDatabase()
{
    val playerId =mAuth?.uid.toString()
val teamRef=FirebaseDatabase.getInstance()
val playersTeamReference=FirebaseDatabase.getInstance().getReference("/PlayersTeam/$playerId")
    playersTeamReference.addListenerForSingleValueEvent(object: ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {}
        override fun onDataChange(p0: DataSnapshot) {
           if(p0.exists())
           {
               p0.children.forEach{
                   val teamId=it.key
                    teamRef.getReference("/Team/$teamId").also { task ->
                       task.addListenerForSingleValueEvent(object:ValueEventListener{
                           override fun onCancelled(p0: DatabaseError) {
                               TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                           }

                           override fun onDataChange(p0: DataSnapshot) {
                               //get the actual Team (Name and Logo)
                               val team_Id=p0.child("teamId").value.toString()
                               val teamLogo=p0.child("teamLogo").value.toString()
                               val teamName=p0.child("teamName").value.toString()
                               val teamCaptain=p0.child("captainName").value.toString()
                               val teamCity=p0.child("city").value.toString()

                               //cardView color
                               val red=(10..230).random()
                               val green=(10..230).random()
                               val blue=(10..230).random()
                               val color= Color.argb(255,red,green,blue)
                               teamAdapter.add(MyTeamOnDashboard(teamLogo,teamName,teamCaptain,teamCity,team_Id))
                           }

                       })
                   }
               }

               dashboard_team_recyclerView.adapter=teamAdapter

           }
        }
    })
}

    class MyTeamOnDashboard(var teamLogo:String,
                            var teamName:String,
                            var teamCaptain:String,
                            var teamCity:String,
                            var teamId:String):Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.my_team_list_ondashboard
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            val logo=viewHolder.itemView.findViewById<ImageView>(R.id.my_team_logo_dashboard)
            Picasso.get().load(teamLogo).into(logo)
            viewHolder.itemView.my_team_name_dashboard.text=teamName

        }


    }




    private fun fetchMatchFromDatabase()
    {

        val playerId =mAuth?.uid.toString()
        val teamRef=FirebaseDatabase.getInstance()
        val teamsMatchRef=FirebaseDatabase.getInstance()
        val playersTeamReference=FirebaseDatabase.getInstance().getReference("/PlayersTeam/$playerId")
        playersTeamReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    Log.d("FetchMatch","PlayerId Received")
                    p0.children.forEach{
                        val teamId=it.key
                        teamRef.getReference("/TeamsMatch/$teamId").also { task ->
                            task.addListenerForSingleValueEvent(object:ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if(p0.exists()){
                                        Log.d("FetchMatch",teamId)
                                        p0.children.forEach {
                                            val matchId = it.key
                                            teamsMatchRef.getReference("/Match/$matchId").also {task->
                                                task.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError) {
                                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                                    }


                                                    override fun onDataChange(p0: DataSnapshot) {


                                                        val team_A_Id=p0.child("team_A").value.toString()
                                                        val team_B_Id=p0.child("team_B").value.toString()
                                                        val match_date=p0.child("matchDate").value.toString()
                                                        val match_time=p0.child("matchTime").value.toString()
                                                        val match_overs=p0.child("matchOver").value.toString()



                                                        matchAdapter.add(MyMatchesOnDashboard(team_A_Id,team_B_Id,match_date,match_time,match_overs))
                                                    }
                                                })
                                            }
                                        }
                                    }




                                }

                            })
                        }
                    }

                    match_card_recycler_view_dashboard.adapter=matchAdapter

                }
            }
        })
    }








    class MyMatchesOnDashboard(var team_A_Id:String,
                               var team_B_Id:String,
                               var match_date:String,
                               var match_time:String,
                               var match_overs:String):Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.match_card_on_dashboard
        }


        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.match_cardView

            viewHolder.itemView.team_A_total_overs_match_card.text=match_overs
            viewHolder.itemView.team_B_total_overs_match_card.text=match_overs
            viewHolder.itemView.date_of_match.text=match_date
            viewHolder.itemView.starting_time_of_match.text=match_time



            val teamARef= FirebaseDatabase.getInstance().getReference("/Team/$team_A_Id")
            teamARef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val nameTeamA=p0.child("teamName").value.toString()
                    val logoTeamA=p0.child("teamLogo").value.toString()
                    Log.d("FetchMatch",nameTeamA)

                    viewHolder.itemView.team_A_name_match_card.text=nameTeamA
                    val logo_team_A=viewHolder.itemView.findViewById<ImageView>(R.id.team_A_logo_match_card)
                    Picasso.get().load(logoTeamA).into(logo_team_A)

                }
            })


            val teamBRef= FirebaseDatabase.getInstance().getReference("/Team/$team_B_Id")
            teamBRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {

                    val nameTeamB=p0.child("teamName").value.toString()
                    val logoTeamB=p0.child("teamLogo").value.toString()


                    Log.d("FetchMatch",nameTeamB)

                    viewHolder.itemView.team_B_name_match_card.text=nameTeamB
                    val logo_team_B=viewHolder.itemView.findViewById<ImageView>(R.id.team_B_logo_match_card)
                    Picasso.get().load(logoTeamB).into(logo_team_B)

                }
            })

        }


    }





}