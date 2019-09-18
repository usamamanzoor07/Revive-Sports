package view.team.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pawegio.kandroid.toast
import com.pawegio.kandroid.visible
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_team_member.*
import kotlinx.android.synthetic.main.fragment_team_member.recyclerView_TeamMemberFragment
import kotlinx.android.synthetic.main.team_member_list.view.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivityForResult
import view.fragment.SearchPlayerToAddInTeam


class TeamMemberFragment(val teamId: String,val captainId:String) : Fragment(),View.OnClickListener {
    private var listener: OnFragmentInteractionListener? = null
    val groupAdapter= GroupAdapter<ViewHolder>() //groupi Adapter

    lateinit var removePopUpDialog: Dialog
    private val currentPlayer = FirebaseAuth.getInstance().uid.toString()
    private val requestCode = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_team_member, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        removePopUpDialog=Dialog(activity!!)  //Dialog Initialization
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
        fun onFragmentInteraction(uri:Uri)
    }

    override fun onStart() {
        super.onStart()

        groupAdapter.setOnItemClickListener { item, _ ->

            val player=item as TeamsPlayer
            val p_id=player.playerId
            val p_phn=player.phn
            val p_bating_S=player.bating_S
            val p_bowling_S=player.bowling_S
            val p_image=player.playerImage
            val p_city=player.p_city
            showRemovePopUpDialog(p_image,p_phn,p_bating_S,p_bowling_S,p_id,p_city)
            toast("GroupAdapter Clicked")
        }
    }



    private fun showRemovePopUpDialog(p_image:String, phn:String, batting_STYLE:String, bowling_STYLE:String,player_Id:String,p_city:String)
    {
        removePopUpDialog.setCancelable(true)
        val view=activity?.layoutInflater?.inflate(R.layout.remove_player_popup,null)
        removePopUpDialog.setContentView(view)
        val cancel=view?.find<TextView>(R.id.cancel_text_RemovePlayerPopUp)
        val image=view?.find<ImageView>(R.id.profile_Image_RemovePlayerPopUp)
        val phone=view?.find<TextView>(R.id.player_phn_RemovePlayerPopUp)
        val batting_S=view?.find<TextView>(R.id.playerBatting_S_RemovePlayerPopUp)
        val bowling_S=view?.find<TextView>(R.id.playerBowling_S_RemovePlayerPopUp)
        val playerCity=view?.find<TextView>(R.id.playerCity_RemovePlayerPopUp)
        val remove=view?.find<Button>(R.id.removeButton_RemovePlayerPopUp)
        val cancelButton=view?.find<Button>(R.id.cancelButton_RemovePlayerPopUp)
        cancel?.setOnClickListener { removePopUpDialog.dismiss() }
        cancelButton?.setOnClickListener { removePopUpDialog.dismiss() }
        Picasso.get().load(p_image).into(image)
        phone?.text=phn
        batting_S?.text=batting_STYLE
        bowling_S?.text=bowling_STYLE
        playerCity?.text=p_city
        remove?.setOnClickListener {
            AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("Conformation")
                .setMessage("Do you really want to remove this PlayerBasicProfile")
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Yes"){ dialog, _ -> removeSelectedPlayer(player_Id)
                    dialog.dismiss()
                }
                .create().show()
        }

        removePopUpDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        removePopUpDialog.show()
    }

    private fun removeMe(currentPlayer:String){

            AlertDialog.Builder(activity)
                .setCancelable(true)
                .setTitle("Conformation")
                .setMessage("Do you really want to Leave this Team")
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Yes"){ dialog, _ -> removeSelectedPlayer(currentPlayer)
                    dialog.dismiss()
                }
                .create().show()

    }


    override fun onResume() {
        super.onResume()
        groupAdapter.clear()
        fetchTeamMember(teamId)
        //assign Click Listener to Button
        removeMe_Button_team_member.setOnClickListener(this)
        addPlayer_Button_team_member.setOnClickListener(this)

        if (currentPlayer!=captainId) {
            makeViewsInvisible(addPlayer_Button_team_member)
        }
    }


    private fun removeSelectedPlayer(p_Id: String)
    {

        toast(currentPlayer)
        if (currentPlayer==captainId){
            toast("You can't be removed while captain")
        }
        else{
        val newDatabaseReference=FirebaseDatabase.getInstance().reference
        val removePlayer=HashMap<String,String?>()
        removePlayer["/TeamsPlayer/$teamId/$p_Id"]=null
        removePlayer["/PlayersTeam/$p_Id/$teamId"]=null
        newDatabaseReference.updateChildren(removePlayer as Map<String, Any>).addOnCompleteListener {

                task ->
            if(task.isSuccessful)
            {toast("Player is Removed")
                removePopUpDialog.dismiss()
            }
        }
        }
    }

    override fun onClick(view: View?) {
        when(view?.id)
        {
            R.id.addPlayer_Button_team_member->{
                val intent = Intent(activity, SearchPlayerToAddInTeam::class.java)
                intent.putExtra("teamId", teamId)
                this.startActivityForResult(intent, requestCode)
            }
            R.id.removeMe_Button_team_member->{
                removeMe(currentPlayer)
            }

        }
    }

    private fun makeViewsInvisible(vararg view:View)
    {
        for(v in view)
        {
            v.visible=false
        }
    }




    private fun fetchTeamMember(teamId:String)
    {
        groupAdapter.clear()
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

                                    //get the actual player
                                    val bowling_style=p0.child("bowling_style").value.toString()
                                    val playerPhn=p0.child("phoneNumber").value.toString()
                                    //   val playerCity=p0.child("city").value.toString()
                                    val batting_style=p0.child("batting_style").value.toString()

                                    val playing_role=p0.child("playing_role").value.toString()
                                    val playerName=p0.child("name").value.toString()
                                    val player_id=p0.child("playerId").value.toString()
                                    val playerImage=p0.child("profile_img").value.toString()
                                    val pCity=p0.child("Location").value.toString()
                                    //cardView color
                                    val red=(10..230).random()
                                    val green=(10..230).random()
                                    val blue=(10..230).random()
                                    val color= Color.argb(255,red,green,blue)
                                    groupAdapter.add(TeamsPlayer(playerImage,playerName,playing_role,color,player_id,batting_style,bowling_style,playerPhn,pCity))
                                }

                            })
                        }
                    }

                    recyclerView_TeamMemberFragment.adapter=groupAdapter

                }
            }
        })


    }



    class TeamsPlayer(var playerImage:String,
                      var playerName:String,
                      var playerRole:String,
                      val color:Int,
                      val playerId: String,
                      val bating_S: String,
                      val bowling_S: String,
                      val phn: String,
                      val p_city: String): Item<ViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.team_member_list
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.cardView_teamMemberList.setCardBackgroundColor(color)
            val image=viewHolder.itemView.findViewById<ImageView>(R.id.playerImage_team_member_list)
            Picasso.get().load(playerImage).into(image)
            viewHolder.itemView.playerName_teamMemberList.text=playerName
            viewHolder.itemView.playerRole_teamMemberList.text=playerRole
        }


    }

}
