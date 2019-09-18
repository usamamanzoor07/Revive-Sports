package view.ProfilePackage.ProfileFragments
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_player_matches_profile.*
import kotlinx.android.synthetic.main.match_card_on_dashboard.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PlayerFragmentMatches.OnFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */

class PlayerFragmentMatches:Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var mAuth: FirebaseAuth?=null
    val matchAdapter= GroupAdapter<ViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player_matches_profile, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
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

    override fun onResume() {
        super.onResume()
        fetchMatchFromDatabase()
    }


    private fun fetchMatchFromDatabase()
    {

        mAuth= FirebaseAuth.getInstance()
        val playerId =mAuth?.uid.toString()
        val teamRef= FirebaseDatabase.getInstance()
        val teamsMatchRef= FirebaseDatabase.getInstance()
        val playersTeamReference= FirebaseDatabase.getInstance().getReference("/PlayersTeam/$playerId")
        playersTeamReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    Log.d("FetchMatch","PlayerId Received")
                    p0.children.forEach{
                        val teamId=it.key
                        teamRef.getReference("/TeamsMatch/$teamId").also { task ->
                            task.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if(p0.exists()){
                                        Log.d("FetchMatch",teamId)
                                        p0.children.forEach {
                                            val matchId = it.key
                                            teamsMatchRef.getReference("/Match/$matchId").also {task->
                                                task.addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
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

                    match_card_recycler_view_player_fragment_match.adapter=matchAdapter

                }
            }
        })
    }



    class MyMatchesOnDashboard(var team_A_Id:String,
                               var team_B_Id:String,
                               var match_date:String,
                               var match_time:String,
                               var match_overs:String): Item<ViewHolder>(){
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }


}