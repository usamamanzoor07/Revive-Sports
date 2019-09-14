package view

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pawegio.kandroid.progressDialog
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.edit_profile_activity.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditProfile:AppCompatActivity(){

    private lateinit var currentPlayer:String
    private var firebaseDatabase: FirebaseDatabase? = null
    private var playerDatabaseRef: DatabaseReference? = null
    private var myStorageReference: StorageReference?= null
    private lateinit var playingRole:String
    private lateinit var battingStyle:String
    private lateinit var bowlingStyle :String
    var selectedLogoUri: Uri?=null //selected profile Image Uri
    private val CODE_LOGO_GALLERY=1
    private val SAMPLE_CROPPED_LOGO_NAME="SampleCropLogo"

    lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_activity)
        currentPlayer = FirebaseAuth.getInstance().uid.toString()

        makeButtonDisableInvisible(update_photo_Button_EditProfileActivity)
        makeButtonEnableVisible(change_photo_Button_EditProfileActivity)
        firebaseDatabase = FirebaseDatabase.getInstance()
        playerDatabaseRef = firebaseDatabase!!.getReference("PlayerBasicProfile")
        myStorageReference = FirebaseStorage.getInstance().reference

        supportActionBar?.title = "Update Profile"
        showProfileData()
//select new Profile Image
        change_photo_Button_EditProfileActivity.setOnClickListener { selectProfileImage() }
        //update new Profile Image
        update_photo_Button_EditProfileActivity.setOnClickListener { updateProfileImage() }

        gender_edit_profile.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            gender =radio.text.toString()
            toast("Gender: $gender")
        }

        date_of_birth_edit_profile.setOnClickListener {
            setDateOfBirth()
        }
        date_of_birth_edit_profile.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
            {setDateOfBirth()}
        }

        update_button_edit_profile.setOnClickListener{ updateProfileData() }

        val roleSpinner=find<Spinner>(R.id.playing_role_spinner_edit_profile)
        //[Playing Role Spinner initialization]
        ArrayAdapter.createFromResource(this,R.array.Playing_Role,android.R.layout.simple_spinner_item).also {
                adapter-> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            roleSpinner.adapter = adapter
        }
        //[playing Role Spinner ItemSelectedListener]
        roleSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                playingRole=roleSpinner.selectedItem.toString()
                Log.d("Playing_Roll",playingRole)
            }
        }
        //Batting Style Spinner
        val battingStyleSpinner=find<Spinner>(R.id.batting_style_spinner_edit_profile)
        ArrayAdapter.createFromResource(this,R.array.Batting_Style,android.R.layout.simple_spinner_item).also {
                adapter-> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            battingStyleSpinner.adapter = adapter
        }
        battingStyleSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                battingStyle=battingStyleSpinner.selectedItem.toString()
                Log.d("BatingStyle",battingStyle)
            }
        }

        //Bowling Style Spinner
        val bowlingStyleSpinner=find<Spinner>(R.id.bowling_style_spinner_edit_profile)
        ArrayAdapter.createFromResource(this,R.array.Bowling_Style,android.R.layout.simple_spinner_item).also {
                adapter-> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            bowlingStyleSpinner.adapter = adapter
        }
        bowlingStyleSpinner.onItemSelectedListener=object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                bowlingStyle=bowlingStyleSpinner.selectedItem.toString()
                Log.d("BowlingStyle",bowlingStyle)
            }
        }


    }

    override fun onStart() {
        super.onStart()
        playingRole = ""
        battingStyle = ""
        bowlingStyle = ""
        gender = ""

    }

    private fun makeButtonEnableVisible(vararg view:View)
    {
        for(v in view){
            v.isEnabled=true
            v.visibility=View.VISIBLE
        }
    }
    private fun makeButtonDisableInvisible(vararg view:View)
    {
        for (v in view)
        {    v.isEnabled=false
            v.visibility=View.GONE
        }
    }
    fun selectProfileImage()
    {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery,CODE_LOGO_GALLERY)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==RESULT_OK && data!=null)
            when( requestCode) {
                CODE_LOGO_GALLERY->{
                    val imageUri=data.data
                    if(imageUri!=null) {
                        startCrop(imageUri)
                    }
                }
                UCrop.REQUEST_CROP->{
                    selectedLogoUri= UCrop.getOutput(data)
                    if(selectedLogoUri != null) {
                        change_photo_edit_profile.setImageURI(selectedLogoUri)
                        makeButtonDisableInvisible(change_photo_Button_EditProfileActivity)
                        makeButtonEnableVisible(update_photo_Button_EditProfileActivity)

                    }
                }
            }
    }



    private fun startCrop(@NonNull uri:Uri){

        var destinationFileName=SAMPLE_CROPPED_LOGO_NAME
        destinationFileName+=".jpg"
        val uCrop:UCrop= UCrop.of(uri, Uri.fromFile(File(cacheDir,destinationFileName)))
        uCrop.withAspectRatio(16f, 9f)
        uCrop.withMaxResultSize(450,450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this)
    }

    private fun getCropOptions():UCrop.Options
    {
        val options=UCrop.Options()
        options.setCompressionQuality(70)
        //Compression Type
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
        //Ui
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(true)
        //Color
        options.setStatusBarColor(resources.getColor(R.color.colorPrimary))
        options.setToolbarColor(resources.getColor(R.color.colorPrimaryDark))
        options.setToolbarTitle("Crop Team Logo")
        return options
    }

    private fun showProfileData(){
        playerDatabaseRef?.child(currentPlayer)?.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                val playerImage=p0.child("profile_img").value.toString()
                val playerName=p0.child("name").value.toString()
                val mobileNumber=p0.child("phoneNumber").value.toString()
                val playerLocation=p0.child("Location").value.toString()
                val playerDob=p0.child("dateOfBirth").value.toString()
                val playerPR=p0.child("playing_role").value.toString()
                val playerBatS=p0.child("batting_style").value.toString()
                val playerBowlS=p0.child("bowling_style").value.toString()

                Picasso
                    .get()
                    .load(playerImage)
                    .fit() // use fit() and centerInside() for making it memory efficient.
                    .centerInside()
                    .into(change_photo_edit_profile)
                edit_name_edit_profile.setText(playerName)
                edit_phone_number_edit_profile.setText(mobileNumber)
                edit_location_edit_profile.setText(playerLocation)
                date_of_birth_edit_profile.setText(playerDob)


            }
        })
    }


    //function to upload the profile image
    private fun updateProfileImage(){
        val dialog=progressDialog(message = "please wait a bit", title = "Updating Image ")
        dialog.show()
        var imageLink: String
        val uId=UUID.randomUUID().toString()
        val imageName = "Profile/$uId.jpg"
        val storageRef = myStorageReference!!.child(imageName)
        storageRef.putFile(selectedLogoUri!!).addOnProgressListener { task->
            val progress =((100.0*task.bytesTransferred)/(task.totalByteCount)).toFloat()
            Log.d("PROGRESS","$progress")
            dialog.progress
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog.incrementProgressBy(progress.toInt())
        }.addOnSuccessListener { snapshot ->
            val result = snapshot.metadata!!.reference!!.downloadUrl
            result.addOnSuccessListener {
                imageLink = it.toString()
                Log.d("ImageLink", imageLink)
                val userUpdate = HashMap<String,Any>()
                userUpdate["$currentPlayer/profile_img"] =imageLink
                playerDatabaseRef?.updateChildren(userUpdate)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()
                        toast("Profile Image Updated")
                    }
                }?.addOnFailureListener { exception ->
                    toast(exception.localizedMessage.toString())
                    dialog.dismiss()
                }

            }.addOnFailureListener { Exception ->
                toast(Exception.localizedMessage.toString())
            }
        }
    }


    private fun updateProfileData(){
        toast("Update Starting")
        // val changePhotoUri = change_photo_edit_profile
        val name = edit_name_edit_profile.text.toString()
        val phoneNumber = edit_phone_number_edit_profile.text.toString()
        val location = edit_location_edit_profile.text.toString()
        val dob = date_of_birth_edit_profile.text.toString()
        if (gender.isNotEmpty() && name.isNotEmpty() && phoneNumber.isNotEmpty() && location.isNotEmpty() && dob.isNotEmpty())
        {
            //by using firebase database instance we get reference to the specific Node
            val userUpdate = HashMap<String,Any>()
            //userUpdate["$playerId/profile_img"] = changePhotoUri
            userUpdate["$currentPlayer/gender"] = gender
            userUpdate["$currentPlayer/name"] = name
            userUpdate["$currentPlayer/phoneNumber"] = phoneNumber
            userUpdate["$currentPlayer/Location"] = location
            userUpdate["$currentPlayer/dateOfBirth"] = dob
            userUpdate["$currentPlayer/playing_role"] = playingRole
            userUpdate["$currentPlayer/batting_style"] = battingStyle
            userUpdate["$currentPlayer/bowling_style"] = bowlingStyle

            playerDatabaseRef?.updateChildren(userUpdate)?.addOnCompleteListener { task ->
                if(task.isSuccessful)
                { toast("Profile Updated Successfully")
                    startActivity<Dashboard>()
                    finish()
                }
            }?.addOnFailureListener { Exception-> toast(Exception.localizedMessage) }
        }
        else{
            toast("Failure of Updating Data")
        }


    }

    private fun setDateOfBirth(){
        val cal = Calendar.getInstance()
        // cal.add(Calendar.YEAR)
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            date_of_birth_edit_profile.setText(sdf.format(cal.time))

        }

        DatePickerDialog(
            this, dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



}
