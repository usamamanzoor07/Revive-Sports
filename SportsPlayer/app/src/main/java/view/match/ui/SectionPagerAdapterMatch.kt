package view.match.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionPagerAdapterMatch(fm: FragmentManager): FragmentPagerAdapter(fm)
{

    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0->{TeamAvailableFragment()}
            1->{SearchTeamForMatch()}
            else->{return TeamAvailableFragment()}
        }
    }

    override fun getCount(): Int {
        return 2
    }


    override fun getPageTitle(position: Int): CharSequence? {

        return when(position)
        {
            0->"My Team"
            1->"Search Team"
            else->{return "My Team"}
        }
    }

}