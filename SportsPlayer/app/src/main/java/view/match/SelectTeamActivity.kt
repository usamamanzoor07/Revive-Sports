package view.match

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.sportsplayer.R
import kotlinx.android.synthetic.main.activity_select_team.*
import view.match.ui.SearchTeamForMatch
import view.match.ui.SectionPagerAdapterMatch
import view.match.ui.TeamAvailableFragment

class SelectTeamActivity : AppCompatActivity(),
    TeamAvailableFragment.OnFragmentInteractionListener,
    SearchTeamForMatch.OnFragmentInteractionListener {

    override fun onFragmentInteraction(teamId: String,teamLogo:String,teamName:String,teamSquad:ArrayList<String>) {
        Log.d("onFragmentInteraction",teamId)
        Log.d("onFragmentInteraction",teamLogo)

        val intent = Intent()
        intent.putStringArrayListExtra("teamSquad",teamSquad)
        intent.putExtra("teamId",teamId)
        intent.putExtra("teamLogo",teamLogo)
        intent.putExtra("teamName",teamName)
        setResult(Activity.RESULT_OK, intent)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("SelectTeamActivity","onActivityResult called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_team)
        val fragmentAdapter=SectionPagerAdapterMatch(supportFragmentManager)
        viewPager_SelectTeamActivity.adapter=fragmentAdapter
        tabLayout_SelectTeamActivity.setupWithViewPager(viewPager_SelectTeamActivity)
    }

    override fun onResume() {
        super.onResume()

        val teamData=intent.extras
        if(teamData!==null)
        {
            val teamId=teamData.getString("teamId")
            val teamLogo=teamData.getString("teamLogo")
            Log.d("Select Team Activity",teamId)
        }else{
            Log.d("Select Team Activity","  NULL")
        }


    }


    override fun onStart() {
        super.onStart()
        supportActionBar?.title="Select Team"

    }
}
