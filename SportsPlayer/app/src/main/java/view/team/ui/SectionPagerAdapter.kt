package view.team.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionPagerAdapter(private val teamId:String, fm:FragmentManager):FragmentPagerAdapter(fm)
{
    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0->{TeamMemberFragment(teamId)}
            1->{TeamMatchFragment()}
            2->{TeamStatsFragment()}
            else->{return TeamMatchFragment()}
        }
    }

    override fun getCount(): Int {
        return 3

    }

    override fun getPageTitle(position: Int): CharSequence? {

        return when(position)
        {
            0->"Member"
            1->"Match"
            2->"Stats"
            else->{return "Match"}
        }
    }

}