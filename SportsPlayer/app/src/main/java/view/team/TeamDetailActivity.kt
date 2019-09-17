package view.team


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pawegio.kandroid.visible
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_team_detail.*
import view.team.ui.SectionPagerAdapter
import view.team.ui.TeamMatchFragment
import view.team.ui.TeamMemberFragment
import view.team.ui.TeamStatsFragment
import org.jetbrains.anko.*

class TeamDetailActivity : AppCompatActivity(), View.OnClickListener,
TeamStatsFragment.OnFragmentInteractionListener,
    TeamMatchFragment.OnFragmentInteractionListener,
    TeamMemberFragment.OnFragmentInteractionListener
     {



    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
         lateinit var captainId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)


        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)


        }

        setViewsContent()
        //assign Click Listener to Button
        challenge_for_match.setOnClickListener(this)

        val captainId =intent.getStringExtra("captainId").toString()
        val currentPlayer = FirebaseAuth.getInstance().uid.toString()
        if (currentPlayer!=captainId){
            makeViewsInvisible(challenge_for_match)
        }


    }


         private fun getUserInfo(teamId:String)
         {
             val teamRef= FirebaseDatabase.getInstance().getReference("/Team/$teamId")
             teamRef.addListenerForSingleValueEvent(object: ValueEventListener {
                 override fun onCancelled(p0: DatabaseError) {
                     TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                 }
                 override fun onDataChange(p0: DataSnapshot) {
                     captainId=p0.child("captainId").value.toString()
                     val playerRef= FirebaseDatabase.getInstance().getReference("/PlayerBasicProfile/$captainId")
                     playerRef.addListenerForSingleValueEvent(object: ValueEventListener {
                         override fun onCancelled(p0: DatabaseError) {
                             TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                         }

                         override fun onDataChange(p0: DataSnapshot) {

                             val captainName=p0.child("name").value.toString()
                             teamCaptain_TeamDetailActivity.text=captainName
                         }
                     })
                 }
             })
         }




         private fun setViewsContent()
         {
             val teamId=intent.getStringExtra("teamId")
             val teamLogo=intent.getStringExtra("teamLogo")
             val teamName=intent.getStringExtra("teamName")
             val teamCity=intent.getStringExtra("teamCity")
             val captainId =intent.getStringExtra("captainId")
             Log.d("CaptainId",captainId)

             supportActionBar?.title=teamName

             val fragmentAdapter=SectionPagerAdapter(teamId,captainId,supportFragmentManager)
             viewPager.adapter=fragmentAdapter
             tabLayout.setupWithViewPager(viewPager)

             Picasso.get().load(teamLogo).into(team_logo_TeamDetailActivity)
             teamCity_TeamDetailActivity.text=teamCity
             getUserInfo(teamId)


         }

         override fun onClick(view: View?) {
             when(view?.id)
             {
                 R.id.challenge_for_match->{
                     toast("Challenge For Match")

             }
         }
     }
         private fun makeViewsInvisible(vararg view:View)
         {
             for(v in view)
             {
                 v.visible=false
             }
         }

     }
