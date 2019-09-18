package view.ProfilePackage.ProfileFragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ProfileSectionPagerAdapter( fm: FragmentManager):
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0->{PlayerFragmentGeneral()}
            1->{PlayerFragmentStats()}
            2->{PlayerFragmentMatches()}
            3->{PlayerFragmentTeams()}
            4->{PlayerFragmentMedia()}
            5->{PlayerFragmentConnections()}
            else->{return PlayerFragmentGeneral()}
        }
    }

    override fun getCount(): Int {
        return 6

    }

    override fun getPageTitle(position: Int): CharSequence? {

        return when(position)
        {
            0->"General"
            1->"Stats"
            2->"Matches"
            3->"Teams"
            4->"Media"
            5->"Connections"

            else->{return "General"}
        }
    }
}