package view.match

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sportsplayer.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_select_squad.*
import kotlinx.android.synthetic.main.list_view_selected_squad.view.*
import org.jetbrains.anko.find

class SelectSquadActivity : AppCompatActivity() {
    val groupAdapter = GroupAdapter<ViewHolder>().apply { spanCount = 2 }
    val selectedSquad_List = ArrayList<String>()//Creating an empty arraylist

    val myset = setOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_squad)

        recyclerView_SelectSquadActivity.apply {
            layoutManager = GridLayoutManager(context, groupAdapter.spanCount).apply {
                spanSizeLookup = groupAdapter.spanSizeLookup
            }
            adapter = groupAdapter
        }

        ok_button_SelectedSquadActivity.setOnClickListener {
            val intent = Intent()
            intent.putStringArrayListExtra("list",selectedSquad_List)
            setResult(Activity.RESULT_OK, intent)
            finish()

        }
    }

    override fun onResume() {
        super.onResume()
        val teamId = intent.getStringExtra("teamId")
        Log.d("TeamID_SelectedSquad", teamId)
        if (teamId != null)
            fetchTeamMember(teamId)

    }


    private fun fetchTeamMember(teamId: String) {
        groupAdapter.clear()
        val teamRef = FirebaseDatabase.getInstance()
        val teamsPlayerRef = FirebaseDatabase.getInstance().getReference("/TeamsPlayer/$teamId")
        teamsPlayerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    p0.children.forEach {
                        val playerId = it.key
                        Log.d("TeamMember_ID", playerId)
                        teamRef.getReference("/PlayerBasicProfile/$playerId").also { task ->
                            task.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}
                                override fun onDataChange(p0: DataSnapshot) {
                                    //get the actual player
                                    val playerName = p0.child("name").value.toString()
                                    val player_id = p0.child("playerId").value.toString()
                                    val playerImage = p0.child("profile_img").value.toString()
                                    Log.d("XXXXX_Name", playerName)
                                    Log.d("XXXXX_ID", player_id)
                                    Log.d("XXXXX_Image", playerImage)
                                    //cardView color
                                    val red = (10..230).random()
                                    val green = (10..230).random()
                                    val blue = (10..230).random()
                                    val color = Color.argb(255, red, green, blue)
                                    groupAdapter.add(
                                        SquadHolder(
                                            playerImage,
                                            playerName,
                                            player_id,
                                            color,
                                            this@SelectSquadActivity
                                        )
                                    )
                                }

                            })
                        }
                    }


                }
            }
        })


    }


    fun addPlayerInSquad(view: View, position: Int) {
        val card = view as MaterialCardView
        val item = groupAdapter.getItem(position) as SquadHolder
        val playerId = item.pID
        if (card.isChecked) {
            selectedSquad_List.add(playerId)
        } else {
            if (selectedSquad_List.contains(playerId)) {
                selectedSquad_List.remove(playerId)
            }
        }
        Log.d("SquadList", "${selectedSquad_List.size}")

    }


    class SquadHolder(
        private val imageUri: String,
        val name: String,
        val pID: String,
        val color: Int,
        private val ctx: SelectSquadActivity
    ) : Item<ViewHolder>() {
        lateinit var mCardView: MaterialCardView
        override fun getLayout(): Int {
            return R.layout.list_view_selected_squad
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            mCardView = viewHolder.itemView.find(R.id.mCardView_ListViewSelectedSquad)
            mCardView.setOnClickListener { view ->
                run {
                    val v = view as MaterialCardView
                    v.isChecked = !v.isChecked
                    ctx.addPlayerInSquad(v, position)
                }
            }

            Picasso.get().load(imageUri)
                .into(viewHolder.itemView.player_ImageView_ListViewSelectedSquad)
            viewHolder.itemView.playerName_ListViewSelectedSquad.text = name


        }

        override fun getSpanSize(spanCount: Int, position: Int): Int {
            return spanCount / 2
        }
    }
}
