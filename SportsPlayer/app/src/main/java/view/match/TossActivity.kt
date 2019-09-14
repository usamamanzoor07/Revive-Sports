package view.match

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sportsplayer.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_toss.*
import kotlinx.android.synthetic.main.selected_team_listview.view.*
import org.jetbrains.anko.startActivity

class TossActivity : AppCompatActivity() {

    val groupAdapter= GroupAdapter<ViewHolder>().apply { spanCount=2 }
   lateinit var teamA_Id:String
   lateinit var teamB_Id:String
   lateinit var newMatchId:String //new match id sent from StartMatchActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toss)

        recyclerView_TossActivity.apply {
            layoutManager= GridLayoutManager(context,groupAdapter.spanCount).apply {
                spanSizeLookup=groupAdapter.spanSizeLookup
            }
            adapter=groupAdapter
        }
//GroupiAdapter OnItemClickListener
        groupAdapter.setOnItemClickListener { item, view ->

            val team = item as SelectedTeam
            val teamId=team.teamId
Log.d("Team_ID_Adapter",teamId)
            val teamName=team.name
            startActivity<StartInning>(
                "batingTeamId" to teamId,
                "batingTeamName" to teamName,
                "newMatchId" to newMatchId,
                "teamA_Id" to teamA_Id,
                "teamB_Id" to teamB_Id)

        }
    }


    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        groupAdapter.clear()
        showTeams()
    }

    fun showTeams()
    {

         teamA_Id=intent.getStringExtra("teamA_Id")
        teamB_Id=intent.getStringExtra("teamB_Id")
        newMatchId=intent.getStringExtra("newMatchId")

        val teamA_Logo=intent.getStringExtra("teamALogo")
        val teamB_Logo=intent.getStringExtra("teamBLogo")
        val teamA_Name=intent.getStringExtra("teamAName")
        val teamB_Name=intent.getStringExtra("teamBName")
        groupAdapter.add(SelectedTeam(teamA_Logo,teamA_Name,teamA_Id))
        groupAdapter.add(SelectedTeam(teamB_Logo,teamB_Name,teamB_Id))
    }
    class SelectedTeam(val logo:String,
                       val name:String,
                       val teamId:String): Item<ViewHolder>()
    {
        override fun getLayout(): Int {
            return R.layout.selected_team_listview
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {

            Picasso.get().load(logo).into(viewHolder.itemView.teamLogo_selected_team_List)
            viewHolder.itemView.teamFullName_selected_team_List.text=name

        }
        override fun getSpanSize(spanCount: Int, position: Int): Int {
            return spanCount/2
        }


    }




}
