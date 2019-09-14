package view.team.ui

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.example.sportsplayer.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.dashboard_activity.*
import kotlinx.android.synthetic.main.fragment_team_member.*
import kotlinx.android.synthetic.main.team_member_list.view.*

class TeamMemberFragment(val teamId: String) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    val adapter= GroupAdapter<ViewHolder>() //groupi Adapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_team_member, container, false)
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
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        adapter.clear()
        fetchTeamMember(teamId)
    }


    private fun fetchTeamMember(teamId:String)
    {
        adapter.clear()
        val teamRef= FirebaseDatabase.getInstance()
        val teamsPlayerRef= FirebaseDatabase.getInstance().getReference("/TeamsPlayer/$teamId")
        teamsPlayerRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    p0.children.forEach{
                        val playerId=it.key
                        Log.d("TeamMember_ID",playerId)
                        teamRef.getReference("/PlayerBasicProfile/$playerId").also { task ->
                            task.addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {
                                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    //get the actual player
                                    val playerImage=p0.child("profile_img").value.toString()
                                    val playerName=p0.child("name").value.toString()
                                    val playerPhn=p0.child("phoneNumber").value.toString()
                                    val playerCity=p0.child("city").value.toString()


                                    //cardView color
                                    val red=(10..230).random()
                                    val green=(10..230).random()
                                    val blue=(10..230).random()
                                    val color= Color.argb(255,red,green,blue)
                                    adapter.add(TeamsPlayer(playerImage,playerName,playerPhn,playerCity,color))
                                }

                            })
                        }
                    }

                    recyclerView_TeamMemberFragment.adapter=adapter

                }
            }
        })


    }



    class TeamsPlayer(var playerImage:String,
                            var playerName:String,
                            var playerPhn:String,
                            var playerCity:String,
                            val color:Int): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.team_member_list
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.cardView_teamMemberList.setCardBackgroundColor(color)
            val image=viewHolder.itemView.findViewById<ImageView>(R.id.playerImage_team_member_list)
            Picasso.get().load(playerImage).into(image)
            viewHolder.itemView.playerName_teamMemberList.text=playerName
            viewHolder.itemView.playerPhn_teamMemberList.text=playerPhn
        }


    }

}
