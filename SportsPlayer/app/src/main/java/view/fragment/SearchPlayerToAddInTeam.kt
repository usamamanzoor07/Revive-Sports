package view.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsplayer.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.pawegio.kandroid.inflateLayout
import com.pawegio.kandroid.onQuerySubmit
import com.pawegio.kandroid.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_search_player_to_add_in_team.*
import model.PlayerBasicProfile
import org.jetbrains.anko.startActivity
import view.team.TeamDetailActivity

class SearchPlayerToAddInTeam : AppCompatActivity() {
    // TODO: Rename and change types of parameters
    private var firebaseDatabase: FirebaseDatabase? = null
    lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search_player_to_add_in_team)

        firebaseDatabase = FirebaseDatabase.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("PlayerBasicProfile")

    }


    override fun onStart() {
        super.onStart()

        searchView_Player.onQuerySubmit {
            query->
            run {
                val dialCode="+92"
               val phn=query.substring(1)
              val newQuery=dialCode.plus(phn)
                Log.d("Search_Player",newQuery)
                searchPlayer(newQuery)

            }


        }
    }
    private fun searchPlayer(inputText:String)
    {

        val allPlayerDatabaseRef=firebaseDatabase?.getReference("PlayerBasicProfile")
        val query: Query?=allPlayerDatabaseRef?.orderByChild("phoneNumber")?.
            startAt(inputText)?.endAt(inputText +"\uf8ff")

        //query is a reference to the specific Node
        val option = FirebaseRecyclerOptions.Builder<PlayerBasicProfile>()
            .setQuery(query!!, PlayerBasicProfile::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<PlayerBasicProfile, SearchPlayerViewHolder>(option)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlayerViewHolder {

                val itemView=inflateLayout(R.layout.player_search_list ,parent)
                return SearchPlayerViewHolder(itemView)
            }

            override fun onBindViewHolder(playerViewHolder: SearchPlayerViewHolder, position: Int, model:PlayerBasicProfile) {
                playerViewHolder.itemView.setOnClickListener {
                    //view-> listener for item click
                    run {

                        try {
                            val newRef=firebaseDatabase?.reference

                            val playerId=model.playerId
                            val teamId=intent.getStringExtra("teamId")
                            Log.d("Search_Player_teamId",teamId)
                            Log.d("Search_Player_playerId",playerId)

                            if(teamId.isNotEmpty()) {
                                val updateTeamMember = HashMap<String, Any>()
                                updateTeamMember["/TeamsPlayer/$teamId/$playerId"] = true
                                updateTeamMember["/PlayersTeam/$playerId/$teamId"] = true

                                newRef?.updateChildren(updateTeamMember)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            toast("PlayerBasicProfile Added")
                                            val intent = Intent()
                                            setResult(Activity.RESULT_OK, intent)
                                            finish()
                                            val teamRef=firebaseDatabase?.getReference("Team/$teamId")
                                            teamRef?.addListenerForSingleValueEvent(object:ValueEventListener{
                                                override fun onCancelled(p0: DatabaseError) {
                                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                                }

                                                override fun onDataChange(p0: DataSnapshot) {

                                                    val team_Id=p0.child("teamId").value.toString()
                                                    val teamLogo=p0.child("teamLogo").value.toString()
                                                    val teamName=p0.child("teamName").value.toString()
                                                    val teamCaptain=p0.child("captainName").value.toString()
                                                    val teamCity=p0.child("city").value.toString()

                                                    startActivity<TeamDetailActivity>(
                                                        "teamId" to teamId,
                                                        "teamLogo" to teamLogo,
                                                        "teamName" to teamName,
                                                        "teamCaptain" to teamCaptain,
                                                        "teamCity" to teamCity ,
                                                        "Dashboard" to "dashboard")

                                                }

                                            })




                                            //startActivity<TeamDetailActivity>()

                                        }
                                    }?.addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                            }
                            else{toast("team Id is empty")}

                        }catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                val player = getRef(position).key.toString()
                ref.child(player).addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        toast("Error ${p0.toException()}")
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        //show_progress.visibility = if(itemCount == 0) View.VISIBLE else View.GONE

                        Picasso.get().load(model.profile_img).into(playerViewHolder.pImage)
                        playerViewHolder.pName.text = model.name
                        playerViewHolder.pCity.text=model.city

                    }
                })
            }

        }
        recyclerView_searchPlayer.adapter= firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    class SearchPlayerViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!)
    {

        internal  var pImage=itemView!!.findViewById<ImageView>(R.id.search_player_image)
        internal var pName=itemView!!.findViewById<TextView>(R.id.search_player_name)
        internal var pCity=itemView!!.findViewById<TextView>(R.id.search_player_city)


    }




}
