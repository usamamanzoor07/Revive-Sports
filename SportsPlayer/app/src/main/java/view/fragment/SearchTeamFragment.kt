package view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsplayer.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.pawegio.kandroid.inflateLayout
import com.pawegio.kandroid.onQueryChange
import com.pawegio.kandroid.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_search_team.*
import model.Team
import view.Dashboard
import view.team.TeamDetailActivity

class SearchTeamFragment : Fragment() {



    private var listener: OnFragmentInteractionListener? = null

    private var firebaseDatabase: FirebaseDatabase? = null
    lateinit var ref: DatabaseReference
    lateinit var toolbar:Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val menuItem:MenuItem=menu.findItem(R.id.actionbar_search)
        val searchView=menuItem.actionView as SearchView
        Log.d("SearchTeamFragment",searchView.isHovered.toString())
        searchView.onQueryChange {
                query->
            run {
                if (query.isNotEmpty())
                { searchTeam(query)
                }else{
                    recyclerViewTeamSearchFragment.removeAllViewsInLayout()
                }
            }

        }



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        when(item.itemId){

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        firebaseDatabase = FirebaseDatabase.getInstance()
        ref = FirebaseDatabase.getInstance().reference.child("Team")
        return LayoutInflater.from(container?.context).inflate(R.layout.fragment_search_team,container,false)
    }


    override fun onResume() {

        super.onResume()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }


    }

    override fun onStart() {
        super.onStart()

        recyclerViewTeamSearchFragment.addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))


    }

    override fun onPause() {
        super.onPause()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as Dashboard). makeLayoutVisible()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
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

                val itemView=context?.inflateLayout(R.layout.team_search_listview ,parent)
                return SearchTeamViewHolder(itemView)
            }

            override fun onBindViewHolder(teamViewHolder: SearchTeamViewHolder, position: Int, model:Team) {
                teamViewHolder.itemView.setOnClickListener {
                    //view-> listener for item click
                    run {
                        val name=model.teamName
                        val city=model.city
                        val teamId=model.teamId


                        val intent= Intent(context,TeamDetailActivity::class.java)
                        intent.putExtra("teamName",name)
                        intent.putExtra("teamCity",city)
                        intent.putExtra("teamId",teamId)
                        startActivity(intent)

                        /**
                       activity?.supportFragmentManager
                            ?.beginTransaction()
                            ?.add(R.id.fragmentContainer,teamDetailFragment)
                            ?.addToBackStack(null)
                            ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            ?.commit()
                       **/
                        recyclerViewTeamSearchFragment.adapter=null


                        Log.d("SearchTeam",name)
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
        recyclerViewTeamSearchFragment.adapter= firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()

    }

    class SearchTeamViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView!!)
    {

        internal  var tLogo=itemView!!.findViewById<ImageView>(R.id.search_team_logo)
        internal var tName=itemView!!.findViewById<TextView>(R.id.search_team_name)
        internal var tAdmin=itemView!!.findViewById<TextView>(R.id.search_team_admin)
        internal var tCity=itemView!!.findViewById<TextView>(R.id.search_team_city)


    }


}
