package view

import android.app.ProgressDialog
import android.app.ProgressDialog.STYLE_HORIZONTAL
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pawegio.kandroid.progressDialog
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.team_registration.*
import model.Team
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.io.File
import java.util.*
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class TeamRegistration:AppCompatActivity(),View.OnClickListener
{


    var selectedLogoUri: Uri?=null
    private var mAuth: FirebaseAuth? = null
    private var myStorageReference: StorageReference? = null
    val PICK_IMAGE=1
    var databaseRef:FirebaseDatabase?=null
    lateinit var level:String
    private val CODE_LOGO_GALLERY=1
    private val SAMPLE_CROPPED_LOGO_NAME="SampleCropLogo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_registration)


        mAuth = FirebaseAuth.getInstance()
        databaseRef= FirebaseDatabase.getInstance()
        myStorageReference = FirebaseStorage.getInstance().reference
        //assign click listener
        save_team_button.setOnClickListener(this)

        // Get radio group selected item using on checked change listener
        team_lavel_radio_group.setOnCheckedChangeListener { _, checkedId ->
            val radio: RadioButton = find(checkedId)
            level=radio.text.toString()
            toast("Team Level: $level")

        }

    }

    fun selectTeamLogo(v:View)
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
                    team_cicular_logo.setImageURI(selectedLogoUri)
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





    //function to upload the profile image
    private fun uploadTeamLogo(){
        val dialog=progressDialog(message = "please wait a bit", title = "uploading image")
        dialog.show()
        var imageLink: String
        var uId=UUID.randomUUID().toString()
        val imageName = "TeamLogo/$uId.jpg"
        val storageRef = myStorageReference!!.child(imageName)
        storageRef.putFile(selectedLogoUri!!).addOnProgressListener { task->

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
                saveTeam(imageLink)
            }

        }?.addOnFailureListener { Exception ->
            toast(Exception.localizedMessage.toString())

        }
    }



    private fun saveTeam(logoUrl:String)
    {
        val captainId =mAuth?.uid.toString()
        val teamName=team_name.text.toString()
        val shortName=team_short_name.text.toString()
        val city=team_city.text.toString()
        if (teamName.isNotEmpty() && shortName.isNotEmpty() && city.isNotEmpty() && level.isNotEmpty())
        {

            val progressDialog: ProgressDialog = ProgressDialog.show(this, "Creating Team", "saving team...")
            progressDialog.show()
            val newDatabaseReference=databaseRef?.reference

            //generate unique id for team
            val teamId=newDatabaseReference?.push()?.key!!
            Log.d("TeamId  ",teamId)

            //build a team instance
            val team= Team(
                logoUrl,
                teamName,
                shortName,
                captainId,
                city,
                level,
                teamId)

            val createTeamUpdateMember=HashMap<String,Any>()
            createTeamUpdateMember["/Team/$teamId"]=team
            createTeamUpdateMember["/TeamsPlayer/$teamId/$captainId"]=true
            createTeamUpdateMember["/PlayersTeam/$captainId/$teamId"]=true

            newDatabaseReference.updateChildren(createTeamUpdateMember).addOnCompleteListener { task->
                if(task.isSuccessful){

                    toast("Team created")
                    progressDialog.dismiss()
                    finish()
                    //team created.....what next..?
                }
            }.addOnFailureListener { exception ->
                toast(exception.localizedMessage.toString())
                progressDialog.dismiss()

            }

        }

    }

    override fun onClick(v: View?) {

        when(v){
            save_team_button->{

                try {
                    uploadTeamLogo()
                }catch(e:Exception)
                {
                    toast(e.localizedMessage)
                }
            }
        }

    }



























}