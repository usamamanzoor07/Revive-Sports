package view
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.app.ProgressDialog.STYLE_HORIZONTAL
import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pawegio.kandroid.progressDialog
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.player_signup.*
import model.*
import org.jetbrains.anko.*
import java.io.File

class PlayerSignUp:AppCompatActivity(),View.OnClickListener {

    private var playerAuth: FirebaseAuth? = null
    private var uAuthListener: FirebaseAuth.AuthStateListener? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var playerDatabaseRef: DatabaseReference? = null
    private var rootDatabaseRef: DatabaseReference? = null
    private var myStorageReference: StorageReference? = null
    private var selectedProfileUri: Uri? = null
    private var isSignInButtonClicked=false
    private var isSignUpButtonClick=false
private val CODE_IMAGE_GALLERY=10
private val SAMPLE_CROPPED_IMAGE_NAME="SampleCropImage"
@SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_signup)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }


        playerAuth = FirebaseAuth.getInstance()
        uAuthListener = FirebaseAuth.AuthStateListener { }
        firebaseDatabase = FirebaseDatabase.getInstance()
        playerDatabaseRef = firebaseDatabase!!.getReference("PlayerBasicProfile")
        rootDatabaseRef = firebaseDatabase!!.getReference("")
        myStorageReference = FirebaseStorage.getInstance().reference

        phoneNumber_field.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
            { startActivityForResult<PhoneHintActivity>(PHONE_HINT)}
        }



        // Assign click listeners
        user_profile.setOnClickListener(this)
        signUp_button.setOnClickListener(this)
        phoneNumber_field.setOnClickListener(this)
        signIn_button.setOnClickListener(this)


    }


    fun selectProfileImage() {
        val gallery = Intent(Intent.ACTION_PICK)
        gallery.type = "image/*"
        startActivityForResult(gallery,CODE_IMAGE_GALLERY)

    }

    //function to upload the profile image
    private fun uploadProfileImage(){
        val dialog=progressDialog(message = "please wait a bit", title = "uploading image")
        dialog.show()
        var imageLink: String
        val playerId = playerAuth?.uid
        val imageName = "Profile/$playerId.jpg"
        val storageRef = myStorageReference!!.child(imageName)
        storageRef.putFile(selectedProfileUri!!).addOnProgressListener { task->


            val progress =((100.0*task.bytesTransferred)/(task.totalByteCount)).toFloat()

            Log.d("PROGRESS","$progress")
            dialog.progress
            dialog.setProgressStyle(STYLE_HORIZONTAL)
            dialog.incrementProgressBy(progress.toInt())

        }?.addOnSuccessListener { snapshot ->
            val result = snapshot.metadata!!.reference!!.downloadUrl
            result.addOnSuccessListener {
                imageLink = it.toString()
                Log.d("ImageLink", imageLink)
                dialog.dismiss()
                savePlayerData(imageLink)
            }

        }?.addOnFailureListener { Exception ->
            toast(Exception.localizedMessage.toString())

        }
    }


    private fun savePlayerData(downloadUrl: String) {

        val name = fullnameEditText.text.toString()
        val phone = phoneNumber_field.text.toString()
        if (name.isEmpty() || phone.isEmpty())
        {

            alert {
                toast("empty fields")
                title = "Empty Fields"
                message = "please set all the fields carefully"
                neutralPressed("OK"){
                        dialog ->
                    dialog.dismiss()
                }
            }.show()
        }else{

           val progressDialog: ProgressDialog = show(this, "Player Registration", "Registering...")
            progressDialog.show()

            Log.d("downloadUrl_Image", downloadUrl)

            val playerId = playerAuth?.uid.toString()
            val player = PlayerBasicProfile(downloadUrl,name,phone,playerId)
            val bat_stats = BattingStats()
            val bowl_stats = BowlingStats()
            val field_stats = FieldingStats()
            val captain_stats = CaptainStats()
            rootDatabaseRef?.child("BattingStats")?.child(playerId)?.setValue(bat_stats)
            rootDatabaseRef?.child("BowlingStats")?.child(playerId)?.setValue(bowl_stats)
            rootDatabaseRef?.child("FieldingStats")?.child(playerId)?.setValue(field_stats)
            rootDatabaseRef?.child("CaptainStats")?.child(playerId)?.setValue(captain_stats)
            playerDatabaseRef?.child(playerId)?.setValue(player)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("player saved")
                    progressDialog.dismiss()

                    startActivity<EditProfile>()
                    //go to dashboard
                }
            }?.addOnFailureListener { exception ->
                toast(exception.localizedMessage.toString())
               progressDialog.dismiss()

            }

        }


    }

    private fun isRecordExist(){
        var exist=false
        val playerId = playerAuth?.uid.toString()
           playerDatabaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
               override fun onDataChange(p0: DataSnapshot) {
                   if (p0.exists()) {
                       for (u in p0.children) {
                           Log.d("UserExist_Key", u.key)
                           if (u.key == playerId) {
                               exist=true
                               longToast("record Found")
                               startActivity<Dashboard>()
                               break
                               }
                           }
                       if(!exist)
                       {
                           alert{
                               title="User Record"
                               message="No record found.."
                               neutralPressed("Sign Up"){
                                   dialog ->
                                   startActivity<PlayerSignUp>()
                                   dialog.dismiss()
                               }
                           }.show()
                       }
                       }
                   }
               override fun onCancelled(p0: DatabaseError) {}
           })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                CODE_IMAGE_GALLERY->{
                    val imageUri=data.data

                    if(imageUri!=null) {
                        startCrop(imageUri)
                    }
                }
                UCrop.REQUEST_CROP->{
                    selectedProfileUri= UCrop.getOutput(data)
                    if(selectedProfileUri != null) {
                        user_profile.setImageURI(selectedProfileUri)
                    }
                }

                PHONE_HINT-> {
                    val number = data.getStringExtra("number")
                    phoneNumber_field.setText(number)
                }

                PLAYER_LOCATION -> {

                }

                PHONE_AUTH_SUCCESSFUL->{
                    val success = data.getStringExtra("status")
                    val number = data.getStringExtra("number")
                    Log.d("Success",number)
                    if(success=="1") {
                        when {isSignUpButtonClick -> {
                            Log.d("PlayerSignUp_Button",isSignUpButtonClick.toString())
                            uploadProfileImage()
                        }
                            isSignInButtonClicked -> {
                                isRecordExist()
                            }

                        }
                    }else{
                        toast("status failed")
                    }


                }
            }
        }
    }


    private fun startCrop(@NonNull uri:Uri){

        var destinationFileName=SAMPLE_CROPPED_IMAGE_NAME
        destinationFileName+=".jpg"

        val uCrop:UCrop= UCrop.of(uri, Uri.fromFile(File(cacheDir,destinationFileName)))
        uCrop.withAspectRatio(16f, 9f)
//        uCrop.withAspectRatio(3,4)
//        uCrop.useSourceImageAspectRatio()
//        uCrop.withAspectRatio(2,3)
//        uCrop.withAspectRatio(16,9)
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

        options.setToolbarTitle("Crop Profile Image")

        return options

    }




    override fun onClick(view: View) {
        when (view.id) {

            R.id.user_profile -> {
                selectProfileImage()
            }
            R.id.signUp_button -> {
                val name = fullnameEditText.text.toString()
                val phone = phoneNumber_field.text.toString()
                if (name.isEmpty() || phone.isEmpty())
                {

                    alert{
                        title = "Empty Fields"
                        message = "please set all the fields carefully"
                        neutralPressed("OK"){
                                dialog ->
                            dialog.dismiss()
                        }
                    }.show()
                }
                else {
                    if (selectedProfileUri != null) {
                            isSignUpButtonClick=true
                            val number = phoneNumber_field.text.toString().trim()
                            startActivityForResult<PhoneAuthActivity>(
                                PHONE_AUTH_SUCCESSFUL,
                                "number" to number,
                                "signUp" to "7")
                    }else{
                        toast("select profile")
                        alert {
                            title = "Missing Profile Image"
                            message = "please select profile image"
                            neutralPressed("OK"){
                                    dialog ->
                                dialog.dismiss()
                            }
                        }.show()
                    }
                }

            }
            R.id.signIn_button ->{
                isSignInButtonClicked=true
                startActivityForResult<PhoneAuthActivity>(PHONE_AUTH_SUCCESSFUL,"signIn" to "8")
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


        companion object {
            const val PHONE_HINT = 1
            const val PLAYER_LOCATION = 2
            const val PHONE_AUTH_SUCCESSFUL = 3




        }
    }
