package view.team.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionPagerAdapter(private val teamId:String, private val captainId:String,fm:FragmentManager):FragmentPagerAdapter(fm)
{
    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0->{TeamMemberFragment(teamId,captainId)}
            1->{TeamRequestMatchFragment()}
            2->{TeamStatsFragment()}
            3->{TeamMatchFragment()}
            else->{return TeamMemberFragment(teamId,captainId)}
        }
    }

    override fun getCount(): Int {
        return 4

    }

    override fun getPageTitle(position: Int): CharSequence? {

        return when(position)
        {
            0->"Member"
            1->"Invites"
            2->"Stats"
            3->"Match"
            else->{return "Member"}
        }
    }

}