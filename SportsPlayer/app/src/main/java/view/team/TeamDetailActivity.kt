package view.team

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pawegio.kandroid.startActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_team_detail.*
import view.fragment.SearchPlayerToAddInTeam
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)


        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)


        }

           setViewsContent()
        //assign Click Listener to Button
        addNewPlayer_TeamDetailActivity.setOnClickListener(this)




    }


         fun getUserInfo(teamId:String)
         {
             val teamRef= FirebaseDatabase.getInstance().getReference("/Team/$teamId")
             teamRef.addListenerForSingleValueEvent(object: ValueEventListener {
                 override fun onCancelled(p0: DatabaseError) {
                     TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                 }
                 override fun onDataChange(p0: DataSnapshot) {
                     val captainId=p0.child("captainId").value.toString()
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

             supportActionBar?.title=teamName

             val fragmentAdapter=SectionPagerAdapter(teamId,supportFragmentManager)
             viewPager.adapter=fragmentAdapter
             tabLayout.setupWithViewPager(viewPager)

             Picasso.get().load(teamLogo).into(team_logo_TeamDetailActivity)
             teamCity_TeamDetailActivity.text=teamCity
             getUserInfo(teamId)


         }

    override fun onStart() {
        super.onStart()


    }

    override fun onClick(view: View?) {
        when(view?.id)
        {
            R.id.addNewPlayer_TeamDetailActivity->{
                val teamId=intent.getStringExtra("teamId")
                Log.d("Team_Detail_Activity",teamId)
                startActivity<SearchPlayerToAddInTeam>("teamId" to teamId)

            }

        }
    }


}
