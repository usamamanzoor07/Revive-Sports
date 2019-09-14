package view.team

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sportsplayer.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_teams_player_ready_to_play_match.*
import kotlinx.android.synthetic.main.player_in_selected_team_to_start_inning.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.toast

class TeamsPlayerReadyToPlayMatch : AppCompatActivity() {

    val groupAdapter= GroupAdapter<ViewHolder>().apply { spanCount=3 }
    lateinit var teamId:String
    lateinit var newMatchId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams_player_ready_to_play_match)

        //recycler View initialization
        recyclerView_TeamsPlayerReadyToPlayMatch.apply {
            layoutManager= GridLayoutManager(context,groupAdapter.spanCount).apply {
                spanSizeLookup=groupAdapter.spanSizeLookup
            }
            adapter=groupAdapter
        }
//groupAdapter OnItemClick Listener
        groupAdapter.setOnItemClickListener { player, view ->
            val team_player = player as SelectedTeamPlayer
            val name=team_player.name
            val playerId=team_player.player_id
            Log.d("GroupAdapter","Clicked")
            toast("Clicked")
   isPlayerAlreadySelected(playerId,name)
          }
    }


    private fun isPlayerAlreadySelected(playerId:String, name: String) {
        val matchRef=FirebaseDatabase.getInstance().getReference("/MatchScore/$newMatchId/$teamId")
 matchRef.addListenerForSingleValueEvent(object :ValueEventListener{
     override fun onCancelled(p0: DatabaseError) {}
     override fun onDataChange(p0: DataSnapshot) {

     if (p0.exists())
     {
         var found =false
         for(it in p0.children){
             Log.d("PlayersMatch",it.key)
             if(playerId==it.key)
             { found=true
                 break
             }
         }
         if(found)
         {Log.d("Reselection","found")
             alert {
                 title="PlayerBasicProfile Reselection"
                 message="$name already selected for Bating"
                 okButton { dialog -> dialog.dismiss() }
             }.show()
         } else
         {Log.d("Reselection","not found")
             setPlayer(playerId,name)
         }

     }else{
         Log.d("ONDATACHANGE","no data found")
          setPlayer(playerId,name)
     }
     }
 })
    }


    fun setPlayer(playerId: String,name: String)
    {
        val intent = Intent()
        intent.putExtra("name",name)
        intent.putExtra("playerId",playerId)
        setResult(Activity.RESULT_OK, intent)
        finish()

    }
    override fun onResume() {
        super.onResume()
        teamId=intent.getStringExtra("teamId")
        newMatchId=intent.getStringExtra("newMatchId")
        Log.d("FetchMatch_ID", newMatchId)

        groupAdapter.clear()
    fetchTeamMember(teamId)
    }


    private fun fetchTeamMember(teamId:String) {
        Log.d("FetchTeam_ID", teamId)
        val playerRef= FirebaseDatabase.getInstance()
        val teamsPlayerRef = FirebaseDatabase.getInstance().getReference("/TeamsPlayer/$teamId")
        teamsPlayerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { Log.d("FetchTeam_ID", "onCancelled") }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    Log.d("FetchTeam_ID", "team exist")
                    p0.children.forEach {
                        val playerId = it.key
                        Log.d("FetchPlayer_ID",playerId)

                        playerRef.getReference("/PlayerBasicProfile/$playerId").also { task ->
                            task.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    //get the actual player
                                    val playerId = p0.child("playerId").value.toString()
                                    val playerName = p0.child("name").value.toString()
                                    groupAdapter.add(SelectedTeamPlayer(playerName,playerId!!))

                                }

                            })
                        }

                    }
                }
            }
        })
    }


        class SelectedTeamPlayer(val name: String,val player_id:String) : Item<ViewHolder>() {
            override fun getLayout(): Int {
                return R.layout.player_in_selected_team_to_start_inning
            }

            override fun bind(viewHolder: ViewHolder, position: Int) {
                viewHolder.itemView.playerName_PlayerInTeamToStartInning.text = name

            }

            override fun getSpanSize(spanCount: Int, position: Int): Int {
                return spanCount / 3
            }

        }

}
