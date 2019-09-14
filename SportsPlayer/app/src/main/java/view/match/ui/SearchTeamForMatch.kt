package view.match.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import view.match.StartMatchActivity
import com.example.sportsplayer.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.pawegio.kandroid.inflateLayout
import com.pawegio.kandroid.onQueryChange
import com.pawegio.kandroid.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_search_team_for_match.*
import model.Team

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchTeamForMatch.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class SearchTeamForMatch : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private var firebaseDatabase: FirebaseDatabase? = null
    lateinit var ref: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseDatabase = FirebaseDatabase.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("Team")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_team_for_match, container, false)
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

    override fun onStart() {
        super.onStart()
        recyclerView_SearchTeamForMatch.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))

        searchView_SearchTeamForMatch.onQueryChange { query ->
            run {
                if (query.isNotEmpty()) {
                    searchTeam(query)
                } else {
                    recyclerView_SearchTeamForMatch.removeAllViewsInLayout()
                }
            }
        }
    }
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(teamId:String,teamLogo:String,teamName:String)
    }





    private fun searchTeam(inputText:String)
    {
        val allTeamDatabaseRef=firebaseDatabase?.getReference("Team")
        var query: Query?=allTeamDatabaseRef?.orderByChild("teamName")?.
            startAt(inputText)?.endAt(inputText +"\uf8ff")

        //query is a reference to the specific Node
        val option = FirebaseRecyclerOptions.Builder<Team>()
            .setQuery(query!!,Team::class.java)
            .setLifecycleOwner(this)
            .build()

        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Team, SearchTeamViewHolder>(option)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTeamViewHolder {

                /**team search list view may changed **/
                val itemView=context?.inflateLayout(R.layout.search_team_list_for_match ,parent)
                return SearchTeamViewHolder(itemView)
            }

            override fun onBindViewHolder(teamViewHolder: SearchTeamViewHolder, position: Int, model:Team) {
                teamViewHolder.itemView.setOnClickListener {
                    //view-> listener for item click
                    run {

                        val teamId=model.teamId
                        val teamLogo=model.teamLogo
                        val teamName=model.teamName
                        listener?.onFragmentInteraction(teamId,teamLogo!!,teamName)
                        activity?.onBackPressed()


                        /**
                        activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.add(R.id.fragmentContainer,teamDetailFragment)
                        ?.addToBackStack(null)
                        ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        ?.commit()
                         **/
                        recyclerView_SearchTeamForMatch.adapter=null

                        toast("Clicked")
                    }
                }
                val newTeam = getRef(position).key.toString()
                ref.child(newTeam).addValueEventListener(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        toast("Error ${p0.toException()}")
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        //mpb_progressbar.visibility = if(itemCount == 0) View.VISIBLE else View.GONE

                        Picasso.get().load(model.teamLogo).into(teamViewHolder.tLogo)
                        teamViewHolder.tName.text = model.teamName
                        teamViewHolder.tCity.text=model.city

                    }
                })
            }

        }
        recyclerView_SearchTeamForMatch.adapter= firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }


    class SearchTeamViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!)
    {

        internal  var tLogo=itemView!!.findViewById<ImageView>(R.id.teamLogo__SearchTeamListForMatch)
        internal var tName=itemView!!.findViewById<TextView>(R.id.teamName_SearchTeamListForMatch)
        internal var tAdmin=itemView!!.findViewById<TextView>(R.id.teamAdmin_SearchTeamListForMatch)
        internal var tCity=itemView!!.findViewById<TextView>(R.id.teamCity_SearchTeamListForMatch)


    }




}
