package view.ProfilePackage

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import com.example.sportsplayer.R
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_activity.*
import view.ProfilePackage.ProfileFragments.*
import view.ProfilePackage.ProfileFragments.StatsDetailFragments.BattingStatsFragment
import view.ProfilePackage.ProfileFragments.StatsDetailFragments.BowlingStatsFragment
import view.ProfilePackage.ProfileFragments.StatsDetailFragments.CaptainStatsFragment
import view.ProfilePackage.ProfileFragments.StatsDetailFragments.FieldingStatsFragment


class Profile:AppCompatActivity(), PlayerFragmentStats.OnFragmentInteractionListener,
    PlayerFragmentGeneral.OnFragmentInteractionListener,PlayerFragmentMatches.OnFragmentInteractionListener,
    PlayerFragmentTeams.OnFragmentInteractionListener,PlayerFragmentMedia.OnFragmentInteractionListener,
    PlayerFragmentConnections.OnFragmentInteractionListener,BattingStatsFragment.OnFragmentInteractionListener,
    BowlingStatsFragment.OnFragmentInteractionListener,FieldingStatsFragment.OnFragmentInteractionListener,
        CaptainStatsFragment.OnFragmentInteractionListener
{
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    var mAuth:FirebaseAuth?=null
    var firebaseDatabase:FirebaseDatabase?=null
   //var myStorage: FirebaseStorage? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        setViewsContent()



        mAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance() //get the firebase database instance
        getProfileData()




    }


    override fun onStart() {
        super.onStart()


    }

    override fun onResume() {
        super.onResume()
    }

    private fun setViewsContent()
    {
        val fragmentAdapter= ProfileSectionPagerAdapter(supportFragmentManager)
        viewPager.adapter=fragmentAdapter
        tabLayout_profile.setupWithViewPager(viewPager)

    }



//get player detail from firebase database
private fun getProfileData()
    {

        val progressDialog: ProgressDialog = ProgressDialog.show(this, "getting Info", "please wait...")
        progressDialog.show()
        val user = mAuth?.uid

        if(user!=null)
        {
            val userRef=firebaseDatabase?.reference?.child("/PlayerBasicProfile") //by using firebase database instance we get reference to the specific Node
            userRef?.child(user)?.addValueEventListener(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        val profileImage=p0.child("profile_img").value.toString()
                        val name=p0.child("name").value.toString()

                        Picasso
                            .get()
                            .load(profileImage)
                            .fit() // use fit() and centerInside() for making it memory efficient.
                            .centerInside()
                            .into(playerImage_ProfileActivity)
                        playerName_ProfileActivity.setText(name)

                        progressDialog.dismiss()

                    }
                }

            }
            )




        }


    }
}