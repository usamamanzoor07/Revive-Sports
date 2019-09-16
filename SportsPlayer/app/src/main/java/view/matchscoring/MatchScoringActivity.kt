package view.matchscoring

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import com.example.sportsplayer.R
import com.pawegio.kandroid.toast
import kotlinx.android.synthetic.main.activity_match_scoring.*

@Suppress("DEPRECATION")
class MatchScoringActivity : AppCompatActivity(), View.OnClickListener{


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_match_scoring)

            batsman_1_card.setOnClickListener {
                toast("Clicked")
                batsman_2_card.strokeColor = resources.getColor(R.color.darkGray)
                batsman_1_card.strokeColor = resources.getColor(R.color.BlueViolet)
                batsman_1_card.isChecked=true

            }
            batsman_2_card.setOnClickListener {
                batsman_1_card.strokeColor = resources.getColor(R.color.darkGray)
                batsman_2_card.strokeColor = resources.getColor(R.color.BlueViolet)
                 batsman_1_card.isChecked=false

            }

            batsman_1_card.setOnCheckedChangeListener { card, isChecked ->
                toast("setOnCheckedChangeListener")
                card.strokeColor=resources.getColor(R.color.DarkRed)
                if(isChecked)
                {
                    toast("CardView is Checked")
                }else{
                    toast("UnChecked")

                }
            }

    }


fun switchStrokeWidth(view:CardView)
{

}

        override fun onClick(view: View?) {

        when(view?.id)
        {
            /**
            R.id.batsman_1_card->{

                batsman_1_card.isChecked=true

            }
**/
        }

        }



    }
