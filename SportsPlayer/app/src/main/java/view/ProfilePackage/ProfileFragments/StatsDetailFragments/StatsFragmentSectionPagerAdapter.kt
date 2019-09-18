package view.ProfilePackage.ProfileFragments.StatsDetailFragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter



class StatsFragmentSectionPagerAdapter ( fm: FragmentManager):
    FragmentPagerAdapter(fm)  {


    override fun getItem(position: Int): Fragment {
        return when(position)
        {
            0->{
                BattingStatsFragment()
            }
            1->{
                BowlingStatsFragment()
            }
            2->{
                FieldingStatsFragment()
            }
            3->{
                CaptainStatsFragment()
            }
            else->{return BattingStatsFragment()
            }
        }
    }


    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {

        return when(position)
        {
            0->"Batting"
            1->"Bowling"
            2->"Fielding"
            3->"Captain"


            else->{return "Batting"}
        }
    }



}
