package view.match.ui

import com.example.sportsplayer.R
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.available_team_list.view.*
import kotlinx.android.synthetic.main.fragment_team_available.*


class TeamAvailableFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var mAuth: FirebaseAuth?=null

    val groupAdapter= GroupAdapter<ViewHolder>().apply { spanCount=3 }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_team_available, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_TeamAvailabelFragment.apply {
            layoutManager=GridLayoutManager(context,groupAdapter.spanCount).apply {
                spanSizeLookup=groupAdapter.spanSizeLookup
            }
            adapter=groupAdapter
        }


    }

    override fun onStart() {
        super.onStart()
        mAuth= FirebaseAuth.getInstance()

        groupAdapter.setOnItemClickListener { item, view ->

            val team = item as AvailableTeamForMatch
            Log.d("team_ID",team.team_Id)
            val teamId=team.team_Id
            val teamLogo=team.teamLogo
            val teamName=team.teamFullName

            listener?.onFragmentInteraction(teamId,teamLogo,teamName)
            activity?.onBackPressed()
            //activity?.supportFragmentManager?.popBackStack()


            /**
            val intent= Intent()
            intent.putExtra("teamId",teamId)
            intent.putExtra("teamLogo",teamLogo)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
            **/
            // [END SENDING RESULT]

        }

    }

    override fun onResume() {
        super.onResume()
        groupAdapter.clear()
        fetchTeamFromDatabase()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(teamId:String,teamLogo:String,teamName:String)
    }


    private fun fetchTeamFromDatabase()
    {
        val playerId =mAuth?.uid.toString()
        val teamRef= FirebaseDatabase.getInstance()
        val playersTeamReference= FirebaseDatabase.getInstance().getReference("/PlayersTeam/$playerId")
        playersTeamReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    p0.children.forEach{
                        val teamId=it.key
                        teamRef.getReference("/Team/$teamId").also { task ->
                            task.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    //get the actual Team (Name and Logo)
                                    val teamShortName=p0.child("shortName").value.toString()
                                    val teamFullName=p0.child("teamName").value.toString()
                                    val team_Id=p0.child("teamId").value.toString()
                                    val teamLogo=p0.child("teamLogo").value.toString()


                                    //cardView color
                                    val red=(10..230).random()
                                    val green=(10..230).random()
                                    val blue=(10..230).random()
                                    val color= Color.argb(255,red,green,blue)
                                    groupAdapter.add(AvailableTeamForMatch(team_Id,teamShortName,teamFullName,color,teamLogo))
                                }

                            })
                        }
                    }


                }
            }
        })
    }






    class AvailableTeamForMatch(val team_Id:String,
                                val teamShortName:String,
                                val teamFullName:String,
                                val color:Int,
                                val teamLogo:String):Item<ViewHolder>()
    {
        override fun getLayout(): Int {
           return R.layout.available_team_list
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.cardView_Available_team_list.setCardBackgroundColor(color)
           viewHolder.itemView.teamShortName_available_team_List.text=teamShortName
            viewHolder.itemView.teanFullName_available_team_List.text=teamFullName

        }

        override fun getSpanSize(spanCount: Int, position: Int): Int {
            return spanCount/3
        }

    }

}
