package view.match.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsplayer.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import com.pawegio.kandroid.inflateLayout
import com.pawegio.kandroid.onQueryChange
import com.pawegio.kandroid.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_search_team_for_match.*
import model.Team
import org.jetbrains.anko.find
import view.match.SelectSquadActivity
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
    val SQUAD_RC = 1
    lateinit var team_Id: String
    lateinit var team_name: String
    lateinit var team_logo: String

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
        fun onFragmentInteraction(teamId:String,teamLogo:String,teamName:String,teamSquad:ArrayList<String>)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("XXXX_AvailableTeams_F", "onActivityResult called")

        if (data != null && resultCode == Activity.RESULT_OK)
            when (requestCode) {
                SQUAD_RC -> {
                    val bundle = data.extras
                    val squadList = bundle.getStringArrayList("list") as ArrayList<String>
                    toast("Fragment_OnActivityResult")
                    listener?.onFragmentInteraction(team_Id, team_logo, team_name,squadList)
                    activity?.onBackPressed()

                }
            }
    }



    //Launch SelectSquadActivity
    private fun selectSquadForMatch(teamId: String) {
        val intent = Intent(activity,SelectSquadActivity::class.java)
        intent.putExtra("teamId", teamId)
        this.startActivityForResult(intent, SQUAD_RC)
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
        var card:MaterialCardView

        val firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Team, SearchTeamViewHolder>(option)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTeamViewHolder {

                /**team search list view may changed **/
                val itemView=context?.inflateLayout(R.layout.search_team_list_for_match ,parent)
                return SearchTeamViewHolder(itemView)
            }

            override fun onBindViewHolder(teamViewHolder: SearchTeamViewHolder, position: Int, model:Team) {

                card=teamViewHolder.itemView.find(R.id.cardView_SearchTeamListForMatch)
                card.setOnClickListener { view ->
                    run {
                        val v = view as MaterialCardView
                        v.isChecked = !v.isChecked
                        if (v.isChecked) {

                            toast("select Team")
                            AlertDialog.Builder(activity)
                                .setCancelable(true)
                                .setTitle("Conformation")
                                .setMessage("Do you want to select ${model.teamName}")
                                .setNegativeButton("No") { dialog, _ ->
                                    run {
                                        card.isChecked = false
                                        dialog.dismiss()
                                    }
                                }.setPositiveButton("Yes") { dialog, _ ->
                                    run {

                                        team_Id = model.teamId
                                        team_name = model.teamName
                                        team_logo = model.teamLogo!!
                                        selectSquadForMatch(team_Id)

                                        /**
                                        val teamLogo = item.teamLogo
                                        val teamName = item.teamFullName
                                        listener?.onFragmentInteraction(teamId, teamLogo, teamName)
                                        activity?.onBackPressed()
                                         **/
                                        toast("selected")
                                        dialog.dismiss()
                                    }
                                }.create().show()

                        }
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
