package view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.annotation.NonNull
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
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.dashboard_activity.*
import kotlinx.android.synthetic.main.match_card_on_dashboard.view.*
import kotlinx.android.synthetic.main.my_team_list_ondashboard.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import view.ProfilePackage.Profile
import view.fragment.SearchTeamFragment
import view.match.StartMatchActivity
import view.match.ui.MatchScoring
import view.team.TeamDetailActivity
import java.io.File

@Suppress("DEPRECATION")
class Dashboard:AppCompatActivity(), SearchTeamFragment.OnFragmentInteractionListener
{
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val CODE_IMAGE_GALLERY=1
    val SAMPLE_CROPPED_IMAGE_NAME="SampleCropImage"
    private var selectedProfileUri: Uri? = null

    //For Match
    lateinit var team_A_name: String
    lateinit var team_A_logo: String
    lateinit var team_B_name: String
    lateinit var team_B_logo: String


    private var mAuth:FirebaseAuth?=null
   private lateinit var searchTeamFragment: SearchTeamFragment
    val adapter=GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)
        mAuth= FirebaseAuth.getInstance()


        adapter.setOnItemClickListener { item, view ->

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
                startActivity<MatchScoring>()
            }


/**
            val timePicker:TimePickerDialog= TimePickerDialog(this,object:TimePickerDialog.OnTimeSetListener{
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            },hh,mint,false)
            timePicker.show()

**/






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
                CODE_IMAGE_GALLERY->{
                   val imageUri=data.data

                    if(imageUri!=null)
                    {
                        startCrop(imageUri)
                    }

                }
                UCrop.REQUEST_CROP->{
                    val imageUriResultCrop=UCrop.getOutput(data)
                    if(imageUriResultCrop != null)
                    {
                        cropedImage.setImageURI(imageUriResultCrop)
                    }

                }
            }

    }

    fun startCrop(@NonNull uri:Uri){

        var destinationFileName=SAMPLE_CROPPED_IMAGE_NAME
        destinationFileName+=".jpg"

        val uCrop:UCrop= UCrop.of(uri, Uri.fromFile(File(cacheDir,destinationFileName)))
        uCrop.withAspectRatio(16f, 9f)
//        uCrop.withAspectRatio(3,4)
//        uCrop.useSourceImageAspectRatio()
//        uCrop.withAspectRatio(2,3)
//        uCrop.withAspectRatio(16,9)
        uCrop.withMaxResultSize(450,450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this)


    }

    fun getCropOptions():UCrop.Options
    {
        val options=UCrop.Options()

        options.setCompressionQuality(70)

        //Compression Type
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
       //  options.setCompressionFormat(Bitmap.CompressFormat.JPEG)

        //Ui
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)

        //Color
        options.setStatusBarColor(resources.getColor(R.color.colorPrimary))
        options.setToolbarColor(resources.getColor(R.color.colorPrimaryDark))

        options.setToolbarTitle("Cropped Image")

        return options

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

        val playerRefCatches= FirebaseDatabase.getInstance().getReference("/FieldingStats/$uid")
        playerRefCatches.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {


                val playerCatches=p0.child("catches").value.toString()

                catching_figures_Dashboard.text=playerCatches


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
            R.id.profile->startActivity<EditProfile>()
            R.id.upcomingMatch->{
                startActivity<Profile>()
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
                               adapter.add(MyTeamOnDashboard(teamLogo,teamName,teamCaptain,teamCity,team_Id,color))
                           }

                       })
                   }
               }

               dashboard_team_recyclerView.adapter=adapter

           }
        }
    })
}

    class MyTeamOnDashboard(var teamLogo:String,
                            var teamName:String,
                            var teamCaptain:String,
                            var teamCity:String,
                            var teamId:String,
                            val color:Int):Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.my_team_list_ondashboard
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.team_logo_cardView.setCardBackgroundColor(color)
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
                                        Log.d("FetchMatch","TeamId Received")
                                        p0.children.forEach {
                                            val matchId = it.key
                                            teamsMatchRef.getReference("/Match/$matchId").also {task->
                                                task.addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onCancelled(p0: DatabaseError) {
                                                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                                    }

                                                    override fun onDataChange(p0: DataSnapshot) {
                                                        Log.d("FetchMatch","MatchId Received")

                                                        val team_A_Id=p0.child("team_A").value.toString()
                                                        Log.d("FetchMatch", team_A_Id)
                                                        val team_B_Id=p0.child("team_B")
                                                        val match_date=p0.child("matchDate").value.toString()
                                                        val match_time=p0.child("matchTime").value.toString()
                                                        val match_overs=p0.child("matchOver").value.toString()

                                                        val team_A_Ref=FirebaseDatabase.getInstance().getReference("/Team/$team_A_Id")
                                                        team_A_Ref.addListenerForSingleValueEvent(object: ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError) {}
                                                            override fun onDataChange(p0: DataSnapshot) {

                                                                Log.d("FetchMatch","Team A Details Received")
                                                                    //get the actual Team (Name and Logo)
                                                                    team_A_logo=p0.child("teamLogo").value.toString()
                                                                    team_A_name=p0.child("teamName").value.toString()

                                                            }
                                                        })

                                                        val team_B_Ref=FirebaseDatabase.getInstance().getReference("/Team/$team_B_Id")

                                                        team_B_Ref.addListenerForSingleValueEvent(object: ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError) {}
                                                            override fun onDataChange(p0: DataSnapshot) {

                                                                Log.d("FetchMatch","Team B Details Received")

                                                                    //get the actual Team (Name and Logo)
                                                                    team_B_logo=p0.child("teamLogo").value.toString()
                                                                    team_B_name=p0.child("teamName").value.toString()




                                                            }
                                                        })
                                                        adapter.add(MyMatchesOnDashboard(team_A_name,team_A_logo,team_B_name,team_B_logo,match_date,match_time,match_overs))
                                                    }
                                                })
                                            }
                                        }
                                    }




                                }

                            })
                        }
                    }

                    match_card_recycler_view_dashboard.adapter=adapter

                }
            }
        })
    }






    class MyMatchesOnDashboard(var team_A_name:String,
                               var team_A_logo:String,
                               var team_B_name:String,
                               var team_B_logo:String,
                               var match_date:String,
                               var match_time:String,
                               var match_overs:String):Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.match_card_on_dashboard
        }


        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.match_cardView
            val logo1=viewHolder.itemView.findViewById<ImageView>(R.id.team_A_logo_match_card)
            Picasso.get().load(team_A_logo).into(logo1)
            viewHolder.itemView.team_A_name_match_card.text=team_A_name

            val logo2=viewHolder.itemView.findViewById<ImageView>(R.id.team_B_logo_match_card)
            Picasso.get().load(team_B_logo).into(logo2)
            viewHolder.itemView.team_B_name_match_card.text=team_B_name

            viewHolder.itemView.team_A_total_overs_match_card.text=match_overs
            viewHolder.itemView.team_B_total_overs_match_card.text=match_overs
            viewHolder.itemView.date_of_match.text=match_date
            viewHolder.itemView.starting_time_of_match.text=match_time



        }


    }





}