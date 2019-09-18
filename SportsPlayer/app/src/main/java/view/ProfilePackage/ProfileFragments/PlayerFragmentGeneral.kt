package view.ProfilePackage.ProfileFragments
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_general_info_profile.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PlayerFragmentGeneral.OnFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */

class PlayerFragmentGeneral:Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    var mAuth: FirebaseAuth?=null
    var firebaseDatabase: FirebaseDatabase?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_general_info_profile, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
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

    override fun onResume() {
        super.onResume()
        mAuth= FirebaseAuth.getInstance()
        getProfileBasicData()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    private fun getProfileBasicData()
    {

        val uid = mAuth?.uid

        if(uid!=null)
        {
            val playerRef= FirebaseDatabase.getInstance().getReference("/PlayerBasicProfile/$uid") //by using firebase database instance we get reference to the specific Node
            playerRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        val gender=p0.child("gender").value.toString()
                        val dateOfBirth=p0.child("dateOfBirth").value.toString()
                        val phoneNo=p0.child("phoneNumber").value.toString()
                        val playing_role=p0.child("playing_role").value.toString()
                        val batting_style=p0.child("batting_style").value.toString()
                        val bowling_style=p0.child("bowling_style").value.toString()


                        gender_general_fragment.setText(gender)
                        dob_general_fragment.setText(dateOfBirth)
                        mobile_no_general_fragment.setText(phoneNo)
                        playing_role_general_fragment.setText(playing_role)
                        batting_style_general_fragment.setText(batting_style)
                        bowling_style_general_fragment.setText(bowling_style)

                    }
                }
            }
            )
        }
    }


}